package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.security.JwtTokenProvider;
import iut.rodez.projet.sae.fourawalkapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User savedUser = userService.registerNewUser(user);
        Authentication authForToken = new UsernamePasswordAuthenticationToken(savedUser.getMail(), null);

        String token = tokenProvider.generateToken(authForToken);

        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("mail");
        String password = loginRequest.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = tokenProvider.generateToken(authentication);

        User userFromDb = userService.findByMail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userFromDb);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody User user,
                                        Authentication authentication) {

        String currentEmail = authentication.getName();

        User currentUser = userService.findByMail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable."));

        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body("Accès refusé : Vous ne pouvez modifier que votre propre profil.");
        }

        user.setId(id);

        try {
            User updatedUser = userService.updateUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Erreur métier (ex: email déjà pris par quelqu'un d'autre)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}