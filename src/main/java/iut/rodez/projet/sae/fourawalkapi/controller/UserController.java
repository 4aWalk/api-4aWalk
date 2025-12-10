package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.controller.dto.UserLoginRequest;
import iut.rodez.projet.sae.fourawalkapi.controller.dto.UserRegistrationRequest;
import iut.rodez.projet.sae.fourawalkapi.controller.dto.UserResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.security.JwtTokenProvider;
import iut.rodez.projet.sae.fourawalkapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// Indique que c'est une API REST
@RestController
// Chemin de base pour les requêtes
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    // Injection du UserService
    public UserController(UserService userService, JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    // --- Endpoint 1: Inscription (UC001) ---

    /**
     * Requête : POST /api/v1/users/register
     * Crée un nouvel utilisateur.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {

        // Étape 1: Création de l'Entité User à partir du DTO
        User newUser = new User(); // Utilisez le constructeur de l'Entité ou MapStruct ici
        newUser.setMail(request.getMail());
        newUser.setPassword(request.getPassword()); // Le service va hacher ce mot de passe
        newUser.setNom(request.getNom());
        newUser.setPrenom(request.getPrenom());
        newUser.setAge(request.getAge());
        newUser.setNiveau(request.getNiveau());
        // Étape 2: Appel du service (la validation et le hachage se font ici)
        User registeredUser = userService.registerNewUser(newUser);

        UserResponseDto responseDto = new UserResponseDto(registeredUser);

        // Étape 3: Retourne la réponse (HTTP 201 Created)
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

    }

    // --- Endpoint 2: Connexion (UC002) ---

    /**
     * Requête : POST /api/v1/users/login
     * Tente d'authentifier un utilisateur.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {

        // 1. Utiliser Spring Security pour authentifier
        Authentication authentication = authenticationManager.authenticate(
                )
        );

        // 2. Si l'authentification réussit, définir le contexte
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Générer le token JWT
        String token = tokenProvider.generateToken(authentication);

        // 4. Retourner le token au client (qui doit le stocker)
        // NOTE: Il serait préférable de retourner un DTO contenant le token et les détails de l'utilisateur.
        return ResponseEntity.ok(token);
    }
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody UserLoginRequest request) {
        // ...
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getMail(), request.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        // 1. Récupérer l'Entité User via l'email
        User user = userService.findByMail(request.getMail()) // *Nécessite d'ajouter findByMail au UserService*
                .orElseThrow(() -> new InternalServerError("User not found after successful authentication."));

        // 2. Créer les DTOs
        UserResponseDto userDto = new UserResponseDto(user);
        JwtResponseDto response = new JwtResponseDto(token, userDto);

        // 3. Retourner le DTO combiné
        return ResponseEntity.ok(response);
    }
}