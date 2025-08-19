package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.WorkoutDto.WorkoutUploadDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Workout.Workout;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountService {

    @Inject
    EntityManager em;

    public List<Account> getAllUsers() {
        return em.createQuery("SELECT a FROM Account a", Account.class)
                .getResultList();
    }

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

    public Optional<Account> findById(String id) {
        try {
            Account account = em.find(Account.class, UUID.fromString(id));
            return Optional.ofNullable(account);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

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
    public Account create(Account account) {
        // Hash le mot de passe avant de sauvegarder
        if (account.getPassword() != null) {
            account.setPassword(hashPassword(account.getPassword()));
        }

        em.persist(account);
        return account;
    }

    @Transactional
    public void update(Account account) {
        em.merge(account);
    }

    @Transactional
    public void delete(String id) {
        Account account = em.find(Account.class, UUID.fromString(id));
        if (account != null) {
            em.remove(account);
        }
    }

    @Transactional
    public void delete(UUID id) {
        Account account = em.find(Account.class, id);
        if (account != null) {
            em.remove(account);
        }
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public List<Account> findByLastName(String lastName) {
        return em.createQuery("SELECT a FROM Account a WHERE a.lastName LIKE :lastName", Account.class)
                .setParameter("lastName", "%" + lastName + "%")
                .getResultList();
    }
}