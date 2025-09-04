package org.heigvd.training_generator.generator_V1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.DailyPlan;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.training_plan.WeeklyPlan;
import org.heigvd.service.GoalService;
import org.heigvd.training_generator.interfaces.TrainingPlanGenerator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Version 1 implementation of the training plan generator.
 * This implementation provides basic training plan generation with
 * fundamental scheduling and goal-based planning capabilities.
 *
 * Key features:
 * - Basic goal-based training plan creation
 * - Simple workout distribution across available days
 * - Support for multiple workouts per day (swimming + other sport)
 * - Phase-based training progression
 *
 * @version 1.0
 */
@ApplicationScoped
public class TrainingPlanGeneratorV1 implements TrainingPlanGenerator {

    @Inject
    GoalService goalService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "V1";
    }

    /**
     * Generates a complete training plan based on the provided request and account information.
     * This method orchestrates the entire training plan creation process including
     * goal validation, time calculations, and weekly plan generation.
     *
     * @param tpDto the training plan request containing user preferences and constraints
     * @param account the user account with fitness level and personal information
     * @return a complete training plan with weekly schedules
     * @throws IllegalArgumentException if goals are invalid, dates are incompatible,
     *                                 or there are insufficient available days
     */
    @Override
    public TrainingPlan generate(TrainingPlanRequestDto tpDto, Account account) {

        List<Goal> goals = goalService.getGoalsByIds(tpDto.getGoalIds());

        if (goals.isEmpty()) {
            throw new IllegalArgumentException("No available goals for the training plan.");
        }

        List<DayOfWeek> availableDays = tpDto.getDaysOfWeek().stream()
                .map(DayOfWeek::valueOf)
                .toList();

        int nbWeeksOfTraining = calculateTrainingWeeks(tpDto);
        int nbOfWorkoutsPerWeek = calculateNbOfWorkoutsPerWeek(goals);

        validateTrainingDuration(tpDto, nbWeeksOfTraining);
        validateWorkoutParameters(goals, nbOfWorkoutsPerWeek);

        boolean multipleWorkoutsPerDay = authorizeMultipleWorkoutsPerDay(
                goals,
                nbOfWorkoutsPerWeek,
                account.getLastFitnessLevel().getFitnessLevel(),
                availableDays.size()
        );

        validateDayAvailability(multipleWorkoutsPerDay, nbOfWorkoutsPerWeek, availableDays.size());

        List<WeeklyPlan> weeklyPlans = generateWeeklyPlans(
                goals,
                nbWeeksOfTraining,
                nbOfWorkoutsPerWeek,
                availableDays
        );

        TrainingPlan trainingPlan = createTrainingPlan(tpDto, account, goals, availableDays, nbWeeksOfTraining);
        trainingPlan.setWeeklyPlans(weeklyPlans);

        return trainingPlan;
    }

    /**
     * Calculates the number of training weeks based on the request parameters.
     *
     * @param tpDto the training plan request
     * @return the number of weeks for training
     */
    private int calculateTrainingWeeks(TrainingPlanRequestDto tpDto) {
        if (tpDto.startNow()) {
            return (int) (tpDto.getEndDate().toEpochDay() - LocalDate.now().toEpochDay()) / 7;
        } else {
            List<Goal> goals = goalService.getGoalsByIds(tpDto.getGoalIds());
            return calculateNbWeeksOfTraining(goals);
        }
    }

    /**
     * Validates that the training duration is compatible with the end date.
     *
     * @param tpDto the training plan request
     * @param nbWeeksOfTraining the calculated number of training weeks
     * @throws IllegalArgumentException if the end date is too soon
     */
    private void validateTrainingDuration(TrainingPlanRequestDto tpDto, int nbWeeksOfTraining) {
        if (tpDto.getEndDate().isBefore(LocalDate.now().plusWeeks(nbWeeksOfTraining))) {
            throw new IllegalArgumentException(
                    "The end date is too soon for the number of weeks of training. " +
                            "In this case it should be at least " + nbWeeksOfTraining + " weeks from now. " +
                            "So the end date should be at least " + LocalDate.now().plusWeeks(nbWeeksOfTraining) + "."
            );
        }
    }

    /**
     * Validates that the workout parameters are valid.
     *
     * @param goals the list of training goals
     * @param nbOfWorkoutsPerWeek the calculated number of workouts per week
     * @throws IllegalArgumentException if the goals are not valid
     */
    private void validateWorkoutParameters(List<Goal> goals, int nbOfWorkoutsPerWeek) {
        if (goals.isEmpty() || nbOfWorkoutsPerWeek == 0) {
            throw new IllegalArgumentException("The provided goals are not valid.");
        }
    }

    /**
     * Validates that there are enough available days for the planned workouts.
     *
     * @param multipleWorkoutsPerDay whether multiple workouts per day are allowed
     * @param nbOfWorkoutsPerWeek the number of workouts per week
     * @param nbOfAvailableDays the number of available days
     * @throws IllegalArgumentException if there are insufficient available days
     */
    private void validateDayAvailability(boolean multipleWorkoutsPerDay, int nbOfWorkoutsPerWeek, int nbOfAvailableDays) {
        if (!multipleWorkoutsPerDay && nbOfWorkoutsPerWeek > nbOfAvailableDays) {
            throw new IllegalArgumentException("Not enough available days for the number of workouts per week.");
        }
    }

    /**
     * Creates the base training plan object with common properties.
     *
     * @param tpDto the training plan request
     * @param account the user account
     * @param goals the list of training goals
     * @param availableDays the available training days
     * @param nbWeeksOfTraining the number of training weeks
     * @return the initialized training plan
     */
    private TrainingPlan createTrainingPlan(TrainingPlanRequestDto tpDto, Account account,
                                            List<Goal> goals, List<DayOfWeek> availableDays,
                                            int nbWeeksOfTraining) {

        TrainingPlan trainingPlan = new TrainingPlan(goals, tpDto.getEndDate(), availableDays, availableDays, account);

        LocalDate startDate = tpDto.getEndDate().minusWeeks(nbWeeksOfTraining);
        startDate = startDate.with(DayOfWeek.MONDAY);
        trainingPlan.setStartDate(startDate);

        return trainingPlan;
    }

    /**
     * Generates the complete weekly plan structure for the entire training period.
     * Creates weekly plans with appropriate phases and distributes daily workouts.
     *
     * @param goals the training goals
     * @param nbWeeksOfTraining the total number of training weeks
     * @param nbOfWorkoutsPerWeek the number of workouts per week
     * @param availableDays the days available for training
     * @return a list of weekly plans covering the entire training period
     * @throws IllegalStateException if daily plan generation fails
     */
    private List<WeeklyPlan> generateWeeklyPlans(List<Goal> goals, int nbWeeksOfTraining,
                                                 int nbOfWorkoutsPerWeek, List<DayOfWeek> availableDays) {

        List<DailyPlan> templateDailyPlans = generateDailyPlans(goals, nbOfWorkoutsPerWeek, availableDays);

        if (templateDailyPlans.size() != nbOfWorkoutsPerWeek) {
            throw new IllegalStateException("Error while generating the daily plans.");
        }

        List<WeeklyPlan> weeklyPlans = new ArrayList<>();
        int currentWeek = 1;

        for (TrainingPlanPhase phase : TrainingPlanPhase.values()) {
            int weeksInPhase = phase.computeWeeks(nbWeeksOfTraining);

            for (int i = 0; i < weeksInPhase; i++) {
                WeeklyPlan weeklyPlan = new WeeklyPlan(new ArrayList<>(templateDailyPlans), currentWeek, phase);
                weeklyPlans.add(weeklyPlan);
                currentWeek++;
            }
        }

        return weeklyPlans;
    }

    /**
     * Generates the daily training plans based on goals and constraints.
     * Handles different scenarios: equal days/workouts, fewer workouts than days,
     * or multiple workouts per day.
     *
     * @param goals the training goals
     * @param nbOfWorkoutsPerWeek the number of workouts per week
     * @param availableDays the available training days
     * @return a list of daily plans for the week
     */
    private List<DailyPlan> generateDailyPlans(List<Goal> goals, int nbOfWorkoutsPerWeek, List<DayOfWeek> availableDays) {

        List<DailyPlan> dailyPlans;

        if (nbOfWorkoutsPerWeek == availableDays.size()) {
            dailyPlans = generateTrivialDailyPlans(goals, availableDays);

        } else if (nbOfWorkoutsPerWeek < availableDays.size()) {
            List<DayOfWeek> optimizedDays = generateTrivialAvailableDays(availableDays, nbOfWorkoutsPerWeek);
            dailyPlans = generateTrivialDailyPlans(goals, optimizedDays);

        } else {
            dailyPlans = generateMultiDailyPlans(goals, availableDays);
        }

        return dailyPlans;
    }

    /**
     * Generates daily plans with a simple one-to-one mapping of days to workouts.
     * Distributes sports evenly across the available days using a round-robin approach.
     *
     * @param goals the training goals
     * @param availableDays the days available for training
     * @return a list of daily plans with sport assignments
     */
    private List<DailyPlan> generateTrivialDailyPlans(List<Goal> goals, List<DayOfWeek> availableDays) {

        List<DailyPlan> dailyPlans = new ArrayList<>();
        int goalIndex = 0;
        int totalGoals = goals.size();

        for (DayOfWeek day : availableDays) {
            Goal currentGoal = goals.get(goalIndex);
            dailyPlans.add(new DailyPlan(day, currentGoal.getSport()));

            goalIndex = (goalIndex + 1) % totalGoals;
        }

        return dailyPlans;
    }

    /**
     * Optimally distributes workouts across available days when there are more
     * available days than required workouts. Uses even spacing to maximize recovery time.
     *
     * @param availableDays the complete list of available days
     * @param nbOfWorkoutsPerWeek the number of workouts needed
     * @return an optimized subset of available days
     */
    public List<DayOfWeek> generateTrivialAvailableDays(List<DayOfWeek> availableDays, int nbOfWorkoutsPerWeek) {

        List<DayOfWeek> result = new ArrayList<>();
        int size = availableDays.size();
        double step = (double) size / nbOfWorkoutsPerWeek;

        double index = 0;
        for (int i = 0; i < nbOfWorkoutsPerWeek; i++) {
            int chosenIndex = (int) Math.round(index) % size;
            DayOfWeek chosenDay = availableDays.get(chosenIndex);

            if (!result.contains(chosenDay)) {
                result.add(chosenDay);
            } else {
                result.add(availableDays.get((chosenIndex + 1) % size));
            }

            index += step;
        }

        return result;
    }

    /**
     * Generates daily plans for scenarios requiring multiple workouts per day.
     * Currently supports swimming combined with other sports, with swimming
     * workouts distributed separately from other activities.
     *
     * @param goals the training goals
     * @param availableDays the days available for training
     * @return a list of daily plans including multiple workout days
     * @throws IllegalArgumentException if multiple workouts are requested without swimming
     *                                 or if there are insufficient days
     */
    public List<DailyPlan> generateMultiDailyPlans(List<Goal> goals, List<DayOfWeek> availableDays) {

        int swimmingNbWorkout = (int) goals.stream()
                .filter(g -> g.getSport().equals(Sport.SWIMMING))
                .mapToInt(Goal::getNbOfWorkoutsPerWeek)
                .sum();

        int totalNbWorkout = goals.stream()
                .mapToInt(Goal::getNbOfWorkoutsPerWeek)
                .sum();

        if (swimmingNbWorkout == 0) {
            throw new IllegalArgumentException(
                    "Multiple workouts per day are only allowed if swimming is one of the goals."
            );
        }

        if (totalNbWorkout - swimmingNbWorkout > availableDays.size()) {
            throw new IllegalArgumentException("Not enough available days for these goals.");
        }

        List<DayOfWeek> swimmingDays = generateTrivialAvailableDays(availableDays, swimmingNbWorkout);

        List<Goal> nonSwimmingGoals = goals.stream()
                .filter(g -> !g.getSport().equals(Sport.SWIMMING))
                .toList();

        int nonSwimmingNbWorkout = nonSwimmingGoals.stream()
                .mapToInt(Goal::getNbOfWorkoutsPerWeek)
                .sum();

        List<DayOfWeek> nonSwimmingDays = generateTrivialAvailableDays(availableDays, nonSwimmingNbWorkout);

        List<DailyPlan> dailyPlans = new ArrayList<>(generateTrivialDailyPlans(
                List.of(goals.stream()
                        .filter(g -> g.getSport().equals(Sport.SWIMMING))
                        .findFirst()
                        .orElseThrow()),
                swimmingDays));

        dailyPlans.addAll(generateTrivialDailyPlans(nonSwimmingGoals, nonSwimmingDays));
        dailyPlans.sort(Comparator.comparing(DailyPlan::getDayOfWeek));

        return dailyPlans;
    }

    /**
     * Calculates the total number of training weeks required based on goals.
     * Currently uses the maximum number of weeks from all goals.
     * Future versions could implement more sophisticated duration calculations.
     *
     * @param goals the list of training goals
     * @return the number of weeks of training needed
     */
    public int calculateNbWeeksOfTraining(List<Goal> goals) {
        return goals.stream()
                .mapToInt(Goal::getNbOfWeek)
                .max()
                .orElse(0);
    }

    /**
     * Calculates the total number of workouts per week by summing
     * the workout requirements from all goals.
     *
     * @param goals the list of training goals
     * @return the total number of workouts per week
     */
    public int calculateNbOfWorkoutsPerWeek(List<Goal> goals) {
        return goals.stream()
                .mapToInt(Goal::getNbOfWorkoutsPerWeek)
                .sum();
    }

    /**
     * Determines whether multiple workouts per day should be authorized
     * based on sport combination, fitness level, and scheduling constraints.
     *
     * Authorization criteria:
     * - Must include swimming and at least one other sport
     * - Mean fitness level must be 65 or higher
     * - Number of weekly workouts must exceed available days
     *
     * @param goals the training goals
     * @param nbOfWorkoutsPerWeek the number of workouts per week
     * @param meanFitnessLevel the user's fitness level (1-100)
     * @param nbOfAvailableDays the number of available training days
     * @return true if multiple workouts per day are authorized
     */
    public boolean authorizeMultipleWorkoutsPerDay(List<Goal> goals, int nbOfWorkoutsPerWeek,
                                                   int meanFitnessLevel, int nbOfAvailableDays) {

        boolean hasSwimming = goals.stream()
                .anyMatch(g -> g.getSport().equals(Sport.SWIMMING));

        boolean hasOtherSport = goals.stream()
                .anyMatch(g -> !g.getSport().equals(Sport.SWIMMING));

        boolean highLevel = meanFitnessLevel >= 65;

        return hasSwimming && hasOtherSport && highLevel && nbOfWorkoutsPerWeek > nbOfAvailableDays;
    }
}