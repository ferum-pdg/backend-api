package org.heigvd.resource;

import org.heigvd.entity.Account;
import org.heigvd.service.AccountService;
import org.heigvd.dto.AccountDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.util.List;
import java.util.UUID;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    /**
     * GET /accounts - Récupère tous les comptes
     */
    @GET
    @Produces(RestMediaType.APPLICATION_JSON)
    public Response getAllAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            List<AccountDto> accountDtos = accounts.stream()
                    .map(AccountDto::new)
                    .toList();

            return Response.ok(accountDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération des comptes: " + e.getMessage())
                    .build();
        }
    }

    /**
     * GET /accounts/{id} - Récupère un compte par ID
     */
    @GET
    @Path("/id/{id}")
    public Response getAccountById(@PathParam("id") UUID id) {
        try {
            Account account = accountService.getAccountById(id);
            AccountDto accountDto = new AccountDto(account);

            return Response.ok(accountDto).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération du compte: " + e.getMessage())
                    .build();
        }
    }

    /**
     * GET /accounts/email/{email} - Récupère un compte par email
     */
    @GET
    @Path("/email/{email}")
    public Response getAccountByEmail(@PathParam("email") String email) {
        try {
            Account account = accountService.getAccountByEmail(email);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Compte non trouvé")
                        .build();
            }
            AccountDto accountDto = new AccountDto(account);

            return Response.ok(accountDto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération du compte: " + e.getMessage())
                    .build();
        }
    }

    /**
     * GET /accounts/search?name={searchTerm} - Recherche par nom/prénom
     */
    @GET
    @Path("/search")
    public Response searchAccountsByName(@QueryParam("name") String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le terme de recherche est requis")
                    .build();
        }

        try {
            List<Account> accounts = accountService.searchAccountsByName(searchTerm.trim());
            List<AccountDto> accountDtos = accounts.stream()
                    .map(AccountDto::new)
                    .toList();

            return Response.ok(accountDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la recherche: " + e.getMessage())
                    .build();
        }
    }

    /**
     * POST /accounts - Crée un nouveau compte
     */
    @POST
    public Response createAccount(Account account) {
        try {
            if (account.getEmail() == null || account.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("L'email est requis")
                        .build();
            }

            Account createdAccount = accountService.createAccount(account);
            AccountDto accountDto = new AccountDto(createdAccount);

            return Response.status(Response.Status.CREATED)
                    .entity(accountDto)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la création du compte: " + e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /accounts/{id} - Met à jour un compte complet
     */
    @PUT
    @Path("/{id}")
    public Response updateAccount(@PathParam("id") UUID id, Account updatedAccount) {
        try {
            Account account = accountService.updateAccount(id, updatedAccount);
            AccountDto accountDto = new AccountDto(account);

            return Response.ok(accountDto).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la mise à jour du compte: " + e.getMessage())
                    .build();
        }
    }

    /**
     * PATCH /accounts/{id} - Met à jour partiellement un compte
     */
    @PATCH
    @Path("/{id}")
    public Response patchAccount(@PathParam("id") UUID id, Account partialUpdate) {
        try {
            Account account = accountService.patchAccount(id, partialUpdate);
            AccountDto accountDto = new AccountDto(account);

            return Response.ok(accountDto).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la mise à jour du compte: " + e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /accounts/{id} - Supprime un compte
     */
    @DELETE
    @Path("/{id}")
    public Response deleteAccount(@PathParam("id") UUID id) {
        try {
            accountService.deleteAccount(id);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la suppression du compte: " + e.getMessage())
                    .build();
        }
    }
}
