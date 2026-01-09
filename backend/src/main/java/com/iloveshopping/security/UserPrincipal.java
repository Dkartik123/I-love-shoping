package com.iloveshopping.security;

import com.iloveshopping.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation that works with both local and OAuth2 authentication.
 */
@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final UUID id;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final Boolean enabled;
    private final Boolean accountNonLocked;
    private final Boolean emailVerified;
    private final Boolean twoFactorEnabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(UUID id, String email, String password, String firstName, 
                         String lastName, Boolean enabled, Boolean accountNonLocked,
                         Boolean emailVerified, Boolean twoFactorEnabled,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.emailVerified = emailVerified;
        this.twoFactorEnabled = twoFactorEnabled;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getEnabled(),
                user.getAccountNonLocked(),
                user.getEmailVerified(),
                user.getTwoFactorEnabled(),
                authorities
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
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

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isTwoFactorRequired() {
        return twoFactorEnabled != null && twoFactorEnabled;
    }
}
