package com.xinchao.FoodApp.security;

import com.xinchao.FoodApp.auth_users.entity.User;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@Builder
public class AuthUser implements UserDetails {

    private User user;

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
