package org.heigvd.training_generator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.dto.TrainingPlanResponseDto;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.DailyPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlanPhase;
import org.heigvd.entity.TrainingPlan.WeeklyPlan;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutStatus;
import org.heigvd.entity.Workout.WorkoutType;
import org.heigvd.service.GoalService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Générateur de plan d'entraînement version 1.
 * Distribue les sports de manière équilibrée sur la semaine selon les objectifs de l'utilisateur.
 */
@ApplicationScoped
public class TrainingGeneratorV1 implements TrainingGenerator {

    @Inject
    GoalService goalService;

    /** Indique si plusieurs entraînements par jour sont autorisés (utilisateurs avancés uniquement) */
    private static boolean canHaveMultipleWorkoutsPerDay = false;

    /**
     * Permet de générer une plan d'entrainement basé sur les données envoyées par l'utilisateur de l'application
     * mobile.
     * @param trainingPlanRequestDto correspond au payload transmis par l'application mobile
     * @param account correspond au compte de l'utilisateur authentifié
     * @return un TrainingPlan de base sans les workouts
     */
    @Override
    public TrainingPlan generateTrainingPlan(TrainingPlanRequestDto trainingPlanRequestDto, Account account) {
        List<Goal> goals = goalService.getGoalsByIds(trainingPlanRequestDto.getGoalIds());
        if (goals.isEmpty()) {
            throw new IllegalArgumentException("No available goals for the training plan.");
        }

        List<DayOfWeek> daysOfWeek = trainingPlanRequestDto.getDaysOfWeek().stream()
                .map(DayOfWeek::valueOf)
                .toList();

        LocalDate endDate = trainingPlanRequestDto.getEndDate();

        TrainingPlan tp = new TrainingPlan(goals, endDate, daysOfWeek, daysOfWeek, account);

        // Calculs préliminaires
        int nbWorkoutsPerWeek = calculateNbOfWorkoutsPerWeek(goals, account);
        int nbTrainingWeeks = calculateNbOfWeeks(tp);
        LocalDate startDate = tp.getEndDate().minusWeeks(nbTrainingWeeks);

        // Start date should be the monday of the week of the start date
        startDate = startDate.with(DayOfWeek.MONDAY);

        // Détermine si plusieurs entraînements par jour sont autorisés (niveau élevé + objectifs multiples)
        canHaveMultipleWorkoutsPerDay = account.getLastFitnessLevel().getFitnessLevel() >= 60 && goals.size() > 1;

        // Vérification de faisabilité
        if (nbWorkoutsPerWeek > tp.getDaysOfWeek().size() && !canHaveMultipleWorkoutsPerDay) {
            throw new IllegalArgumentException("Pas assez de jours disponibles pour programmer tous les entraînements.");
        }

        // Distribution des sports sur la semaine
        LinkedHashMap<Sport, Integer> sportDistribution = calculateSportDistribution(nbWorkoutsPerWeek, goals);
        List<DailyPlan> dailyPlans = generateWeeklyDailyPlans(tp, sportDistribution);

        tp.setStartDate(startDate);
        tp.setWeeklyPlans(generateWeeklyPlans(dailyPlans, nbTrainingWeeks));

        return tp;
    }

    /**
     * Génère l'ensemble des entraînements pour un plan d'entraînement donné.
     *
     * @param tp le plan d'entraînement à compléter avec les workouts et daily plans
     * @throws IllegalArgumentException si pas assez de jours disponibles pour programmer les entraînements
     */
    @Override
    public TrainingPlan generateTrainingWorkouts(TrainingPlan tp) {
        List<Workout> workouts = generateWorkouts(tp);
        tp.setWorkouts(workouts);

        return tp;
    }

