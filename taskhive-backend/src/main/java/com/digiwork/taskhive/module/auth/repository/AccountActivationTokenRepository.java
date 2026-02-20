package com.digiwork.taskhive.module.auth.repository;

import com.digiwork.taskhive.module.auth.model.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, UUID> {

    Optional<AccountActivationToken> findByTokenHashAndUsedFalse(String tokenHash);
}
