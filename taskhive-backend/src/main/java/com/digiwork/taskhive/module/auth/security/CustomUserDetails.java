package com.digiwork.taskhive.module.auth.security;

import com.digiwork.taskhive.module.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final Long tokenVersion;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UUID id, String email, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this(id, email, password, enabled, true, 0L, authorities);
    }

    public static CustomUserDetails build(User user, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus() == com.digiwork.taskhive.module.auth.enums.UserStatus.ACTIVE,
                !user.isAccountLocked(),
                user.getTokenVersion(),
                authorities);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
