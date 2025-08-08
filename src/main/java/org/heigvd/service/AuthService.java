package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.heigvd.dto.LoginRequest;
import org.heigvd.dto.LoginResponse;
import org.heigvd.dto.RegisterRequest;
import org.heigvd.entity.Account;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class AuthService {

    @Inject
    EntityManager entityManager;

    @Inject
    JwtService jwtService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Account account = entityManager
                    .createQuery("SELECT a FROM Account a WHERE a.email = :email", Account.class)
                    .setParameter("email", request.getEmail())
                    .getSingleResult();

            if (BCrypt.checkpw(request.getPassword(), account.getPassword())) {
                String token = jwtService.generateToken(account);
                return new LoginResponse(token, account.getEmail(),
                        account.getFirstName(), account.getLastName());
            } else {
                throw new SecurityException("Mot de passe incorrect");
            }
        } catch (NoResultException e) {
            throw new SecurityException("Utilisateur non trouvé");
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        Long count = entityManager
                .createQuery("SELECT COUNT(a) FROM Account a WHERE a.email = :email", Long.class)
                .setParameter("email", request.getEmail())
                .getSingleResult();

        if (count > 0) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Hasher le mot de passe
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        // Créer le nouveau compte
        Account account = new Account(
                request.getEmail(),
                hashedPassword,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getBirthDate(),
                request.getWeight(),
                request.getHeight(),
                request.getFCMax() != null ? request.getFCMax() : 190
        );

        entityManager.persist(account);

        // Générer le token
        String token = jwtService.generateToken(account);
        return new LoginResponse(token, account.getEmail(),
                account.getFirstName(), account.getLastName());
    }
}