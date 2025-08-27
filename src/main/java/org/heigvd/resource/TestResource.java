package org.heigvd.resource;

import io.quarkus.security.Authenticated;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.heigvd.dto.workout_dto.*;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.workout_dto.data_point_dto.WorkoutPerfDetailsDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.IntensityZone;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.service.AccountService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/test")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    public Response testWorkoutDto() {
        List<WorkoutPlanDetailsDto> warmup = new ArrayList<>(
                List.of(
                        new WorkoutPlanDetailsDto(1, 3600, 100, 120, IntensityZone.RECOVERY)
                )
        );

        List<WorkoutPlanDetailsDto> main = new ArrayList<>(
                List.of(
                        new WorkoutPlanDetailsDto(2, 180, 160, 168, IntensityZone.TEMPO),
                        new WorkoutPlanDetailsDto(3, 90, 121, 140, IntensityZone.ENDURANCE)
                )
        );

        List<WorkoutPlanDetailsDto> cooldown = new ArrayList<>(
                List.of(
                        new WorkoutPlanDetailsDto(4, 1800, 100, 120, IntensityZone.RECOVERY)
                )
        );

        List<WorkoutPlanDto> plan = new ArrayList<>(
                List.of(
                        new WorkoutPlanDto(1, 1, warmup),
                        new WorkoutPlanDto(2, 4, main),
                        new WorkoutPlanDto(3, 1, cooldown)
                )
        );

        WorkoutFullDto workout = new WorkoutFullDto(
                UUID.randomUUID(),
                Sport.RUNNING,
                WorkoutType.INTERVAL,
                WorkoutStatus.PLANNED,
                DayOfWeek.TUESDAY,
                3600 + (4 * (180 + 90)) + 1800,
                plan
        );

        return Response.ok(workout).build();
    }

    @GET
    @Path("/full")
    public Response testWorkoutFullDto() {
        List<WorkoutPlanDetailsDto> warmup = new ArrayList<>(
                List.of(
                        new WorkoutPlanDetailsDto(1, 3600, 100, 120, IntensityZone.RECOVERY)
                )
        );

        List<WorkoutPlanDetailsDto> main = new ArrayList<>(
                List.of(
                        new WorkoutPlanDetailsDto(2, 180, 160, 168, IntensityZone.TEMPO),
                        new WorkoutPlanDetailsDto(3, 90, 121, 140, IntensityZone.ENDURANCE)
                )
        );

        List<WorkoutPlanDetailsDto> cooldown = new ArrayList<>(
                List.of(
                        new WorkoutPlanDetailsDto(4, 1800, 100, 120, IntensityZone.RECOVERY)
                )
        );

        List<WorkoutPlanDto> plan = new ArrayList<>(
                List.of(
                        new WorkoutPlanDto(1, 1, warmup),
                        new WorkoutPlanDto(2, 4, main),
                        new WorkoutPlanDto(3, 1, cooldown)
                )
        );

        List<WorkoutPerfDetailsDto> workoutPerfDetails = new ArrayList<>(
                List.of(
                    new WorkoutPerfDetailsDto(1, 100, 120, 123),
                    new WorkoutPerfDetailsDto(2, 160, 168, 165),
                    new WorkoutPerfDetailsDto(3, 121, 140, 130)
                )
        );

        WorkoutFullDto workout = new WorkoutFullDto(
                UUID.randomUUID(),
                Sport.RUNNING,
                WorkoutType.INTERVAL,
                WorkoutStatus.COMPLETED,
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now(),
                DayOfWeek.TUESDAY,
                3600 + (4 * (180 + 90)) + 1800,
                134,
                10000.0,
                600.0,
                8.5,
                "Great workout, well done!",
                plan,
                workoutPerfDetails
        );

        return Response.ok(workout).build();
    }
}