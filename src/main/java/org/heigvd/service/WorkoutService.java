package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.workout_dto.WorkoutFullDto;
import org.heigvd.dto.workout_dto.WorkoutPlanDetailsDto;
import org.heigvd.dto.workout_dto.WorkoutPlanDto;
import org.heigvd.dto.workout_dto.WorkoutUploadDto;
import org.heigvd.dto.workout_dto.data_point_dto.WorkoutPerfDetailsDto;
import org.heigvd.entity.*;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.data_point.BPMDataPoint;
import org.heigvd.entity.workout.details.WorkoutPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for workout management.
 *
 * Provides search, creation and deletion operations for workouts.
 */
@ApplicationScoped
public class WorkoutService {

    @Inject
    EntityManager em;

    @Inject
    TrainingGeneratorService tgs;

    /**
     * Searches for a workout by identifier.
     * @param id workout identifier
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
     * Lists workouts for a user for a given sport.
     * @param accountId account identifier
     * @param sport target sport
     * @return list of filtered workouts
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

    /**
     * Retrieves all workouts for a user.
     * @param accountId account identifier
     * @return list of all workouts ordered by start time descending
     */
    public List<Workout> getAllWorkouts(UUID accountId) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    /**
     * Retrieves workouts between two dates for a user.
     * @param accountId account identifier
     * @param start start date and time
     * @param end end date and time
     * @return list of workouts within the date range
     */
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

