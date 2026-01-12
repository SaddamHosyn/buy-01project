package ax.gritlab.buy_01.user.service;

import ax.gritlab.buy_01.user.dto.AuthenticationRequest;
import ax.gritlab.buy_01.user.dto.AuthenticationResponse;
import ax.gritlab.buy_01.user.dto.RegisterRequest;
import ax.gritlab.buy_01.user.dto.UserProfileResponse;
import ax.gritlab.buy_01.user.model.User;
import ax.gritlab.buy_01.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service for user registration and login.
 */
@Service
@RequiredArgsConstructor
public final class AuthenticationService {

        /** User repository for database access. */
        private final UserRepository userRepository;
        /** Password encoder for hashing. */
        private final PasswordEncoder passwordEncoder;
        /** JWT service for token operations. */
        private final JwtService jwtService;
        /** Authentication manager for validating credentials. */
        private final AuthenticationManager authenticationManager;

        /**
         * Registers a new user.
         *
         * @param request registration request
         * @return user profile response
         */
        public UserProfileResponse register(final RegisterRequest request) {
                var user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(
                                        request.getPassword()))
                                .role(request.getRole())
                                .build();
                User savedUser = userRepository.save(user);
                return UserProfileResponse.builder()
                                .id(savedUser.getId())
                                .name(savedUser.getName())
                                .email(savedUser.getEmail())
                                .role(savedUser.getRole())
                                .avatar(savedUser.getAvatar())
                                .build();
        }

        /**
         * Authenticates user and generates JWT token.
         *
         * @param request authentication request
         * @return authentication response with token
         */
        public AuthenticationResponse authenticate(
                final AuthenticationRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));
                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow();
                var jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .role(user.getRole())
                                .avatarUrl(user.getAvatar())
                                .build();
        }
}
