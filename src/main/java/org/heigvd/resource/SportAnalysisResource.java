package org.heigvd.resource;

import org.heigvd.service.AIService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ai")
@Produces(MediaType.TEXT_PLAIN)
public class SportAnalysisResource {

    @Inject
    AIService analysisService;

    @GET
    @Path("/ping")
    public String testConnection() {
        return analysisService.pingGroq();
    }

    @POST
    @Path("/analyze")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyzeActivity(String activityJson) {
        // Validation basique
        if (activityJson == null || activityJson.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Données JSON manquantes")
                    .build();
        }

        // Analyser l'activité
        String result = analysisService.analyzeSportActivity(activityJson);

        // Retourner la réponse
        return Response.ok(result).build();
    }
}