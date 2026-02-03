package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
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

    private static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(User newUser) {
        validateUserData(newUser);

        if (userRepository.findByMail(newUser.getMail()).isPresent()) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
        }

        String hashedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        return userRepository.save(newUser);
    }

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

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Impossible de mettre à jour un utilisateur sans ID.");
        }

        validateUserData(user);

        Optional<User> existingUserWithEmail = userRepository.findByMail(user.getMail());

        if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée par un autre utilisateur.");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByMail(String mail) {
        return userRepository.findByMail(mail);
    }

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

        if (!PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un caractère spécial.");
        }

        int age = user.getAge();
        if (age < 3 || age > 99) {
            throw new IllegalArgumentException("L'âge de l'utilisateur doit être compris entre 3 et 99 ans.");
        }
    }
}