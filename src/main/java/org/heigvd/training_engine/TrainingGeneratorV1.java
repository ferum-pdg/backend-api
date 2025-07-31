package org.heigvd.training_engine;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.util.*;

@ApplicationScoped
public class TrainingGeneratorV1 implements TrainingGenerator {

    public List<Workout> generateTrainingWorkouts(Account account, TrainingPlan trainingPlan) {
        List<Workout> workouts = new ArrayList<>();

        LocalDate currentDate = trainingPlan.getStartDate();
        LocalDate endDate = trainingPlan.getEndDate();
        List<DayOfWeek> availableDays = trainingPlan.getDaysOfWeek().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .toList();

        Goal goal = trainingPlan.getGoals().get(0);
        Sport sport = goal.getSport();
        int nbWorkoutsPerWeek = goal.getNbOfWorkoutsPerWeek();

        // Utilisé pour grouper les jours par semaine
        Map<Integer, List<LocalDate>> daysByWeek = new TreeMap<>();

        // Grouper tous les jours dispo par semaine
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dow = currentDate.getDayOfWeek();
            if (availableDays.contains(dow)) {
                int week = currentDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                daysByWeek.computeIfAbsent(week, w -> new ArrayList<>()).add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        // Pour chaque semaine, on choisit les jours espacés
        for (List<LocalDate> weekDays : daysByWeek.values()) {
            if (weekDays.isEmpty()) continue;

            int available = weekDays.size();
            int toSchedule = Math.min(nbWorkoutsPerWeek, available);

            if (toSchedule == available) {
                // Pas le choix, on les prend tous
                for (LocalDate date : weekDays) {
                    workouts.add(buildWorkout(account, trainingPlan, sport, date));
                }
            } else {
                // Répartition équilibrée
                double step = (double) (available - 1) / (toSchedule - 1);
                for (int i = 0; i < toSchedule; i++) {
                    int index = (int) Math.round(i * step);
                    LocalDate date = weekDays.get(index);
                    workouts.add(buildWorkout(account, trainingPlan, sport, date));
                }
            }
        }

        return workouts;
    }

    private Workout buildWorkout(Account account, TrainingPlan plan, Sport sport, LocalDate date) {
        OffsetDateTime start = date.atTime(18, 0).atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusHours(1);
        return new Workout(account, plan, sport, start, end, "GENERATOR_V1", TrainingStatus.PLANNED);
    }
}
