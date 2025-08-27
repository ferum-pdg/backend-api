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
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.service.GoalService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class TrainingPlanGeneratorV1 {

    @Inject
    GoalService goalService;

    // GENERATORS ------------------------------------------------------------------------------------------------------

    public TrainingPlan generate(TrainingPlanRequestDto tpDto, Account account) {

        List<Goal> goals = goalService.getGoalsByIds(tpDto.getGoalIds());

        if(goals.isEmpty()) {
            throw new IllegalArgumentException("No available goals for the training plan.");
        }

        List<DayOfWeek> availableDays = tpDto.getDaysOfWeek().stream()
                .map(DayOfWeek::valueOf)
                .toList();

        int nbWeeksOfTraining;

        if(tpDto.startNow()) {
            nbWeeksOfTraining = (int) (tpDto.getEndDate().toEpochDay() - LocalDate.now().toEpochDay()) / 7;
        } else {
            nbWeeksOfTraining = calculateNbWeeksOfTraining(goals);
        }

        int nbOfWorkoutsPerWeek = calculateNbOfWorkoutsPerWeek(goals);

        //check if the end week is after the start week
        if(tpDto.getEndDate().isBefore(LocalDate.now().plusWeeks(nbWeeksOfTraining))) {
            throw new IllegalArgumentException("The end date is too soon for the number of weeks of training. In this case it should be at least " + nbWeeksOfTraining + " weeks from now. So the end date should be at least " + LocalDate.now().plusWeeks(nbWeeksOfTraining) + ".");
        }

        if(nbWeeksOfTraining == 0 || nbOfWorkoutsPerWeek == 0) {
            throw new IllegalArgumentException("The provided goals are not valid.");
        }

        boolean multipleWorkoutsPerDay = authorizeMultipleWorkoutsPerDay(
                goals,
                nbOfWorkoutsPerWeek,
                account.getLastFitnessLevel().getFitnessLevel(),
                availableDays.size()
        );

        if(multipleWorkoutsPerDay || nbOfWorkoutsPerWeek > availableDays.size()) {
            throw new IllegalArgumentException("Not enough available days for the number of workouts per week.");
        }

        List<WeeklyPlan> weeklyPlans = generateWeeklyPlans(
                goals,
                nbWeeksOfTraining,
                nbOfWorkoutsPerWeek,
                availableDays
        );

        // Création du TrainingPlan
        TrainingPlan trainingPlan = new TrainingPlan(goals, tpDto.getEndDate(), availableDays, availableDays, account);

        // Calcul de la date de début
        LocalDate startDate = tpDto.getEndDate().minusWeeks(nbWeeksOfTraining);
        startDate = startDate.with(DayOfWeek.MONDAY);
        trainingPlan.setStartDate(startDate);

        trainingPlan.setWeeklyPlans(weeklyPlans);

        return trainingPlan;
    }

    private List<WeeklyPlan> generateWeeklyPlans(List<Goal> goals, int nbWeeksOfTraining, int nbOfWorkoutsPerWeek,
                                                 List<DayOfWeek> availableDays) {

        // Génération du plan quotidien type pour la semaine
        List<DailyPlan> templateDailyPlans = generateDailyPlans(goals, nbOfWorkoutsPerWeek, availableDays);

        if(templateDailyPlans.size() != nbOfWorkoutsPerWeek) {
            throw new IllegalStateException("Error while generating the daily plans.");
        }

        // Génération des plans hebdomadaires avec phases
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

    private List<DailyPlan> generateDailyPlans(List<Goal> goals, int nbOfWorkoutsPerWeek, List<DayOfWeek> availableDays) {
        List<DailyPlan> dailyPlans = List.of();

        if(nbOfWorkoutsPerWeek == availableDays.size()) {
            dailyPlans = generateTrivialDailyPlans(goals, availableDays);
        } else if(nbOfWorkoutsPerWeek < availableDays.size()) {
            // Cas complexe : on cherche une répartition équilibrée
            List<DayOfWeek> optimizedDays = generateTrivialAvailableDays(availableDays, nbOfWorkoutsPerWeek);
            dailyPlans = generateTrivialDailyPlans(goals, optimizedDays);
        } else {
            // Cas complexe : plusieurs séances par jour
            dailyPlans = generateMultiDailyPlans(goals, availableDays);
        }
        
        return dailyPlans;
    }

    private List<DailyPlan> generateTrivialDailyPlans(List<Goal> goals, List<DayOfWeek> availableDays) {
        List<DailyPlan> dailyPlans = new ArrayList<>();
        int goalIndex = 0;
        int totalGoals = goals.size();

        for (DayOfWeek day : availableDays) {
            Goal currentGoal = goals.get(goalIndex);
            dailyPlans.add(new DailyPlan(day, currentGoal.getSport()));

            // Passer au sport suivant pour la prochaine séance
            goalIndex = (goalIndex + 1) % totalGoals;
        }

        return dailyPlans;
    }

    public List<DayOfWeek> generateTrivialAvailableDays(List<DayOfWeek> availableDays, int nbOfWorkoutsPerWeek) {
        List<DayOfWeek> result = new ArrayList<>();
        int size = availableDays.size();
        double step = (double) size / nbOfWorkoutsPerWeek;

        double index = 0;
        for (int i = 0; i < nbOfWorkoutsPerWeek; i++) {
            int chosenIndex = (int) Math.round(index) % size;
            DayOfWeek chosenDay = availableDays.get(chosenIndex);

            // éviter doublon en cas d'arrondi
            if (!result.contains(chosenDay)) {
                result.add(chosenDay);
            } else {
                // si déjà pris, on avance d’un jour
                result.add(availableDays.get((chosenIndex + 1) % size));
            }

            index += step;
        }

        return result;
    }

    public List<DailyPlan> generateMultiDailyPlans(List<Goal> goals, List<DayOfWeek> availableDays) {

        int swimmingNbWorkout = (int) goals.stream()
                .filter(g -> g.getSport().equals(Sport.SWIMMING))
                .mapToInt(Goal::getNbOfWorkoutsPerWeek)
                .sum();

        if(swimmingNbWorkout == 0) {
            throw new IllegalArgumentException("Multiple workouts per day are only allowed if swimming is one of the goals.");
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


    // CALCULATORS -----------------------------------------------------------------------------------------------------

    /**
     * For now, we just take the max number of weeks from the goals, in an updated version we could try to find
     * a better way to calculate the number of weeks.
     * @param goals the list of goals
     * @return the number of weeks of training
     */
    public int calculateNbWeeksOfTraining(List<Goal> goals) {
        return goals.stream().mapToInt(Goal::getNbOfWeek).max().orElse(0);
    }

    /**
     * Return the sum of the number of workouts per week for each goal, adjusted by the fitness level ponderation.
     * @param goals the list of goals
     * @return the number of workouts per week
     */
    public int calculateNbOfWorkoutsPerWeek(List<Goal> goals) {
        return goals.stream().mapToInt(Goal::getNbOfWorkoutsPerWeek).sum();
    }

    /**
     * Authorize multiple workouts per day only if:
     * - The goals contains swimming and at least another sport
     * - The mean fitness level is high enough (>= 65)
     * - The number of workouts per week is higher than the number of available days
     * @param goals the list of goals
     * @param nbOfWorkoutsPerWeek the number of workouts per week
     * @param nbOfAvailableDays the number of available days
     * @return true if multiple workouts per day are authorized, false otherwise
     */
    public boolean authorizeMultipleWorkoutsPerDay(List<Goal> goals, int nbOfWorkoutsPerWeek,
                                                   int meanFitnessLevel, int nbOfAvailableDays) {
        boolean hasSwimming = goals.stream().anyMatch(g -> g.getSport().equals(Sport.SWIMMING));
        boolean hasOtherSport = goals.stream().anyMatch(g -> !g.getSport().equals(Sport.SWIMMING));
        boolean highLevel = meanFitnessLevel >= 65;

        return hasSwimming && hasOtherSport && highLevel && nbOfWorkoutsPerWeek > nbOfAvailableDays;
    }
}