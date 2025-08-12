package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.heigvd.dto.AccountDto;
import org.jboss.resteasy.reactive.common.util.RestMediaType;
import org.heigvd.dto.LoginRequestDto;
import org.heigvd.dto.LoginResponseDto;
import org.heigvd.entity.Account;
import org.heigvd.service.AccountService;
import org.heigvd.service.JwtService;

import java.util.Optional;
import java.util.UUID;

@Path("/auth")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AccountService accountService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    @Transactional
    public Response login(@Valid LoginRequestDto dto) {

        Optional<Account> userOpt = accountService.findByEmail(dto.getEmail());

        if (userOpt.isEmpty() || !accountService.checkPassword(dto.getPassword(), userOpt.get().getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Identifiants invalides").build();
        }


        String token = jwtService.generateToken(userOpt.get().getId());

        return Response.ok(new LoginResponseDto(token)).build();

    }

    @GET
    @Path("/me")
    @Authenticated
    public Response getMe(SecurityContext context) {

        return Response.ok("{\"status\": \"ok\", \"message\": \"Endpoint works!\"}").build();
    }

/*
    @PUT
    @Path("/me")
    @Authenticated
    public Response updateMe(SecurityContext context, @Valid UserExtendedDto userExtendedDto) {

        String id = context.getUserPrincipal().getName();
        Long userId = Long.valueOf(id);

        User user = userService.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Mise à jour des champs de base
        user.setFirstName(userExtendedDto.getFirstName());
        user.setLastName(userExtendedDto.getLastName());
        user.setEmail(userExtendedDto.getEmail());
        user.setUsername(userExtendedDto.getUsername());

        // Mise à jour des champs d'adresse
        user.setAddress(userExtendedDto.getAddress());
        user.setCity(userExtendedDto.getCity());
        user.setPostalCode(userExtendedDto.getPostalCode());
        user.setCountry(userExtendedDto.getCountry());

        // Mise à jour des champs de contact et profil
        user.setPhoneNumber(userExtendedDto.getPhoneNumber());
        user.setProfilePicture(userExtendedDto.getProfilePicture());

        userService.update(user);

        return Response.ok(new UserExtendedDto(user)).build();
    }
 */
}
