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

/**
 * @brief Contrôleur REST pour les opérations sur les comptes
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    /**
     * @brief Récupère la liste de tous les comptes
     * @return Response contenant la liste des comptes ou une erreur
     * @retval 200 OK - Liste des comptes au format JSON
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur lors de la récupération
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
     * @brief Récupère un compte spécifique par son identifiant
     * @param id L'identifiant unique du compte (UUID)
     * @return Response contenant le compte demandé ou une erreur
     * @retval 200 OK - Compte trouvé et retourné
     * @retval 404 NOT_FOUND - Compte non trouvé
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
     */
    @GET
    @Path("/{id}")
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
     * @brief Récupère un compte par son adresse email
     * @param email L'adresse email du compte à rechercher
     * @return Response contenant le compte trouvé ou une erreur
     * @retval 200 OK - Compte trouvé
     * @retval 404 NOT_FOUND - Aucun compte avec cet email
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
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
     * @brief Recherche des comptes par nom ou prénom
     * @param searchTerm Le terme de recherche (nom ou prénom)
     * @return Response contenant la liste des comptes correspondants
     * @retval 200 OK - Liste des comptes trouvés (peut être vide)
     * @retval 400 BAD_REQUEST - Terme de recherche manquant ou vide
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
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
     * @brief Crée un nouveau compte utilisateur
     * @param account L'objet Account contenant les informations du nouveau compte
     * @return Response contenant le compte créé ou une erreur
     * @retval 201 CREATED - Compte créé avec succès
     * @retval 400 BAD_REQUEST - Email manquant ou invalide
     * @retval 409 CONFLICT - Email déjà utilisé
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
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
     * @brief Met à jour complètement un compte existant
     * @param id L'identifiant du compte à mettre à jour
     * @param updatedAccount L'objet Account avec les nouvelles informations
     * @return Response contenant le compte mis à jour ou une erreur
     * @retval 200 OK - Compte mis à jour avec succès
     * @retval 404 NOT_FOUND - Compte non trouvé
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
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
     * @brief Met à jour partiellement un compte existant
     * @param id L'identifiant du compte à modifier
     * @param partialUpdate L'objet Account contenant seulement les champs à modifier
     * @return Response contenant le compte mis à jour ou une erreur
     * @retval 200 OK - Compte modifié avec succès
     * @retval 404 NOT_FOUND - Compte non trouvé
     * @retval 409 CONFLICT - Conflit lors de la modification (ex: email déjà utilisé)
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
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
     * @brief Supprime définitivement un compte
     * @param id L'identifiant du compte à supprimer
     * @return Response indiquant le succès ou l'échec de l'opération
     * @retval 204 NO_CONTENT - Compte supprimé avec succès
     * @retval 404 NOT_FOUND - Compte non trouvé
     * @retval 500 INTERNAL_SERVER_ERROR - Erreur serveur
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