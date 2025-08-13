package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.WorkoutDto;
import org.heigvd.entity.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorkoutService {

    @Inject
    EntityManager em;

    // READ OPERATIONS
    public List<WorkoutDto> getAllWorkouts() {
        List<Workout> workouts = em.createQuery("SELECT w FROM Workout w ORDER BY w.startTime DESC", Workout.class)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Optional<WorkoutDto> findById(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            return Optional.ofNullable(workout).map(this::convertToDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Workout> findEntityById(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            return Optional.ofNullable(workout);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<WorkoutDto> findByAccountId(UUID accountId) {
        List<Workout> workouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<WorkoutDto> findByAccountIdPaginated(UUID accountId, int page, int size) {
        List<Workout> workouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<WorkoutDto> findByAccountIdAndSport(UUID accountId, Sport sport) {
        List<Workout> workouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.sport = :sport ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("sport", sport)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<WorkoutDto> findByDateRange(UUID accountId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Workout> workouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.startTime >= :startDate AND w.startTime <= :endDate ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<WorkoutDto> findRecentWorkouts(UUID accountId, int days) {
        OffsetDateTime since = OffsetDateTime.now().minusDays(days);
        List<Workout> workouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.startTime >= :since ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("since", since)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<WorkoutDto> findByTrainingPlan(UUID accountId, UUID trainingPlanId) {
        List<Workout> workouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.trainingPlan.id = :trainingPlanId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("trainingPlanId", trainingPlanId)
                .getResultList();
        return workouts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // CREATE OPERATION
    @Transactional
    public WorkoutDto create(WorkoutDto workoutDto) {
        // Récupérer les entités nécessaires
        Account account = em.find(Account.class, workoutDto.getAccountId());
        if (account == null) {
            throw new RuntimeException("Account not found with ID: " + workoutDto.getAccountId());
        }

        TrainingPlan trainingPlan = null;
        if (workoutDto.getTrainingPlanId() != null) {
            trainingPlan = em.find(TrainingPlan.class, workoutDto.getTrainingPlanId());
            if (trainingPlan == null) {
                throw new RuntimeException("TrainingPlan not found with ID: " + workoutDto.getTrainingPlanId());
            }
        }

        // Convertir et persister
        Workout workout = convertToEntity(workoutDto, account, trainingPlan);

        // Générer un ID si pas déjà défini
        if (workout.getId() == null) {
            workout.setId(UUID.randomUUID());
        }

        em.persist(workout);
        return convertToDto(workout);
    }

    // UPDATE OPERATION
    @Transactional
    public Optional<WorkoutDto> update(UUID id, WorkoutDto workoutDto) {
        try {
            Workout existingWorkout = em.find(Workout.class, id);
            if (existingWorkout == null) {
                return Optional.empty();
            }

            // Mise à jour des champs
            if (workoutDto.getSport() != null) {
                existingWorkout.setSport(Sport.valueOf(workoutDto.getSport().toUpperCase()));
            }
            if (workoutDto.getStartTime() != null) {
                existingWorkout.setStartTime(workoutDto.getStartTime());
            }
            if (workoutDto.getEndTime() != null) {
                existingWorkout.setEndTime(workoutDto.getEndTime());
            }
            if (workoutDto.getDurationSec() != null) {
                existingWorkout.setDurationSec(workoutDto.getDurationSec());
            }
            if (workoutDto.getDistanceMeters() != null) {
                existingWorkout.setDistanceMeters(workoutDto.getDistanceMeters());
            }
            if (workoutDto.getCaloriesKcal() != null) {
                existingWorkout.setCaloriesKcal(workoutDto.getCaloriesKcal());
            }
            if (workoutDto.getAvgHeartRate() != null) {
                existingWorkout.setAvgHeartRate(workoutDto.getAvgHeartRate());
            }
            if (workoutDto.getMaxHeartRate() != null) {
                existingWorkout.setMaxHeartRate(workoutDto.getMaxHeartRate());
            }
            if (workoutDto.getAverageSpeed() != null) {
                existingWorkout.setAverageSpeed(workoutDto.getAverageSpeed());
            }
            if (workoutDto.getSource() != null) {
                existingWorkout.setSource(workoutDto.getSource());
            }
            if (workoutDto.getStatus() != null) {
                existingWorkout.setStatus(TrainingStatus.valueOf(workoutDto.getStatus().toUpperCase()));
            }

            // Mise à jour du training plan si nécessaire
            if (workoutDto.getTrainingPlanId() != null) {
                TrainingPlan trainingPlan = em.find(TrainingPlan.class, workoutDto.getTrainingPlanId());
                if (trainingPlan == null) {
                    throw new RuntimeException("TrainingPlan not found with ID: " + workoutDto.getTrainingPlanId());
                }
                existingWorkout.setTrainingPlan(trainingPlan);
            }

            em.merge(existingWorkout);
            return Optional.of(convertToDto(existingWorkout));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // DELETE OPERATIONS
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

    @Transactional
    public void deleteByAccountId(UUID accountId) {
        em.createQuery("DELETE FROM Workout w WHERE w.account.id = :accountId")
                .setParameter("accountId", accountId)
                .executeUpdate();
    }

    // CONVERSION METHODS
    public WorkoutDto convertToDto(Workout workout) {
        return new WorkoutDto(
                workout.getId(),
                workout.getAccount().getId(),
                workout.getTrainingPlan() != null ? workout.getTrainingPlan().getId() : null,
                workout.getSport().name(),
                workout.getStartTime(),
                workout.getEndTime(),
                workout.getDurationSec(),
                workout.getDistanceMeters(),
                workout.getCaloriesKcal(),
                workout.getAvgHeartRate(),
                workout.getMaxHeartRate(),
                workout.getAverageSpeed(),
                workout.getSource(),
                workout.getStatus().name()
        );
    }

    public Workout convertToEntity(WorkoutDto dto, Account account, TrainingPlan trainingPlan) {
        return new Workout(
                account,
                trainingPlan,
                Sport.valueOf(dto.getSport().toUpperCase()),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getDurationSec() != null ? dto.getDurationSec() : 0,
                dto.getDistanceMeters() != null ? dto.getDistanceMeters() : 0.0,
                dto.getCaloriesKcal() != null ? dto.getCaloriesKcal() : 0.0,
                dto.getAvgHeartRate() != null ? dto.getAvgHeartRate() : 0,
                dto.getMaxHeartRate() != null ? dto.getMaxHeartRate() : 0,
                dto.getAverageSpeed(),
                dto.getSource(),
                TrainingStatus.valueOf(dto.getStatus().toUpperCase())
        );
    }

}