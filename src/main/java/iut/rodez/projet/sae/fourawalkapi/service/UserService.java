package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    // Regex pour un format d'email standard
    private static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Regex Mot de passe: 8+ chars, 1 Majuscule, 1 Caractère spécial
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur pour l'injection des dépendances du Repository et du PasswordEncoder.
     * @param userRepository Le repository pour l'accès aux données des utilisateurs.
     * @param passwordEncoder L'encodeur BCrypt pour le hachage des mots de passe.
     */
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) { // <-- AJOUT de @Lazy
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur (Inscription - UC001).
     * Effectue la validation des données et le hachage du mot de passe.
     * * @param newUser L'objet User (avec mot de passe en clair) à enregistrer.
     * @return L'objet User sauvegardé avec le mot de passe haché.
     * @throws IllegalArgumentException si les données sont invalides ou si l'email est déjà utilisé.
     */
    public User registerNewUser(User newUser) {

        validateUserData(newUser);

        if (userRepository.findByMail(newUser.getMail()).isPresent()) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
        }

        // Hachage du mot de passe avec salage automatique (BCrypt)
        String hashedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        return userRepository.save(newUser);
    }

    /**
     * Charge les données de l'utilisateur pour Spring Security (utilise l'email comme "username").
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // Spring Security a besoin d'un objet UserDetails. Nous utilisons ici l'implémentation par défaut.
        // Remplacez 'null' par la liste des Rôles (Authorities) si vous les utilisez (e.g., ROLE_USER).
        return new org.springframework.security.core.userdetails.User(
                user.getMail(),
                user.getPassword(), // Mot de passe HACHÉ
                Collections.emptyList() // Authorities/Rôles (laissez null ou mettez une liste vide pour l'instant)
        );
    }

    /**
     * Récupère un utilisateur par son identifiant unique.
     * * @param userId L'ID de l'utilisateur.
     * @return Un Optional contenant l'utilisateur.
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * **[NOUVELLE MÉTHODE]** Recherche un utilisateur par son adresse email.
     * @param mail L'adresse email de l'utilisateur.
     * @return Un Optional contenant l'utilisateur s'il est trouvé.
     */
    public Optional<User> findByMail(String mail) {
        return userRepository.findByMail(mail);
    }

    /**
     * Méthode interne pour la validation des contraintes métier.
     */
    private void validateUserData(User user) {

        // Vérification des champs non nuls/vides (omission de la vérification du niveau et date de naissance ici
        // car ces objets sont déjà vérifiés comme non-nulls après l'affectation des autres vérifications)
        if (user.getMail() == null || user.getMail().trim().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getNom() == null || user.getNom().trim().isEmpty() ||
                user.getPrenom() == null || user.getPrenom().trim().isEmpty() ||
                user.getAdresse() == null || user.getAdresse().trim().isEmpty() ||
                user.getNiveau() == null || user.getMorphologie() == null) {

            throw new IllegalArgumentException("Tous les champs obligatoires doivent être renseignés.");
        }

        // Validation format email
        if (!EMAIL_PATTERN.matcher(user.getMail()).matches()) {
            throw new IllegalArgumentException("Le format de l'adresse email est invalide.");
        }

        // Validation format mot de passe
        if (!PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un caractère spécial.");
        }

        // Validation âge (3-99 ans)
        int age = user.getAge();
        if (age < 3 || age > 99) {
            throw new IllegalArgumentException("L'âge de l'utilisateur doit être compris entre 3 et 99 ans.");
        }
    }

}