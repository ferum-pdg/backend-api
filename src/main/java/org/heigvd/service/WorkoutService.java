package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.workout_dto.WorkoutFullDto;
import org.heigvd.dto.workout_dto.WorkoutPlanDetailsDto;
import org.heigvd.dto.workout_dto.WorkoutPlanDto;
import org.heigvd.dto.workout_dto.WorkoutUploadDto;
import org.heigvd.entity.*;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.details.WorkoutPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
/**
 * Service de gestion des séances d'entraînement (Workouts).
 *
 * Permet la recherche, la création et la suppression de workouts.
 */
public class WorkoutService {

    @Inject
    EntityManager em;

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    TrainingGeneratorService tgs;

    /**
     * Recherche un workout par identifiant.
     * @param id identifiant du workout
     * @return Optional<Workout>
     */
    public Optional<Workout> getWorkoutByID(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            return Optional.ofNullable(workout);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Liste les workouts d'un utilisateur, triés par date décroissante.
     * @param accountId identifiant du compte
     * @return liste des workouts
     */
    public List<Workout> findByAccountId(UUID accountId) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                "ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    @Transactional
    public List<Workout> getCurrentWeekWorkouts(UUID accountId) {
        OffsetDateTime now = OffsetDateTime.now();
        // On prend le LocalDate courant
        LocalDate today = now.toLocalDate();
        // Début de semaine = lundi 00:00
        LocalDateTime startOfWeekLdt = today
                .with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay();
        // Fin de semaine = dimanche 23:59:59
        LocalDateTime endOfWeekLdt = startOfWeekLdt.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        // Conversion en OffsetDateTime avec le même offset que "now"
        OffsetDateTime startOfWeek = startOfWeekLdt.atOffset(now.getOffset());
        OffsetDateTime endOfWeek   = endOfWeekLdt.atOffset(now.getOffset());

        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                "AND w.startTime >= :startOfWeek AND w.startTime <= :endOfWeek " +
                                "ORDER BY w.startTime ASC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("startOfWeek", startOfWeek)
                .setParameter("endOfWeek", endOfWeek)
                .getResultList();
    }

    public List<Workout> getWorkoutForWeek(UUID accountId, int weekNumber) {
        Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return List.of();
        } else {
            LocalDate planStartDate = tp.get().getStartDate();
            LocalDate startOfWeek = planStartDate.plusWeeks(weekNumber - 1).with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            OffsetDateTime startOfWeekOdt = startOfWeek.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime endOfWeekOdt = endOfWeek.atTime(23, 59, 59).atOffset(OffsetDateTime.now().getOffset());

            return em.createQuery(
                            "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                    "AND w.startTime >= :startOfWeek AND w.startTime <= :endOfWeek " +
                                    "ORDER BY w.startTime ASC",
                            Workout.class)
                    .setParameter("accountId", accountId)
                    .setParameter("startOfWeek", startOfWeekOdt)
                    .setParameter("endOfWeek", endOfWeekOdt)
                    .getResultList();
        }
    }

