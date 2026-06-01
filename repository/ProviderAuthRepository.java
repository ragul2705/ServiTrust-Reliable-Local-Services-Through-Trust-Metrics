package com.servitrust.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.servitrust.model.ProviderAuth;

public interface ProviderAuthRepository extends JpaRepository<ProviderAuth, Long> {
    Optional<ProviderAuth> findByEmail(String email);
    boolean existsByEmail(String email);
}