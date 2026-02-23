package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import iut.rodez.projet.sae.fourawalkapi.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test unitaire pour {@link UserService}.
 * Vérifie l'ensemble des règles de gestion liées aux utilisateurs :
 * - Inscription avec validation stricte des données (email, mot de passe, âge, champs obligatoires).
 * - Intégration avec Spring Security (récupération par email).
 * - Mise à jour du profil et répercussion (cascade) sur les participants associés.
 */
class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ParticipantRepository participantRepository;
    private UserService userService;

    /**
     * Initialisation de l'environnement de test.
     * Création des mocks pour isoler la logique métier des accès réels à la base de données,
     * puis injection manuelle de ces dépendances dans le service à tester.
     */
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        participantRepository = mock(ParticipantRepository.class);

        userService = new UserService(userRepository, passwordEncoder, participantRepository);
    }

    /**
     * Méthode utilitaire (Factory) générant une instance d'{@link User} valide par défaut.
     * Permet de garder les tests concis et de ne modifier que les champs nécessaires
     * selon le scénario de test (principe du DRY - Don't Repeat Yourself).
     *
     * @return un utilisateur avec toutes les données obligatoires correctement renseignées.
     */
    private User createValidUser() {
        User user = new User();
        user.setId(1L);
        user.setMail("test@example.com");
        user.setPassword("ValidPass123!");
        user.setNom("Doe");
        user.setPrenom("John");
        user.setAdresse("1 rue de la Paix");
        user.setAge(25);
        user.setNiveau(mock(Level.class));
        user.setMorphologie(mock(Morphology.class));
        return user;
    }

    // ==========================================
    // TESTS POUR registerNewUser (Création et Validation)
    // ==========================================

    /**
     * Vérifie que le processus d'inscription réussit lorsque toutes les données
     * fournies sont valides et que l'email est disponible.
     */
    @Test
    void registerNewUser_Success() {
        // GIVEN: Le contexte initial
        // Nous avons un utilisateur dont les données respectent toutes les règles métier.
        // On simule le fait que son adresse email n'existe pas encore en base de données (Optional.empty).
        // On prépare le mock de l'encodeur pour simuler le hachage sécurisé du mot de passe.
        User validUser = createValidUser();
        when(userRepository.findByMail(validUser.getMail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("hashedPass");
        when(userRepository.save(validUser)).thenReturn(validUser);

        // WHEN: L'action testée
        // L'utilisateur tente de s'inscrire via la méthode du service.
        User savedUser = userService.registerNewUser(validUser);

        // THEN: Les vérifications des résultats attendus
        // 1. Le mot de passe en clair doit avoir été remplacé par sa version hachée.
        // 2. La méthode save() du repository doit avoir été appelée exactement une fois avec cet utilisateur.
        assertEquals("hashedPass", savedUser.getPassword());
        verify(userRepository).save(validUser);
    }

    /**
     * Vérifie qu'il est impossible de créer un compte avec une adresse email
     * déjà associée à un autre utilisateur.
     */
    @Test
    void registerNewUser_EmailAlreadyExists() {
        // GIVEN: Le contexte initial
        // Un utilisateur tente de s'inscrire avec une adresse email "test@example.com".
        // Cependant, le repository simule la présence d'un utilisateur existant avec cet email.
        User validUser = createValidUser();
        when(userRepository.findByMail(validUser.getMail())).thenReturn(Optional.of(new User()));

        // WHEN & THEN: L'action et la vérification
        // L'appel au service doit être bloqué immédiatement par une IllegalArgumentException
        // avec un message d'erreur spécifique, empêchant ainsi la sauvegarde.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(validUser));
        assertEquals("L'adresse email est déjà utilisée.", exception.getMessage());

        // Sécurité supplémentaire : on s'assure que le repository n'a jamais tenté de sauvegarder.
        verify(userRepository, never()).save(any());
    }

    /**
     * Vérifie la validation stricte du format de l'adresse email.
     */
    @Test
    void registerNewUser_InvalidEmail_ThrowsException() {
        // GIVEN: Un utilisateur dont la chaîne email ne correspond pas à la RegEx
        // attendue (ex: absence de '@' ou de domaine valide).
        User invalidUser = createValidUser();
        invalidUser.setMail("mauvais-email.com");

        // WHEN & THEN: L'inscription est rejetée car le format est invalide.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(invalidUser));
        assertTrue(exception.getMessage().contains("format de l'adresse email est invalide"));
    }

    /**
     * Vérifie la politique de sécurité des mots de passe.
     */
    @Test
    void registerNewUser_InvalidPassword_ThrowsException() {
        // GIVEN: Un utilisateur soumettant un mot de passe trop faible
        // (ex: "weakpass" n'a pas de majuscule, chiffre ou caractère spécial).
        User invalidUser = createValidUser();
        invalidUser.setPassword("weakpass");

        // WHEN & THEN: Le service lève une exception indiquant que les critères
        // de sécurité du mot de passe ne sont pas remplis.
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(invalidUser));
    }

    /**
     * Vérifie le respect de la règle métier sur l'âge minimum requis (3 ans).
     */
    @Test
    void registerNewUser_InvalidAge_ThrowsException() {
        // GIVEN: Un utilisateur fournissant un âge inférieur à la limite autorisée.
        User invalidUser = createValidUser();
        invalidUser.setAge(2);

        // WHEN & THEN: L'inscription est bloquée par la validation des données.
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(invalidUser));
    }

    // ==========================================
    // TESTS POUR loadUserByUsername (Spring Security)
    // ==========================================

    /**
     * Teste la récupération réussie d'un utilisateur pour l'authentification Spring Security.
     */
    @Test
    void loadUserByUsername_Success() {
        // GIVEN: Une tentative de connexion avec un email valide.
        // Le repository renvoie un utilisateur existant correspondant à cet email.
        User user = createValidUser();
        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.of(user));

        // WHEN: Spring Security fait appel au service pour charger les détails du compte.
        UserDetails userDetails = userService.loadUserByUsername(user.getMail());

        // THEN: Un objet UserDetails est généré correctement avec les identifiants
        // nécessaires (email et mot de passe hashé) pour comparer avec la saisie de l'utilisateur.
        assertEquals(user.getMail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
    }

    /**
     * Vérifie le comportement lors d'une tentative de connexion avec un email inconnu.
     */
    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        // GIVEN: L'email fourni n'existe dans aucun enregistrement en base.
        when(userRepository.findByMail("ghost@test.com")).thenReturn(Optional.empty());

        // WHEN & THEN: Une exception spécifique à Spring Security (UsernameNotFoundException)
        // doit être levée pour signaler l'échec d'authentification.
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost@test.com"));
    }

    // ==========================================
    // TESTS POUR updateUser (Mise à jour et Cascade)
    // ==========================================

    /**
     * Teste la mise à jour des informations du profil utilisateur
     * et la synchronisation automatique des données vers son profil "Participant" associé.
     */
    @Test
    void updateUser_Success() {
        // GIVEN: Le contexte initial
        // Un utilisateur modifie son profil en changeant son nom ("Doe" -> "NouveauNom").
        // On simule que l'email n'est pas en conflit.
        // On prépare un objet Participant lié à cet utilisateur (car un utilisateur = un créateur).
        User userToUpdate = createValidUser();
        userToUpdate.setNom("NouveauNom");

        when(userRepository.findByMail(userToUpdate.getMail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userToUpdate.getPassword())).thenReturn("newHashedPass");
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        Participant linkedParticipant = new Participant();
        linkedParticipant.setNom("AncienNom"); // Nom avant la mise à jour
        when(participantRepository.findByCreatorIdAndCreatorTrue(userToUpdate.getId()))
                .thenReturn(List.of(linkedParticipant));

        // WHEN: L'action testée
        // On exécute la mise à jour via le service.
        User updatedUser = userService.updateUser(userToUpdate);

        // THEN: Les vérifications
        // 1. Le mot de passe a bien été encodé avant la sauvegarde de l'utilisateur.
        // 2. CASCADE : Le nom du participant associé a été mis à jour automatiquement pour correspondre au nouveau nom.
        // 3. La liste des participants mis à jour a bien été sauvegardée.
        assertEquals("newHashedPass", updatedUser.getPassword());
        assertEquals("NouveauNom", linkedParticipant.getNom());
        verify(participantRepository).saveAll(anyList());
    }

    /**
     * Empêche la mise à jour d'un objet utilisateur qui n'a pas d'identifiant en base (ID null).
     */
    @Test
    void updateUser_NullId_ThrowsException() {
        // GIVEN: Un DTO utilisateur envoyé depuis le front-end sans son identifiant (ID = null).
        User userWithoutId = createValidUser();
        userWithoutId.setId(null);

        // WHEN & THEN: L'opération est rejetée afin d'éviter de créer un doublon par erreur
        // (car save() créerait une nouvelle ligne si l'ID est null).
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userWithoutId));
        assertEquals("Impossible de mettre à jour un utilisateur sans ID.", exception.getMessage());
    }

    /**
     * Empêche un utilisateur de changer son adresse email pour une adresse
     * qui appartient déjà à un autre compte.
     */
    @Test
    void updateUser_EmailTakenByAnotherUser_ThrowsException() {
        // GIVEN: L'utilisateur avec l'ID 1 souhaite changer son email.
        // Cependant, l'email demandé est trouvé en base... mais il appartient à l'utilisateur ayant l'ID 2.
        User userToUpdate = createValidUser(); // ID = 1

        User otherUser = createValidUser();
        otherUser.setId(2L);
        when(userRepository.findByMail(userToUpdate.getMail())).thenReturn(Optional.of(otherUser));

        // WHEN & THEN: La mise à jour est interdite et une exception est levée.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userToUpdate));
        assertEquals("L'adresse email est déjà utilisée par un autre utilisateur.", exception.getMessage());
    }

    /**
     * Vérifie que l'absence de n'importe quel champ obligatoire déclenche
     * l'exception globale des champs requis.
     * Teste systématiquement les valeurs nulles et les chaînes vides/espaces.
     */
    @Test
    void registerNewUser_MissingMandatoryFields_ThrowsException() {
        // Test Email (null puis vide)
        User u1 = createValidUser();
        u1.setMail(null);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u1));
        assertEquals("Tous les champs obligatoires doivent être renseignés.", ex1.getMessage());

        u1.setMail("   ");
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u1));

        // Test Password (null puis vide)
        User u2 = createValidUser();
        u2.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u2));

        u2.setPassword(""); // Pas de trim() sur le mot de passe dans ta logique
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u2));

        // Test Nom (null puis vide)
        User u3 = createValidUser();
        u3.setNom(null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u3));

        u3.setNom("   ");
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u3));

        // Test Prénom (null puis vide)
        User u4 = createValidUser();
        u4.setPrenom(null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u4));

        u4.setPrenom("   ");
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u4));

        // Test Adresse (null puis vide)
        User u5 = createValidUser();
        u5.setAdresse(null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u5));

        u5.setAdresse("   ");
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u5));

        // Test Niveau (null)
        User u6 = createValidUser();
        u6.setNiveau(null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u6));

        // Test Morphologie (null)
        User u7 = createValidUser();
        u7.setMorphologie(null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(u7));
    }

    /**
     * Vérifie les bornes d'âge autorisées pour un utilisateur.
     * Le système doit bloquer les âges strictement inférieurs à 3 ans
     * et strictement supérieurs à 99 ans.
     */
    @Test
    void registerNewUser_AgeOutOfBounds_ThrowsException() {
        // Borne inférieure (trop jeune)
        User tooYoung = createValidUser();
        tooYoung.setAge(2);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(tooYoung));

        // Borne supérieure (trop âgé) -> C'est ce test qui va te donner le 4/4 !
        User tooOld = createValidUser();
        tooOld.setAge(100);
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(tooOld));
    }
}