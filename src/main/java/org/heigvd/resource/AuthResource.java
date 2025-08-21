package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.AccountDto;
import org.jboss.resteasy.reactive.common.util.RestMediaType;
import org.heigvd.dto.LoginRequestDto;
import org.heigvd.dto.LoginResponseDto;
import org.heigvd.entity.Account;
import org.heigvd.service.AccountService;
import org.heigvd.service.JwtService;
import java.util.Optional;

/**
 * Ressource REST d'authentification et de gestion du profil utilisateur.
 *
 * Fournit les opérations de connexion et d'accès/mise à jour du profil
 * pour l'utilisateur authentifié.
 */
@Path("/auth")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Authentification et profil utilisateur")
public class AuthResource {

    @Inject
    AccountService accountService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    @Transactional
    /**
     * Authentifie un utilisateur et retourne un jeton JWT.
     *
     * @param dto Données de connexion (email et mot de passe)
     * @return 200 avec {@link LoginResponseDto} si succès, sinon code d'erreur approprié
     */
    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur avec email et mot de passe et retourne un jeton JWT."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Authentification réussie",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Identifiants invalides"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @RequestBody(description = "Identifiants de connexion", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequestDto.class)))
    public Response login(@Valid LoginRequestDto dto) {
        try {
            Optional<Account> userOpt = accountService.findByEmail(dto.getEmail());

            if (userOpt.isEmpty() || !accountService.checkPassword(dto.getPassword(), userOpt.get().getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Identifiants invalides\"}")
                        .build();
            }

            String token = jwtService.generateToken(userOpt.get().getId());

            return Response.ok(new LoginResponseDto(token)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    @GET
    @Path("/me")
    @Authenticated
    /**
     * Retourne les informations du compte de l'utilisateur authentifié.
     *
     * @param context Contexte de sécurité contenant l'identité JWT
     * @return 200 avec {@link AccountDto} si trouvé, 404 sinon
     */
    @Operation(
            summary = "Profil utilisateur",
            description = "Retourne les informations du compte associé au jeton JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Profil trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDto.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getMe(SecurityContext context) {
        try {
            String userId = context.getUserPrincipal().getName();

            Optional<Account> accountOpt = accountService.findById(userId);

            if (accountOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }

            Account account = accountOpt.get();

            AccountDto accountDto = new AccountDto(
                    account.getId(),
                    account.getEmail(),
                    account.getFirstName(),
                    account.getLastName(),
                    account.getPhoneNumber(),
                    account.getBirthDate(),
                    account.getWeight(),
                    account.getHeight(),
                    account.getFCMax()
            );

            return Response.ok(accountDto).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    @PUT
    @Path("/me")
    @Authenticated
    @Transactional
    /**
     * Met à jour le profil de l'utilisateur authentifié.
     *
     * @param context Contexte de sécurité contenant l'identité JWT
     * @param accountDto Nouvelles informations du compte
     * @return 200 avec le profil mis à jour, 404 si le compte est introuvable
     */
    @Operation(
            summary = "Mise à jour du profil",
            description = "Met à jour les informations du profil de l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Profil mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDto.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @RequestBody(description = "Nouvelles informations du compte", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AccountDto.class)))
    public Response updateMe(SecurityContext context, @Valid AccountDto accountDto) {
        try {
            String userId = context.getUserPrincipal().getName();

            Optional<Account> accountOpt = accountService.findById(userId);

            if (accountOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }

            Account account = accountOpt.get();

            //Pas email et id
            account.setFirstName(accountDto.getFirstName());
            account.setLastName(accountDto.getLastName());
            account.setPhoneNumber(accountDto.getPhoneNumber());
            account.setBirthDate(accountDto.getBirthDate());
            account.setWeight(accountDto.getWeight());
            account.setHeight(accountDto.getHeight());
            account.setFCMax(accountDto.getFcMax());

            accountService.update(account);

            AccountDto updatedAccountDto = new AccountDto(
                    account.getId(),
                    account.getEmail(),
                    account.getFirstName(),
                    account.getLastName(),
                    account.getPhoneNumber(),
                    account.getBirthDate(),
                    account.getWeight(),
                    account.getHeight(),
                    account.getFCMax()
            );

            return Response.ok(updatedAccountDto).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
