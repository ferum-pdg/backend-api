package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.workout_dto.WorkoutUploadDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.workout.Workout;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
/**
 * Service d'accès et de gestion des comptes utilisateurs.
 *
 * Fournit des méthodes de recherche, création, mise à jour,
 * suppression et utilitaires de hachage/vérification de mot de passe.
 */
public class AccountService {

    @Inject
    EntityManager em;

    /**
     * Récupère tous les comptes utilisateurs.
     * @return liste des comptes
     */
    public List<Account> getAllUsers() {
        return em.createQuery("SELECT a FROM Account a", Account.class)
                .getResultList();
    }

    /**
     * Recherche un compte par email.
     * @param email email recherché
     * @return un Optional du compte s'il existe
     */
    public Optional<Account> findByEmail(String email) {
        try {
            Account account = em.createQuery("SELECT a FROM Account a WHERE a.email = :email", Account.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(account);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Recherche un compte par identifiant sous forme de String UUID.
     * @param id identifiant du compte
     * @return Optional<Account>
     */
    public Optional<Account> findById(String id) {
        try {
            Account account = em.find(Account.class, UUID.fromString(id));
            return Optional.ofNullable(account);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Recherche un compte par identifiant UUID.
     * @param id identifiant du compte
     * @return Optional<Account>
     */
    public Optional<Account> findById(UUID id) {
        try {
            Account account = em.find(Account.class, id);
            return Optional.ofNullable(account);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void mergeWorkouts(Workout workout, WorkoutUploadDto workoutUploadDto) {
    }

    @Transactional
    /**
     * Crée un compte, en hachant le mot de passe et en générant un UUID si nécessaire.
     * @param account entité compte à créer
     * @return le compte persistant
     */
    public Account create(Account account) {
        // Hash le mot de passe avant de sauvegarder
        if (account.getPassword() != null) {
            account.setPassword(hashPassword(account.getPassword()));
        }
        em.persist(account);
        return account;
    }

    @Transactional
    /**
     * Met à jour un compte existant.
     * @param account compte à mettre à jour
     */
    public void update(Account account) {
        em.merge(account);
    }

    @Transactional
    /**
     * Supprime un compte par identifiant String UUID.
     * @param id identifiant du compte
     */
    public void delete(String id) {
        Account account = em.find(Account.class, UUID.fromString(id));
        if (account != null) {
            em.remove(account);
        }
    }

    @Transactional
    /**
     * Supprime un compte par identifiant UUID.
     * @param id identifiant du compte
     */
    public void delete(UUID id) {
        Account account = em.find(Account.class, id);
        if (account != null) {
            em.remove(account);
        }
    }

    /**
     * Vérifie un mot de passe en clair contre un hash BCrypt.
     * @param rawPassword mot de passe en clair
     * @param hashedPassword hash bcrypt
     * @return true si correspond
     */
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    /**
     * Hache un mot de passe avec BCrypt.
     * @param rawPassword mot de passe en clair
     * @return hash bcrypt
     */
    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Recherche des comptes par nom de famille (LIKE %nom%).
     * @param lastName nom de famille
     * @return liste des comptes correspondants
     */
    public List<Account> findByLastName(String lastName) {
        return em.createQuery("SELECT a FROM Account a WHERE a.lastName LIKE :lastName", Account.class)
                .setParameter("lastName", "%" + lastName + "%")
                .getResultList();
    }
}