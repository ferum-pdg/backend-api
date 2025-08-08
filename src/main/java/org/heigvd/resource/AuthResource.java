package org.heigvd.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.heigvd.dto.LoginRequest;
import org.heigvd.dto.LoginResponse;
import org.heigvd.dto.RegisterRequest;
import org.heigvd.service.AuthService;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll  // ← Ajoutez ceci au niveau de la classe
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Response.ok(response).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}