    /**
     * Retrieves all generated workouts for a user and training plan.
     * @param accountId account identifier
     * @param trainingPlanId training plan identifier
     * @return list of generated workouts
     */
    public List<Workout> getAllGeneratedWorkouts(UUID accountId, UUID trainingPlanId) {
        return em.createQuery(
                        "SELECT w FROM Workout w JOIN w.plans p WHERE w.account.id = :accountId " +
                                "AND w.trainingPlan.id = :trainingPlanId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("trainingPlanId", trainingPlanId)
                .getResultList();
    }

    /**
     * Retrieves all completed planned workouts for a user and training plan.
     * @param accountId account identifier
     * @param trainingPlanId training plan identifier
     * @return list of completed planned workouts
     */
    public List<Workout> getAllDonePlannedWorkouts(UUID accountId, UUID trainingPlanId) {
        return em.createQuery(
                        "SELECT w FROM Workout w JOIN w.plans p WHERE w.account.id = :accountId " +
                                "AND w.trainingPlan.id = :trainingPlanId " +
                                "AND w.status = :status ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("trainingPlanId", trainingPlanId)
                .setParameter("status", WorkoutStatus.COMPLETED)
                .getResultList();
    }

    /**
     * Creates a new workout.
     * @param workout workout entity to persist
     * @return the created workout
     */
    @Transactional
    public Workout create(Workout workout) {
        em.persist(workout);
        return workout;
    }

    /**
     * Deletes a workout by identifier.
     * @param id workout identifier
     * @return true if deleted, false otherwise
     */
    @Transactional
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

    /**
     * Finds the closest workout matching the uploaded workout data.
     * @param workout uploaded workout data
     * @param account user account
     * @return Optional containing the closest matching workout if found
     */
    public Optional<Workout> findClosestWorkout(WorkoutUploadDto workout, Account account) {
        Sport sport = Sport.valueOf(workout.getSport().toUpperCase());

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

    /**
     * Generates workouts for a training plan on a specific date.
     * @param trainingPlan the training plan
     * @param date target date for workout generation
     */
    public void generateWorkout(TrainingPlan trainingPlan, LocalDate date) {
        List<Workout> workouts = tgs.generate(trainingPlan, date);

        for (Workout w : workouts) {
            em.persist(w);
        }
    }

    /**
     * Gets the date of the last generated workout for an account.
     * @param account user account
     * @return LocalDate of the last generated workout or null if none found
     */
    public LocalDate getLastGeneratedWorkoutDate(Account account) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId" +
                                " AND w.plans IS NOT EMPTY ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", account.getId())
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(w -> w.getStartTime().toLocalDate())
                .orElse(null);
    }

    /**
     * Converts a Workout to WorkoutFullDto.
     * @param workout The Workout entity to convert
     * @param fcMax User's maximum heart rate (always defined)
     * @return Complete WorkoutFullDto with all details
     */
    public WorkoutFullDto toWorkoutFullDto(Workout workout, int fcMax) {
        if (workout == null) {
            return null;
        }

        WorkoutFullDto dto = new WorkoutFullDto();

        dto.setId(workout.getId());
        dto.setSport(workout.getSport());
        dto.setType(workout.getWorkoutType());
        dto.setStatus(workout.getStatus());
        dto.setStart(workout.getStartTime());
        dto.setEnd(workout.getEndTime());
        dto.setDurationSec(workout.getDurationSec());
        dto.setDay(workout.getStartTime().getDayOfWeek());
        dto.setGrade(workout.getGrade());
        dto.setAiReview(workout.getAiAnalysis());

        dto.setAvgHeartRate(workout.getAvgHeartRate());
        dto.setDistanceMeters(workout.getDistanceMeters() > 0 ? workout.getDistanceMeters() : null);
        dto.setCaloriesKcal(workout.getCaloriesKcal() > 0 ? workout.getCaloriesKcal() : null);
        dto.setPerformanceDetails(buildWorkoutPerfDetailsToDto(workout));

        dto.setPlan(convertWorkoutPlansToDto(workout.getPlans(), fcMax));

        return dto;
    }

    private List<WorkoutPerfDetailsDto> buildWorkoutPerfDetailsToDto(Workout workout) {
        List<BPMDataPoint> bpmDataPoints = workout.getActualBPMDataPoints();
        List<WorkoutPlan> plans = workout.getPlans();
        List<WorkoutPerfDetailsDto> perfDetails = new ArrayList<>();

        if (bpmDataPoints == null || bpmDataPoints.isEmpty() || plans == null || plans.isEmpty()) {
            return null;
        }

        int durationSec = 0;

        for(WorkoutPlan plan : plans) {
            if (plan.getDetails() == null || plan.getDetails().isEmpty()) {
                continue;
            }

            for (var detail : plan.getDetails()) {
                LocalDateTime detailStart = workout.getStartTime().toLocalDateTime().plusSeconds(durationSec);
                durationSec += detail.getDurationSec();
                LocalDateTime detailEnd = workout.getStartTime().toLocalDateTime().plusSeconds(durationSec);

                List<BPMDataPoint> relevantBPMs = bpmDataPoints.stream()
                        .filter(bpm -> {
                            LocalDateTime bpmTime = bpm.getTimestamp().toLocalDateTime();
                            return !bpmTime.isBefore(detailStart) && !bpmTime.isAfter(detailEnd);
                        })
                        .toList();

                if (!relevantBPMs.isEmpty()) {
                    // Calculate average of relevant BPMs
                    double avgBPM = relevantBPMs.stream()
                            .mapToDouble(BPMDataPoint::getBpm)
                            .average()
                            .orElse(0.0);

                    int fcMax = workout.getAccount().getFCMax();

                    WorkoutPerfDetailsDto perfDetail = new WorkoutPerfDetailsDto();
                    perfDetail.setBlocId(plan.getBlocId());
                    perfDetail.setPlannedBPMMin(detail.getIntensityZone().getMinHr() * fcMax);
                    perfDetail.setPlannedBPMMax(detail.getIntensityZone().getMaxHr() * fcMax);
                    perfDetail.setActualBPMMean(Math.round(avgBPM));
                    perfDetails.add(perfDetail);
                } else {
                    WorkoutPerfDetailsDto perfDetail = new WorkoutPerfDetailsDto();
                    perfDetail.setBlocId(plan.getBlocId());
                    perfDetail.setPlannedBPMMin(detail.getIntensityZone().getMinHr() * workout.getAccount().getFCMax());
                    perfDetail.setPlannedBPMMax(detail.getIntensityZone().getMaxHr() * workout.getAccount().getFCMax());
                    perfDetails.add(perfDetail);
                }
            }
        }

        return perfDetails;
    }

    /**
     * Converts a list of WorkoutPlan to WorkoutPlanDto.
     * @param workoutPlans List of training plans
     * @param fcMax Maximum heart rate to calculate target zones
     * @return List of WorkoutPlanDto
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
     * Converts a WorkoutPlan to WorkoutPlanDto.
     * @param workoutPlan Training plan to convert
     * @param fcMax Maximum heart rate
     * @return WorkoutPlanDto with details
     */
    private WorkoutPlanDto convertWorkoutPlanToDto(WorkoutPlan workoutPlan, int fcMax) {
        WorkoutPlanDto dto = new WorkoutPlanDto();
        dto.setBlocId(workoutPlan.getBlocId());
        dto.setRepetitionCount(workoutPlan.getRepetitionCount());

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

    /**
     * Creates a workout outside of a training plan from uploaded data.
     * @param account user account
     * @param workout uploaded workout data
     * @return created workout
     */
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

    /**
     * Merges uploaded workout data with an existing workout.
     * @param existingWorkout the existing workout to update
     * @param workout uploaded workout data
     * @return updated workout
     */
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