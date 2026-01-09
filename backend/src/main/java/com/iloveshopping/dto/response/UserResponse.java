package com.iloveshopping.dto.response;

import com.iloveshopping.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    private Set<String> roles;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .emailVerified(user.getEmailVerified())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
