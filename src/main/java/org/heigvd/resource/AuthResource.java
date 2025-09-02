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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.AccountDto;
import org.heigvd.dto.CreateAccountDto;
import org.jboss.resteasy.reactive.common.util.RestMediaType;
import org.heigvd.dto.LoginRequestDto;
import org.heigvd.dto.LoginResponseDto;
import org.heigvd.entity.Account;
import org.heigvd.service.AccountService;
import org.heigvd.service.JwtService;
import java.util.Optional;

/**
 * REST resource for authentication and user profile management.
 *
 * Provides login operations and access/update of user profile
 * for authenticated users.
 */
@Path("/auth")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication and profile management")
public class AuthResource {

    @Inject
    AccountService accountService;

    @Inject
    JwtService jwtService;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param dto Login credentials (email and password)
     */
    @POST
    @Path("/login")
    @Transactional
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password and returns a JWT token."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Invalid credentials"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(description = "Login credentials", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequestDto.class)))
    public Response login(
            @Parameter(description = "Login credentials (email and password)", required = true)
            @Valid LoginRequestDto dto) {
        try {
            Optional<Account> userOpt = accountService.findByEmail(dto.getEmail());

            if (userOpt.isEmpty() || !accountService.checkPassword(dto.getPassword(), userOpt.get().getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Invalid credentials\"}")
                        .build();
            }

            String token = jwtService.generateToken(userOpt.get().getId());

            return Response.ok(new LoginResponseDto(token)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Returns the account information of the authenticated user.
     *
     * @param context Security context containing the JWT identity
     */
    @GET
    @Path("/me")
    @Authenticated
    @Operation(
            summary = "Get user profile",
            description = "Returns the account information associated with the JWT token."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Profile found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDto.class))),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "404", description = "User not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getMe(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            SecurityContext context) {
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

    /**
     * Updates the profile of the authenticated user.
     *
     * @param context Security context containing the JWT identity
     * @param accountDto New account information
     */
    @PUT
    @Path("/me")
    @Authenticated
    @Transactional
    @Operation(
            summary = "Update user profile",
            description = "Updates the profile information of the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid data"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "404", description = "User not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(description = "New account information", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AccountDto.class)))
    public Response updateMe(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            SecurityContext context,
            @Parameter(description = "New user profile information", required = true)
            @Valid AccountDto accountDto) {
        try {
            String userId = context.getUserPrincipal().getName();

            Optional<Account> accountOpt = accountService.findById(userId);

            if (accountOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }

            Account account = accountOpt.get();

            // Don't update email and id
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

    /**
     * Creates a new user account.
     *
     * @param dto Account information for the new user
     */
    @POST
    @Path("/register")
    @Transactional
    @Operation(
            summary = "Create user account",
            description = "Creates a new user account with the provided information."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid data or email already exists"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(description = "Account information", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CreateAccountDto.class)))
    public Response createAccount(
            @Parameter(description = "Account information for new user", required = true)
            @Valid CreateAccountDto dto) {
        try {
            // Check if email already exists
            Optional<Account> existingUser = accountService.findByEmail(dto.getEmail());
            if (existingUser.isPresent()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Email already exists\"}")
                        .build();
            }

            // Create new account
            Account newAccount = new Account();
            newAccount.setEmail(dto.getEmail());
            newAccount.setPassword(dto.getPassword()); // The create method will hash it automatically
            newAccount.setFirstName(dto.getFirstName());
            newAccount.setLastName(dto.getLastName());
            newAccount.setPhoneNumber(dto.getPhoneNumber());
            newAccount.setBirthDate(dto.getBirthDate());
            newAccount.setWeight(dto.getWeight());
            newAccount.setHeight(dto.getHeight());
            newAccount.setFCMax(dto.getFcMax());

            // Save the account (password will be hashed in the service)
            Account savedAccount = accountService.create(newAccount);

            // Generate JWT token for the new user
            String token = jwtService.generateToken(savedAccount.getId());

            // Return the JWT token
            return Response.status(Response.Status.CREATED)
                    .entity(new LoginResponseDto(token))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}