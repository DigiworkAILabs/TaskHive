package com.digiwork.taskhive.module.auth.security;

import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername: should return user details when found")
    void loadUserByUsername_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashed")
                .status(com.digiwork.taskhive.module.auth.enums.UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse("test@example.com")).thenReturn(Optional.of(user));
        when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("USER"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("loadUserByUsername: should throw exception when not found")
    void loadUserByUsername_NotFound() {
        when(userRepository.findByEmailAndIsDeletedFalse("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
