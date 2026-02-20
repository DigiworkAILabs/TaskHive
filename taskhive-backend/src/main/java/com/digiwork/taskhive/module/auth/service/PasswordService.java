package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.module.auth.model.PasswordHistory;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Checks if the new password matches any of the user's last 3 passwords.
     * Throws BusinessException if reuse is detected.
     */
    public void validateNotReused(User user, String newRawPassword) {
        List<PasswordHistory> recentPasswords = passwordHistoryRepository
                .findTop3ByUserIdOrderByCreatedAtDesc(user.getId());

        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newRawPassword, history.getPasswordHash())) {
                throw new com.digiwork.taskhive.common.exception.BusinessException(
                        MessageConstants.PASSWORD_SAME_AS_OLD);
            }
        }
    }

    @Transactional
    public void saveToHistory(User user) {
        if (user.getPasswordHash() != null) {
            PasswordHistory history = PasswordHistory.builder()
                    .userId(user.getId())
                    .passwordHash(user.getPasswordHash())
                    .build();
            passwordHistoryRepository.save(history);
        }
    }
}
