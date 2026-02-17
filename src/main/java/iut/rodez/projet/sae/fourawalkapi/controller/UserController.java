package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.UserResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import iut.rodez.projet.sae.fourawalkapi.security.JwtTokenProvider;
import iut.rodez.projet.sae.fourawalkapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlleur des endpoints de gestion d'utilisateur
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Injection de dépendance
     * @param userService service utilisateur
     * @param userRepository repository utilisateur
     * @param authenticationManager manager d'authentification spring
     * @param tokenProvider récupérateur d'authentification
     */
    public UserController(UserService userService, UserRepository userRepository, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Inscription d'un utilisateur
     * @param user utilisateur à inscrire
     * @return le token d'authentification et l'utilisateur créer
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        User savedUser = userService.registerNewUser(user);

        String token = tokenProvider.generateToken(savedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", new UserResponseDto(savedUser));

        return ResponseEntity.ok(response);
    }

    /**
     * Connexion de l'utilisateur
     * @param loginRequest map de mail et de mot de passe
     * @return le token de connexion et l'utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("mail");
        String password = loginRequest.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = tokenProvider.generateToken(authentication);

        User userFromDb = userRepository.findByMail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", new UserResponseDto(userFromDb));

        return ResponseEntity.ok(response);
    }

    /**
     * Mise à jour des informations de l'utilisateur
     * @param id identifiant de l'utilisateur à modifier
     * @param user utilisateur modifier
     * @param auth token d'identification
     * @return L'utilisateur mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @RequestBody User user,
                                                      Authentication auth) {
        String currentEmail = auth.getName();
        User currentUser = userRepository.findByMail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur connecté introuvable."));
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : Vous ne pouvez modifier que votre propre profil.");
        }
        user.setId(id);
        try {
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(new UserResponseDto(updatedUser));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}