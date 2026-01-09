package com.iloveshopping.repository;

import com.iloveshopping.entity.RefreshToken;
import com.iloveshopping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entity operations.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.token = :token")
    void revokeToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM RefreshToken r WHERE r.user.id = :userId AND r.revoked = false AND r.expiresAt > :now")
    Optional<RefreshToken> findActiveTokenByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    long countByUserAndRevokedFalseAndExpiresAtAfter(User user, LocalDateTime now);
}