    /**
     * Liste les workouts d'un utilisateur pour un sport donné.
     * @param accountId identifiant du compte
     * @param sport sport ciblé
     * @return liste des workouts filtrés
     */
    public List<Workout> findByAccountIdAndSport(UUID accountId, Sport sport) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.sport = :sport " +
                                "ORDER BY w.startTime ASC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("sport", sport)
                .getResultList();
    }

    public List<Workout> getNextNWorkouts(UUID accountId) {
        Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);
        if(tp.isEmpty()) {
            return getAllWorkouts(accountId);
        } else {
            Integer nbWorkouts = trainingPlanService.getNbWorkoutsPerWeek(accountId);
            // return the next nbWorkouts for the user based on the current date
            return em.createQuery(
                            "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.startTime >= :startTime " +
                                    "ORDER BY w.startTime ASC",
                            Workout.class)
                    .setParameter("accountId", accountId)
                    .setParameter("startTime", OffsetDateTime.now())
                    .setMaxResults(nbWorkouts)
                    .getResultList();
        }
    }

    public List<Workout> getAllWorkouts(UUID accountId) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    public List<Workout> getWorkoutsBetweenDates(UUID accountId, OffsetDateTime start, OffsetDateTime end) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                "AND w.startTime >= :start AND w.endTime <= :end " +
                                "ORDER BY w.startTime ASC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Transactional
    /**
     * Crée un nouveau workout.
     * @param workout entité workout à persister
     * @return le workout créé
     */
    public Workout create(Workout workout) {

        em.persist(workout);
        return workout;
    }

    @Transactional
    /**
     * Supprime un workout par identifiant.
     * @param id identifiant du workout
     * @return true si supprimé, false sinon
     */
    public boolean delete(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            if (workout != null) {
                em.remove(workout);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<Workout> findClosestWorkout(WorkoutUploadDto workout, Account account) {
        Sport sport = Sport.valueOf(workout.getSport().toUpperCase());
        // Find workouts that are the same day, sport
        OffsetDateTime startOfDay = workout.getStart().toLocalDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = workout.getEnd().toLocalDate().atTime(23, 59, 59).atOffset(OffsetDateTime.now().getOffset());
        Optional<Workout> getWorkout = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.sport = :sport " +
                                "AND w.startTime >= :startOfDay AND w.endTime <= :endOfDay " +
                                "ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", account.getId())
                .setParameter("sport", sport)
                .setParameter("startOfDay", startOfDay)
                .setParameter("endOfDay", endOfDay)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();

        return getWorkout;
    }

    public void generateWorkout(TrainingPlan trainingPlan, LocalDate date) {
        List<Workout> workouts = tgs.generate(trainingPlan, date);

        for (Workout w : workouts) {
            em.persist(w);
        }
    }

    /**
     * Convertit un Workout en WorkoutFullDto
     * @param workout L'entité Workout à convertir
     * @param fcMax Fréquence cardiaque maximale de l'utilisateur (toujours définie)
     * @return WorkoutFullDto complet avec tous les détails
     */
    public WorkoutFullDto toWorkoutFullDto(Workout workout, int fcMax) {
        if (workout == null) {
            return null;
        }

        WorkoutFullDto dto = new WorkoutFullDto();

        // Informations de base
        dto.setId(workout.getId());
        dto.setSport(workout.getSport());
        dto.setType(workout.getWorkoutType());
        dto.setStatus(workout.getStatus());
        dto.setStart(workout.getStartTime());
        dto.setEnd(workout.getEndTime());
        dto.setDurationSec(workout.getDurationSec());
        dto.setDay(workout.getStartTime().getDayOfWeek());

        // Métriques de performance
        dto.setAvgHeartRate(workout.getAvgHeartRate());
        dto.setDistanceMeters(workout.getDistanceMeters() > 0 ? workout.getDistanceMeters() : null);
        dto.setCaloriesKcal(workout.getCaloriesKcal() > 0 ? workout.getCaloriesKcal() : null);

        // Conversion du plan d'entraînement avec FC Max
        dto.setPlan(convertWorkoutPlansToDto(workout.getPlans(), fcMax));

        // Champs non encore implémentés
        dto.setGrade(null);
        dto.setAiReview(null);
        dto.setPerformanceDetails(null);

        return dto;
    }

    /**
     * Convertit une liste de WorkoutPlan en WorkoutPlanDto
     * @param workoutPlans Liste des plans d'entraînement
     * @param fcMax Fréquence cardiaque maximale pour calculer les zones cibles
     * @return Liste des WorkoutPlanDto
     */
    private List<WorkoutPlanDto> convertWorkoutPlansToDto(List<WorkoutPlan> workoutPlans, int fcMax) {
        if (workoutPlans == null || workoutPlans.isEmpty()) {
            return new ArrayList<>();
        }

        return workoutPlans.stream()
                .map(plan -> convertWorkoutPlanToDto(plan, fcMax))
                .toList();
    }

    /**
     * Convertit un WorkoutPlan en WorkoutPlanDto
     * @param workoutPlan Plan d'entraînement à convertir
     * @param fcMax Fréquence cardiaque maximale
     * @return WorkoutPlanDto avec détails
     */
    private WorkoutPlanDto convertWorkoutPlanToDto(WorkoutPlan workoutPlan, int fcMax) {
        WorkoutPlanDto dto = new WorkoutPlanDto();
        dto.setBlocId(workoutPlan.getBlocId());
        dto.setRepetitionCount(workoutPlan.getRepetitionCount());

        // Conversion des détails avec calcul automatique des zones FC
        if (workoutPlan.getDetails() != null && !workoutPlan.getDetails().isEmpty()) {
            List<WorkoutPlanDetailsDto> detailsDto = workoutPlan.getDetails().stream()
                    .map(detail -> new WorkoutPlanDetailsDto(detail, fcMax))
                    .toList();
            dto.setDetails(detailsDto);
        } else {
            dto.setDetails(new ArrayList<>());
        }

        return dto;
    }

    @Transactional
    public Workout createWorkoutOutOfTP(Account account, WorkoutUploadDto workout) {
        Workout newWorkout = new Workout();
        newWorkout.setAccount(account);
        newWorkout.setSport(Sport.valueOf(workout.getSport().toUpperCase()));
        newWorkout.setStartTime(workout.getStart());
        newWorkout.setEndTime(workout.getEnd());
        newWorkout.setStatus(WorkoutStatus.COMPLETED);
        newWorkout.setDistanceMeters(workout.getDistance());
        newWorkout.setCaloriesKcal(workout.getCaloriesKcal());
        newWorkout.setAvgHeartRate(workout.getAvgBPM().intValue());
        newWorkout.setMaxHeartRate(workout.getMaxBPM().intValue());
        newWorkout.setSource(workout.getSource());
        newWorkout.setDurationSec((int) (newWorkout.getEndTime().toEpochSecond() - newWorkout.getStartTime().toEpochSecond()));
        newWorkout.setAvgSpeed(workout.getAvgSpeed());

        newWorkout.setActualBPMDataPoints(workout.getBpmDataPoints());
        newWorkout.setActualSpeedDataPoints(workout.getSpeedDataPoints());

        em.persist(newWorkout);

        return newWorkout;
    }

    @Transactional
    public Workout mergeWorkoutWithExisting(Workout existingWorkout, WorkoutUploadDto workout) {
        existingWorkout.setSport(Sport.valueOf(workout.getSport().toUpperCase()));
        existingWorkout.setStartTime(workout.getStart());
        existingWorkout.setEndTime(workout.getEnd());
        existingWorkout.setStatus(WorkoutStatus.COMPLETED);
        existingWorkout.setDistanceMeters(workout.getDistance());
        existingWorkout.setCaloriesKcal(workout.getCaloriesKcal());
        existingWorkout.setAvgHeartRate(workout.getAvgBPM().intValue());
        existingWorkout.setMaxHeartRate(workout.getMaxBPM().intValue());
        existingWorkout.setSource(workout.getSource());
        existingWorkout.setDurationSec((int) (existingWorkout.getEndTime().toEpochSecond() - existingWorkout.getStartTime().toEpochSecond()));
        existingWorkout.setAvgSpeed(workout.getAvgSpeed());

        existingWorkout.setActualBPMDataPoints(workout.getBpmDataPoints());
        existingWorkout.setActualSpeedDataPoints(workout.getSpeedDataPoints());

        em.merge(existingWorkout);

        return existingWorkout;
    }
}