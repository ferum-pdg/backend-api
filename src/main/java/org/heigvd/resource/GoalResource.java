package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.heigvd.entity.Sport;
import org.heigvd.service.GoalService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

@Path("/goals")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
public class GoalResource {

    @Inject
    GoalService goalService;

    @GET
    public Response getAllGoals() {
        return Response.ok(goalService.getAllGoals()).build();
    }

    @GET
    @Path("/{sport}")
    public Response getGoalsBySport(@PathParam("sport") String sport) {
        // First we need to check if the sport is valid
        Sport sportEnum;
        try {
            sportEnum = Sport.valueOf(sport.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Sport invalide. Les sports valides sont: RUNNING, CYCLING, SWIMMING.\"}")
                    .build();
        }
        return Response.ok(goalService.getGoalsBySport(sportEnum)).build();
    }
}
