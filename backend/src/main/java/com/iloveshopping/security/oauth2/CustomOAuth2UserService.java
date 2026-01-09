package com.iloveshopping.security.oauth2;

import com.iloveshopping.entity.AuthProvider;
import com.iloveshopping.entity.Role;
import com.iloveshopping.entity.User;
import com.iloveshopping.exception.OAuth2AuthenticationProcessingException;
import com.iloveshopping.repository.RoleRepository;
import com.iloveshopping.repository.UserRepository;
import com.iloveshopping.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 user service for handling OAuth2 authentication.
 * Supports Google and Facebook authentication providers.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == AuthProvider.LOCAL) {
                throw new OAuth2AuthenticationProcessingException(
                    "This email is already registered with a local account. Please use email/password login.");
            }
            
            AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
            if (user.getProvider() != provider) {
                throw new OAuth2AuthenticationProcessingException(
                    "This email is already registered with " + user.getProvider() + 
                    ". Please use your " + user.getProvider() + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("User role not found"));

        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .firstName(oAuth2UserInfo.getFirstName())
                .lastName(oAuth2UserInfo.getLastName())
                .avatarUrl(oAuth2UserInfo.getImageUrl())
                .provider(provider)
                .providerId(oAuth2UserInfo.getId())
                .emailVerified(true) // OAuth2 emails are considered verified
                .build();

        user.addRole(userRole);
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setFirstName(oAuth2UserInfo.getFirstName());
        existingUser.setLastName(oAuth2UserInfo.getLastName());
        existingUser.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
