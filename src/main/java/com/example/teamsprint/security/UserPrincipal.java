package com.example.teamsprint.security;

import com.example.teamsprint.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // TODO: Implement account expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // TODO: Implement account locking logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // TODO: Implement credentials expiration logic
    }

    @Override
    public boolean isEnabled() {
        return true; // TODO: Implement user enabling/disabling logic
    }

    public Long getId() {
        return user.getId();
    }
}