    /**
     * Génère la liste des entraînements pour la première semaine.
     *
     * @param trainingPlan le plan d'entraînement contenant les informations utilisateur
     * @return une liste d'entraînements planifiés pour la première semaine
     */
    private List<Workout> generateWorkouts(TrainingPlan trainingPlan) {
        LocalDate today = LocalDate.now();
        LocalDate firstWeekStart = today.isBefore(trainingPlan.getStartDate()) ? trainingPlan.getStartDate() : today;

        List<DailyPlan> dailyPlans = trainingPlan.getWeeklyPlans().getFirst().getDailyPlans();

        return dailyPlans.stream()
                .map(dailyPlan -> {
                    LocalDate workoutDate = firstWeekStart.with(dailyPlan.getDayOfWeek());
                    return generateWorkout(trainingPlan, workoutDate, dailyPlan.getSport());
                })
                .collect(Collectors.toList());
    }

    /**
     * Génère un entraînement individuel pour une date et un sport donnés.
     *
     * @param trainingPlan le plan d'entraînement pour récupérer les informations utilisateur
     * @param workoutDate la date à laquelle programmer l'entraînement
     * @param sport le sport à pratiquer lors de cet entraînement
     * @return un entraînement planifié avec heure, durée et type définis
     */
    private Workout generateWorkout(TrainingPlan trainingPlan, LocalDate workoutDate, Sport sport) {
        // Heure de début par défaut : 18h
        OffsetDateTime start = workoutDate.atTime(18, 0)
                .atZone(OffsetDateTime.now().getOffset())
                .toOffsetDateTime();

        // Durée selon le sport
        int durationMinutes = switch (sport) {
            case RUNNING -> 45;
            case CYCLING -> 90;
            case SWIMMING -> 30;
            default -> 45;
        };

        OffsetDateTime end = start.plusMinutes(durationMinutes);

        return new Workout(
                trainingPlan.getAccount(),
                sport,
                start,
                end,
                "AUTO_GENERATED_V1",
                WorkoutStatus.PLANNED,
                new ArrayList<>(), // PlannedDataPoints vides pour la V1
                WorkoutType.EF     // Type par défaut : Endurance Fondamentale
        );
    }

    private List<WeeklyPlan> generateWeeklyPlans(List<DailyPlan> dailyPlans, Integer nbTotalOfWeeks) {
        List<WeeklyPlan> weeklyPlans = new ArrayList<>();
        Integer nbCurrentWeek = 1;
        for(TrainingPlanPhase phase : TrainingPlanPhase.values()) {
            int nbWeeks = phase.computeWeeks(nbTotalOfWeeks);
            for(int i = 0; i < nbWeeks; i++) {
                WeeklyPlan weeklyPlan = new WeeklyPlan(dailyPlans, nbCurrentWeek, phase);
                weeklyPlans.add(weeklyPlan);
                nbCurrentWeek++;
            }

        }
        return weeklyPlans;
    }

    /**
     * Génère les plans quotidiens en distribuant les sports de manière équilibrée sur la semaine.
     *
     * @param trainingPlan le plan d'entraînement contenant les jours disponibles
     * @param sportDistribution la répartition des sports avec le nombre d'entraînements par sport
     * @return une liste de plans quotidiens triés par jour de semaine
     */
    private List<DailyPlan> generateWeeklyDailyPlans(TrainingPlan trainingPlan, LinkedHashMap<Sport, Integer> sportDistribution) {
        List<DayOfWeek> daysOfWeek = trainingPlan.getDaysOfWeek();
        int totalDays = daysOfWeek.size();

        Map<Integer, Sport> assignments = new HashMap<>();
        Set<Integer> usedDays = new HashSet<>();

        // Attribution des sports par ordre de fréquence décroissante
        sportDistribution.entrySet().stream()
                .sorted(Map.Entry.<Sport, Integer>comparingByValue().reversed())
                .forEach(entry -> assignSport(entry.getKey(), entry.getValue(), totalDays, assignments, usedDays));

        // Évite les sports consécutifs identiques
        avoidConsecutiveSameSports(assignments, usedDays, totalDays);

        // Construction des plans quotidiens triés par jour de semaine
        List<DailyPlan> dailyPlans = assignments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DailyPlan(daysOfWeek.get(entry.getKey()), entry.getValue(), WorkoutType.EF))
                .toList();

