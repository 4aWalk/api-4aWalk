package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Indique à Spring que c'est un composant de service
@Service
public class UserService {

    private final UserRepository userRepository;

    // Injection de dépendance par constructeur (méthode recommandée)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Logique métier pour l'authentification (UC002).
     * Vérifie si l'utilisateur existe et si le mot de passe est correct.
     */
    public Optional<User> authenticate(String email, String password) {
        // 1. Trouver l'utilisateur par son email (requête générée par Spring Data)
        // Vous devrez ajouter cette méthode 'findByMail' au UserRepository plus tard.
        Optional<User> userOptional = userRepository.findByMail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 2. Vérification du mot de passe (DOIT être remplacé par un algorithme de hachage sécurisé!)
            /*if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }*/
        }

        return Optional.empty(); // Échec de l'authentification
    }

    public User registerNewUser(User newUser) {

        //Validation des champs obligatoires
        if (newUser.getNom() == null || newUser.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (newUser.getPrenom() == null || newUser.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        }
        if (newUser.getAdresse() == null || newUser.getAdresse().isBlank()) {
            throw new IllegalArgumentException("L'adresse est obligatoire.");
        }
        if (newUser.getMail() == null || newUser.getMail().isBlank()) {
            throw new IllegalArgumentException("L'adresse email est obligatoire.");
        }
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            throw new IllegalArgumentException("Le mots de passe est obligatoire.");
        }
        // TODO faire vérification Niveau et Morphologie

        //Validation de l'âge (> 10 ans)
        if (newUser.getAge() < 3) {
            throw new IllegalArgumentException("La date de naissance doit être de plus de 3 ans.");
        }

        //Vérification si l'email existe déjà
        if (userRepository.findByMail(newUser.getMail()).isPresent()) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
        }

        // Vérification taille mot de passe
        if (newUser.getPassword() == null || newUser.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }


        // 4. Hash du mot de passe (sécurité)
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(newUser.getPassword());
        //newUser.setPassword(hashedPassword);

        // 5. Sauvegarde en base
        return userRepository.save(newUser);
    }
}