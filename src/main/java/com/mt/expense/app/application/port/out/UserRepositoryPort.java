package com.mt.expense.app.application.port.out;

import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.vo.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound Port (Secondary) — Repository interface for user persistence. */
public interface UserRepositoryPort {

    /**
     * Saves a user to the repository.
     *
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Finds a user by its ID.
     *
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findById(UserId id);

    /**
     * Finds a user by their Keycloak ID.
     *
     * @param keycloakId the Keycloak user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findByKeycloakId(UUID keycloakId);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given Keycloak ID.
     *
     * @param keycloakId the Keycloak user ID
     * @return true if a user exists
     */
    boolean existsByKeycloakId(UUID keycloakId);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email address
     * @return true if a user exists
     */
    boolean existsByEmail(String email);

    /**
     * Searches for users by email or name (case-insensitive substring match).
     *
     * @param query the search string
     * @param limit maximum number of results
     * @return list of matching users
     */
    List<User> searchByEmailOrName(String query, int limit);
}
