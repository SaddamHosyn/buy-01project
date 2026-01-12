package ax.gritlab.buy_01.user.controller;

import ax.gritlab.buy_01.user.dto.AuthenticationRequest;
import ax.gritlab.buy_01.user.dto.AuthenticationResponse;
import ax.gritlab.buy_01.user.dto.RegisterRequest;
import ax.gritlab.buy_01.user.dto.UserProfileResponse;
import ax.gritlab.buy_01.user.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public final class AuthController {

    /** Authentication service. */
    private final AuthenticationService authenticationService;

    /**
     * Registers a new user.
     *
     * @param request registration request
     * @return user profile response
     */
    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(
            @Valid @RequestBody final RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    /**
     * Authenticates a user and returns JWT token.
     *
     * @param request authentication request
     * @return authentication response with token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody final AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
