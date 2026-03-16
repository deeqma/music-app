package io.github.deeqma.music.service.auth;


import io.github.deeqma.music.dto.auth.AuthResponse;
import io.github.deeqma.music.dto.auth.LoginRequest;
import io.github.deeqma.music.dto.auth.RegisterRequest;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.UserException;
import io.github.deeqma.music.model.User;
import io.github.deeqma.music.repository.UserRepository;
import io.github.deeqma.music.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' already exists", request.getUsername());
            throw new UserException(ErrorType.ALREADY_EXISTS, "Username '" + request.getUsername() + "' is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        log.info("User registered: id={}, username={}", user.getId(), user.getUsername());

        return "Successfully created user: " + user.getId();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException _) {
            log.warn("Login failed: bad credentials for username '{}'", request.getUsername());
            throw new UserException(ErrorType.BAD_CREDENTIALS, "Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserException(ErrorType.NOT_FOUND, "User '" + request.getUsername() + "' not found"));

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());

        return new AuthResponse(token);
    }
}