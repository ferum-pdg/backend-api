package org.heigvd.service;

import org.heigvd.entity.Account;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * @brief Service de gestion des comptes utilisateur
 */
@ApplicationScoped
public class AccountService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @brief Récupère tous les comptes de la base de données
     * @return Liste de tous les comptes existants
     */
    public List<Account> getAllAccounts() {
        return entityManager.createQuery("SELECT a FROM Account a", Account.class)
                .getResultList();
    }

    /**
     * @brief Récupère un compte par son identifiant unique
     * @param id L'identifiant UUID du compte à récupérer
     * @return Le compte correspondant à l'ID fourni
     */
    public Account getAccountById(UUID id) {
        Account account = entityManager.find(Account.class, id);
        if (account == null) {
            throw new NotFoundException("Account with id " + id + " not found");
        }
        return account;
    }

    /**
     * @brief Récupère un compte par son adresse email
     * @param email L'adresse email du compte à rechercher
     * @return Le compte correspondant à l'email ou null si non trouvé
     */
    public Account getAccountByEmail(String email) {
        try {
            return entityManager.createQuery("SELECT a FROM Account a WHERE a.email = :email", Account.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * @brief Crée un nouveau compte dans la base de données
     * @param account L'objet Account à créer
     * @return Le compte créé avec son ID généré
     */
    @Transactional
    public Account createAccount(Account account) {
        // Vérification de l'unicité de l'email
        if (getAccountByEmail(account.getEmail()) != null) {
            throw new IllegalArgumentException("Account with email " + account.getEmail() + " already exists");
        }

        // Génération automatique de l'UUID si absent
        if (account.getId() == null) {
            account.setId(UUID.randomUUID());
        }

        entityManager.persist(account);
        return account;
    }

    /**
     * @brief Met à jour complètement un compte existant
     * @param id L'identifiant UUID du compte à mettre à jour
     * @param updatedAccount L'objet Account contenant les nouvelles données
     * @return Le compte mis à jour
     * @TODO Implémenter la mise à jour du mot de passe avec hashage sécurisé
     */
    @Transactional
    public Account updateAccount(UUID id, Account updatedAccount) {
        Account existingAccount = getAccountById(id);

        // Mise à jour des champs modifiables
        if (updatedAccount.getFirstName() != null) {
            existingAccount.setFirstName(updatedAccount.getFirstName());
        }
        if (updatedAccount.getLastName() != null) {
            existingAccount.setLastName(updatedAccount.getLastName());
        }
        if (updatedAccount.getEmail() != null) {
            existingAccount.setEmail(updatedAccount.getEmail());
        }
        if (updatedAccount.getPhoneNumber() != null) {
            existingAccount.setPhoneNumber(updatedAccount.getPhoneNumber());
        }
        if (updatedAccount.getBirthDate() != null) {
            existingAccount.setBirthDate(updatedAccount.getBirthDate());
        }
        if (updatedAccount.getHeight() != null) {
            existingAccount.setHeight(updatedAccount.getHeight());
        }
        if (updatedAccount.getWeight() != null) {
            existingAccount.setWeight(updatedAccount.getWeight());
        }
        if (updatedAccount.getFcmax() != null) {
            existingAccount.setFcmax(updatedAccount.getFcmax());
        }

        // Le mot de passe nécessite une logique particulière (hashage)
        /*
        if (updatedAccount.getPassword() != null && !updatedAccount.getPassword().isEmpty()) {
            existingAccount.setPassword(updatedAccount.getPassword());
        }
        */

        return entityManager.merge(existingAccount);
    }

    /**
     * @brief Supprime définitivement un compte de la base de données
     * @param id L'identifiant UUID du compte à supprimer
     */
    @Transactional
    public void deleteAccount(UUID id) {
        Account account = getAccountById(id);
        entityManager.remove(account);
    }

    /**
     * @brief Recherche des comptes par nom ou prénom (insensible à la casse)
     * @param searchTerm Le terme de recherche à chercher dans les noms et prénoms
     * @return Liste des comptes correspondant aux critères de recherche
     */
    public List<Account> searchAccountsByName(String searchTerm) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a WHERE " +
                                "LOWER(a.firstName) LIKE LOWER(:searchTerm) OR " +
                                "LOWER(a.lastName) LIKE LOWER(:searchTerm)",
                        Account.class)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .getResultList();
    }

    /**
     * @brief Compte le nombre total de comptes dans la base de données
     * @return Le nombre total de comptes existants
     */
    public long countAccounts() {
        return entityManager.createQuery("SELECT COUNT(a) FROM Account a", Long.class)
                .getSingleResult();
    }

    /**
     * @brief Vérifie si une adresse email existe déjà dans la base de données
     * @param email L'adresse email à vérifier
     * @return true si l'email existe, false sinon
     */
    public boolean emailExists(String email) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(a) FROM Account a WHERE a.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    /**
     * @brief Effectue une mise à jour partielle d'un compte (opération PATCH)
     * @param id L'identifiant UUID du compte à mettre à jour partiellement
     * @param partialUpdate L'objet Account contenant uniquement les champs à modifier
     * @return Le compte mis à jour avec les nouvelles valeurs
     */
    @Transactional
    public Account patchAccount(UUID id, Account partialUpdate) {
        Account existingAccount = getAccountById(id);

        // Mise à jour sélective - seulement les champs non null
        if (partialUpdate.getFirstName() != null) {
            existingAccount.setFirstName(partialUpdate.getFirstName());
        }
        if (partialUpdate.getLastName() != null) {
            existingAccount.setLastName(partialUpdate.getLastName());
        }
        if (partialUpdate.getEmail() != null) {
            // Vérifier l'unicité si l'email change
            if (!partialUpdate.getEmail().equals(existingAccount.getEmail())) {
                if (emailExists(partialUpdate.getEmail())) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }
            existingAccount.setEmail(partialUpdate.getEmail());
        }
        if (partialUpdate.getPhoneNumber() != null) {
            existingAccount.setPhoneNumber(partialUpdate.getPhoneNumber());
        }
        if (partialUpdate.getBirthDate() != null) {
            existingAccount.setBirthDate(partialUpdate.getBirthDate());
        }
        if (partialUpdate.getHeight() != null) {
            existingAccount.setHeight(partialUpdate.getHeight());
        }
        if (partialUpdate.getWeight() != null) {
            existingAccount.setWeight(partialUpdate.getWeight());
        }
        if (partialUpdate.getFcmax() != null) {
            existingAccount.setFcmax(partialUpdate.getFcmax());
        }

        return entityManager.merge(existingAccount);
    }
}