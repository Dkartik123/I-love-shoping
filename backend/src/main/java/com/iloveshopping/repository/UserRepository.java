package com.iloveshopping.repository;

import com.iloveshopping.entity.User;
import com.iloveshopping.entity.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.email = :email")
    void updateFailedLoginAttempts(@Param("attempts") int attempts, @Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :id")
    void updateUserEnabled(@Param("id") UUID id, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :id")
    void verifyEmail(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
}
