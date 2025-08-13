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
