package com.accessiq.repository;

import com.accessiq.model.RefreshToken;
import com.accessiq.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    List<RefreshToken> findByExpiryDateBefore(Instant expiryDate);

    long countByRevokedFalse();
}