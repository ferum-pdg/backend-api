package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.entity.Account;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for user account access and management.
 *
 * Provides methods for searching, creating, updating,
 * deleting and password hashing/verification utilities.
 */
@ApplicationScoped
public class AccountService {

    @Inject
    EntityManager em;

    /**
     * Retrieves all user accounts.
     * @return list of accounts
     */
    public List<Account> getAllUsers() {
        return em.createQuery("SELECT a FROM Account a", Account.class)
                .getResultList();
    }

    /**
     * Searches for an account by email.
     * @param email email to search for
     * @return an Optional of the account if it exists
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
     * Searches for an account by identifier as String UUID.
     * @param id account identifier
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
     * Searches for an account by UUID identifier.
     * @param id account identifier
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

    /**
     * Creates an account, hashing the password and generating a UUID if necessary.
     * @param account account entity to create
     * @return the persisted account
     */
    @Transactional
    public Account create(Account account) {
        // Hash the password before saving
        if (account.getPassword() != null) {
            account.setPassword(hashPassword(account.getPassword()));
        }
        em.persist(account);
        return account;
    }

    /**
     * Updates an existing account.
     * @param account account to update
     */
    @Transactional
    public void update(Account account) {
        em.merge(account);
    }

    /**
     * Deletes an account by String UUID identifier.
     * @param id account identifier
     */
    @Transactional
    public void delete(String id) {
        Account account = em.find(Account.class, UUID.fromString(id));
        if (account != null) {
            em.remove(account);
        }
    }

    /**
     * Deletes an account by UUID identifier.
     * @param id account identifier
     */
    @Transactional
    public void delete(UUID id) {
        Account account = em.find(Account.class, id);
        if (account != null) {
            em.remove(account);
        }
    }

    /**
     * Verifies a plain text password against a BCrypt hash.
     * @param rawPassword plain text password
     * @param hashedPassword bcrypt hash
     * @return true if matches
     */
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    /**
     * Hashes a password with BCrypt.
     * @param rawPassword plain text password
     * @return bcrypt hash
     */
    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Searches for accounts by last name (LIKE %name%).
     * @param lastName last name
     * @return list of matching accounts
     */
    public List<Account> findByLastName(String lastName) {
        return em.createQuery("SELECT a FROM Account a WHERE a.lastName LIKE :lastName", Account.class)
                .setParameter("lastName", "%" + lastName + "%")
                .getResultList();
    }
}