        // Définit le type d'entraînement pour chaque plan quotidien
        defineWorkoutType(trainingPlan, dailyPlans);

        return dailyPlans;
    }


    /**
     * Définit le type d'entraînement pour chaque plan quotidien en fonction du sport.
     * @param tp le plan d'entraînement pour lequel définir les types
     * @param dps la liste des plans quotidiens à mettre à jour
     */
    private void defineWorkoutType(TrainingPlan tp, List<DailyPlan> dps) {

        for(DailyPlan dp : dps) {
            switch(dp.getSport()) {
                case RUNNING -> dp.setWorkoutType(WorkoutType.EF);
                case CYCLING -> dp.setWorkoutType(WorkoutType.EF);
                case SWIMMING -> dp.setWorkoutType(WorkoutType.TECHNIC);
                default -> dp.setWorkoutType(WorkoutType.EF);
            }
        }
    }

    /**
     * Attribue un sport sur les jours disponibles en les espaçant de manière équitable.
     *
     * @param sport le sport à programmer
     * @param count le nombre d'entraînements de ce sport à programmer dans la semaine
     * @param totalDays le nombre total de jours disponibles dans la semaine
     * @param assignments la map des attributions existantes (index jour -> sport)
     * @param usedDays l'ensemble des index de jours déjà utilisés
     * @throws IllegalArgumentException si le nombre d'entraînements dépasse les jours disponibles
     */
    private void assignSport(Sport sport, int count, int totalDays, Map<Integer, Sport> assignments, Set<Integer> usedDays) {
        if (count <= 0) return;

        if (count > totalDays) {
            throw new IllegalArgumentException("Trop d'entraînements à programmer pour les jours disponibles.");
        }

        // Calcul de l'intervalle idéal pour espacer les sessions
        double interval = (double) totalDays / count;

        for (int i = 0; i < count; i++) {
            int idealIndex = (int) Math.round(i * interval);
            int dayIndex = findClosestFreeDay(idealIndex, usedDays, totalDays);

            if (dayIndex != -1) {
                assignments.put(dayIndex, sport);
                usedDays.add(dayIndex);
            }
        }
    }

    /**
     * Évite d'avoir le même sport sur des jours consécutifs en déplaçant vers des jours plus éloignés.
     *
     * @param assignments la map des attributions actuelles (index jour -> sport)
     * @param usedDays l'ensemble des index de jours déjà utilisés
     * @param totalDays le nombre total de jours disponibles dans la semaine
     */
    private void avoidConsecutiveSameSports(Map<Integer, Sport> assignments, Set<Integer> usedDays, int totalDays) {
        List<Integer> sortedDays = assignments.keySet().stream().sorted().toList();

        for (int i = 1; i < sortedDays.size(); i++) {
            int previousDay = sortedDays.get(i - 1);
            int currentDay = sortedDays.get(i);

            // Si deux jours consécutifs ont le même sport
            if (currentDay - previousDay == 1 && assignments.get(previousDay).equals(assignments.get(currentDay))) {
                // Trouve le jour libre le plus éloigné pour déplacer l'entraînement
                for (int j = totalDays - 1; j >= 0; j--) {
                    if (!usedDays.contains(j)) {
                        assignments.put(j, assignments.remove(currentDay));
                        usedDays.add(j);
                        usedDays.remove(currentDay);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Trouve le jour libre le plus proche de l'index cible.
     *
     * @param target l'index du jour idéal recherché
     * @param used l'ensemble des index de jours déjà utilisés
     * @param totalDays le nombre total de jours disponibles
     * @return l'index du jour libre le plus proche, ou -1 si aucun jour libre trouvé
     */
    private int findClosestFreeDay(int target, Set<Integer> used, int totalDays) {
        for (int radius = 0; radius < totalDays; radius++) {
            int lower = target - radius;
            int upper = target + radius;

            if (lower >= 0 && !used.contains(lower)) return lower;
            if (upper < totalDays && !used.contains(upper)) return upper;
        }
        return -1;
    }

    /**
     * Calcule la distribution des sports selon les objectifs et le niveau de forme.
     * Gère l'alternance running/cycling si nécessaire pour éviter la surcharge.
     *
     * @param nbOfWorkoutsPerWeek le nombre total d'entraînements à programmer par semaine
     * @param goals la liste des objectifs de l'utilisateur définissant sports et fréquences
     * @return une map ordonnée contenant la distribution finale des sports (sport -> nombre d'entraînements)
     */
    private LinkedHashMap<Sport, Integer> calculateSportDistribution(int nbOfWorkoutsPerWeek, List<Goal> goals) {
        // Agrégation des entraînements par sport
        Map<Sport, Integer> sportCounts = goals.stream()
                .collect(Collectors.groupingBy(
                        Goal::getSport,
                        Collectors.summingInt(Goal::getNbOfWorkoutsPerWeek)
                ));

        LinkedHashMap<Sport, Integer> sportDistribution = new LinkedHashMap<>(sportCounts);

        // Gestion spéciale running + cycling : alternance si dépassement
        if (sportDistribution.containsKey(Sport.RUNNING) && sportDistribution.containsKey(Sport.CYCLING)) {
            int running = sportDistribution.get(Sport.RUNNING);
            int cycling = sportDistribution.get(Sport.CYCLING);
            int swimming = sportDistribution.getOrDefault(Sport.SWIMMING, 0);
            int availableSlots = Math.max(0, nbOfWorkoutsPerWeek - swimming);

            if (running + cycling > availableSlots) {
                // Réinitialise et alterne running/cycling
                sportDistribution.put(Sport.RUNNING, 0);
                sportDistribution.put(Sport.CYCLING, 0);

                for (int i = 0; i < availableSlots; i++) {
                    Sport sport = (i % 2 == 0) ? Sport.RUNNING : Sport.CYCLING;
                    sportDistribution.merge(sport, 1, Integer::sum);
                }
            }
        }

        return sportDistribution;
    }

    /**
     * Calcule le nombre d'entraînements par semaine en tenant compte du niveau de forme.
     *
     * @param goals la liste des objectifs définissant les sports et fréquences souhaitées
     * @param account le compte utilisateur contenant le niveau de forme actuel
     * @return le nombre d'entraînements par semaine ajusté selon le niveau de forme
     */
    private int calculateNbOfWorkoutsPerWeek(List<Goal> goals, Account account) {
        // Agrégation des entraînements par sport
        Map<Sport, Integer> workoutPerSport = goals.stream()
                .collect(Collectors.groupingBy(
                        Goal::getSport,
                        Collectors.summingInt(Goal::getNbOfWorkoutsPerWeek)
                ));

        int running = workoutPerSport.getOrDefault(Sport.RUNNING, 0);
        int cycling = workoutPerSport.getOrDefault(Sport.CYCLING, 0);
        int swimming = workoutPerSport.getOrDefault(Sport.SWIMMING, 0);

        // Pondération basée sur le niveau de forme (0.45 à 0.95)
        double fitnessLevelPonderation = (double) account.getLastFitnessLevel().getFitnessLevel() / 200 + 0.45;

        // Application de la pondération sur running/cycling, swimming reste inchangé
        return (int) Math.round((running + cycling) * fitnessLevelPonderation + swimming);
    }

    /**
     * Calcule le nombre de semaines d'entraînement basé sur l'objectif le plus long.
     *
     * @param trainingPlan le plan d'entraînement contenant la liste des objectifs
     * @return le nombre de semaines du plus long objectif, ou 0 si aucun objectif
     */
    private int calculateNbOfWeeks(TrainingPlan trainingPlan) {
        return trainingPlan.getGoals().stream()
                .mapToInt(Goal::getNbOfWeek)
                .max()
                .orElse(0);
    }
}