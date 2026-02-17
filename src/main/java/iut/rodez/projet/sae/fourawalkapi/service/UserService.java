package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service utilisateur permettant de gérer l'inscriuption, la connexion et la mise à jour des informations
 * d'un utilisateur
 */
@Service
public class UserService implements UserDetailsService {

    private static final String EMAIL_REGEX =
            "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String P_REGEX = "^(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,}$";
    private static final Pattern P_PATTERN = Pattern.compile(P_REGEX);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParticipantRepository participantRepository;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder,
                       ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.participantRepository = participantRepository;
    }

    /**
     * Enregistre un nouvel utilisateur après validation et hachage du mot de passe.
     *
     * @param newUser L'objet utilisateur à créer.
     * @return L'utilisateur sauvegardé en base de données.
     * @throws IllegalArgumentException Si l'email existe déjà ou si les données sont invalides.
     */
    public User registerNewUser(User newUser) {
        validateUserData(newUser);

        if (userRepository.findByMail(newUser.getMail()).isPresent()) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
        }

        String hashedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        return userRepository.save(newUser);
    }

    /**
     * Charge un utilisateur par son email pour l'authentification Spring Security.
     *
     * @param email L'email de l'utilisateur.
     * @return Les détails de l'utilisateur (UserDetails).
     * @throws UsernameNotFoundException Si aucun utilisateur ne correspond à l'email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getMail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

    /**
     * Met à jour les informations d'un utilisateur existant.
     * Gère le changement de mot de passe et la vérification d'unicité de l'email.
     *
     * @param user L'utilisateur avec les nouvelles données.
     * @return L'utilisateur mis à jour.
     */
    @Transactional
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Impossible de mettre à jour un utilisateur sans ID.");
        }

        validateUserData(user);

        // Vérification unicité email
        Optional<User> existingUserWithEmail = userRepository.findByMail(user.getMail());
        if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée par un autre utilisateur.");
        }

        // Hachage mot de passe
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Sauvegarde de l'utilisateur
        User savedUser = userRepository.save(user);

        // Update des particpants impactés
        List<Participant> userAppearances = participantRepository.findByCreatorIdAndCreatorTrue(savedUser.getId());

        for (Participant p : userAppearances) {
            p.setNom(savedUser.getNom());
            p.setPrenom(savedUser.getPrenom());
            p.setAge(savedUser.getAge());
            p.setNiveau(savedUser.getNiveau());
            p.setMorphologie(savedUser.getMorphologie());
        }

        participantRepository.saveAll(userAppearances);

        return savedUser;
    }

    /**
     * Valide les données brutes de l'utilisateur (Regex Email, complexité mot de passe, âge).
     *
     * @param user L'utilisateur à valider.
     * @throws IllegalArgumentException Si une contrainte n'est pas respectée.
     */
    private void validateUserData(User user) {
        if (user.getMail() == null || user.getMail().trim().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getNom() == null || user.getNom().trim().isEmpty() ||
                user.getPrenom() == null || user.getPrenom().trim().isEmpty() ||
                user.getAdresse() == null || user.getAdresse().trim().isEmpty() ||
                user.getNiveau() == null || user.getMorphologie() == null) {

            throw new IllegalArgumentException("Tous les champs obligatoires doivent être renseignés.");
        }

        if (!EMAIL_PATTERN.matcher(user.getMail()).matches()) {
            throw new IllegalArgumentException("Le format de l'adresse email est invalide.");
        }

        if (!P_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères," +
                    " une majuscule et un caractère spécial.");
        }

        int age = user.getAge();
        if (age < 3 || age > 99) {
            throw new IllegalArgumentException("L'âge de l'utilisateur doit être compris entre 3 et 99 ans.");
        }
    }
}