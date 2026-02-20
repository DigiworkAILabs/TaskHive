package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.module.auth.enums.RoleType;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.Role;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedAdminUser() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(RoleType.ADMIN.name())
                .orElseThrow(() -> new RuntimeException("ADMIN role not found. Check Flyway migration V2."));

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .isDeleted(false)
                .build();

        admin = userRepository.save(admin);

        UserRole userRole = UserRole.builder()
                .userId(admin.getId())
                .roleId(adminRole.getId())
                .build();

        userRoleRepository.save(userRole);

        log.info("Default admin user created: {}", adminEmail);
    }
}
