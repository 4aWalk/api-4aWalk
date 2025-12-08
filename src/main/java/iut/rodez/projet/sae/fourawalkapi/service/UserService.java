package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

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

    /**
     * Logique métier pour la création de compte (UC001).
     * Gère la validation et l'enregistrement dans la base de données.
     */
    public User registerNewUser(User newUser) {
        // --- Exigences de la logique métier (UC001) ---

        // 1. Vérification de l'existence (UC001 - Ne pas créer si mail existe déjà)
        /*if (userRepository.findByMail(newUser.getMail()).isPresent()) {
            // Dans un vrai projet, on lancerait ici une exception personnalisée
            throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
        }*/

        // 2. Validation des données (Assurez-vous que les champs sont valides, ex: âge > 10)
        // Ceci pourrait faire appel à un service de validation dédié ou à un Pattern Strategy

        // 3. Hachage du mot de passe (CRITIQUE pour la sécurité!)
        // Ici, vous devrez utiliser un outil comme BCrypt, PAS le password en clair.
        // newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // 4. Sauvegarde dans la base de données (Utilise la méthode 'save' du JpaRepository)
        return userRepository.save(newUser);
    }
}