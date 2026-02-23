package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.ParticipantRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.UserRepository;
import iut.rodez.projet.sae.fourawalkapi.model.enums.*; // J'assume le package de tes enums
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ParticipantRepository participantRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        participantRepository = mock(ParticipantRepository.class);

        // Injection dans le service
        userService = new UserService(userRepository, passwordEncoder, participantRepository);
    }

    /**
     * Helper pour créer un utilisateur valide et éviter de dupliquer du code.
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
    // TESTS POUR registerNewUser (ET validation)
    // ==========================================

    @Test
    void registerNewUser_Success() {
        // Given: Un nouvel utilisateur totalement valide
        User validUser = createValidUser();
        when(userRepository.findByMail(validUser.getMail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("hashedPass");
        when(userRepository.save(validUser)).thenReturn(validUser);

        // When: On l'enregistre
        User savedUser = userService.registerNewUser(validUser);

        // Then: Le mot de passe a été haché et l'utilisateur sauvegardé
        assertEquals("hashedPass", savedUser.getPassword());
        verify(userRepository).save(validUser);
    }

    @Test
    void registerNewUser_EmailAlreadyExists() {
        // Given: Un utilisateur valide, mais dont le mail existe déjà en base
        User validUser = createValidUser();
        when(userRepository.findByMail(validUser.getMail())).thenReturn(Optional.of(new User()));

        // When & Then: Une exception est levée
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(validUser));
        assertEquals("L'adresse email est déjà utilisée.", exception.getMessage());
    }

    @Test
    void registerNewUser_InvalidEmail_ThrowsException() {
        // Given: Un utilisateur avec un mauvais format d'email (déclenche validateUserData)
        User invalidUser = createValidUser();
        invalidUser.setMail("mauvais-email.com");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(invalidUser));
        assertTrue(exception.getMessage().contains("format de l'adresse email est invalide"));
    }

    @Test
    void registerNewUser_InvalidPassword_ThrowsException() {
        // Given: Un mot de passe sans majuscule ni caractère spécial
        User invalidUser = createValidUser();
        invalidUser.setPassword("weakpass");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(invalidUser));
    }

    @Test
    void registerNewUser_InvalidAge_ThrowsException() {
        // Given: Un utilisateur de 2 ans (limite < 3)
        User invalidUser = createValidUser();
        invalidUser.setAge(2);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(invalidUser));
    }

    // ==========================================
    // TESTS POUR loadUserByUsername
    // ==========================================

    @Test
    void loadUserByUsername_Success() {
        // Given: Un email existant
        User user = createValidUser();
        when(userRepository.findByMail(user.getMail())).thenReturn(Optional.of(user));

        // When: On charge l'utilisateur
        UserDetails userDetails = userService.loadUserByUsername(user.getMail());

        // Then: On récupère bien les infos pour Spring Security
        assertEquals(user.getMail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        // Given: Un email qui n'existe pas en base
        when(userRepository.findByMail("ghost@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost@test.com"));
    }

    // ==========================================
    // TESTS POUR updateUser
    // ==========================================

    @Test
    void updateUser_Success() {
        // Given: Un utilisateur valide avec ID, on modifie son nom
        User userToUpdate = createValidUser();
        userToUpdate.setNom("NouveauNom");

        // Simule que l'email n'est pris par personne d'autre
        when(userRepository.findByMail(userToUpdate.getMail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userToUpdate.getPassword())).thenReturn("newHashedPass");
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // Simule les participants liés à cet utilisateur
        Participant linkedParticipant = new Participant();
        when(participantRepository.findByCreatorIdAndCreatorTrue(userToUpdate.getId()))
                .thenReturn(List.of(linkedParticipant));

        // When: On met à jour
        User updatedUser = userService.updateUser(userToUpdate);

        // Then: Les informations sont sauvegardées, ET le participant est mis à jour
        assertEquals("newHashedPass", updatedUser.getPassword());
        assertEquals("NouveauNom", linkedParticipant.getNom()); // Vérifie la cascade
        verify(participantRepository).saveAll(anyList());
    }

    @Test
    void updateUser_NullId_ThrowsException() {
        // Given: Un utilisateur sans ID
        User userWithoutId = createValidUser();
        userWithoutId.setId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userWithoutId));
        assertEquals("Impossible de mettre à jour un utilisateur sans ID.", exception.getMessage());
    }

    @Test
    void updateUser_EmailTakenByAnotherUser_ThrowsException() {
        // Given: On veut update notre utilisateur (ID 1), mais l'email est pris par l'ID 2
        User userToUpdate = createValidUser(); // ID = 1

        User otherUser = createValidUser();
        otherUser.setId(2L);
        when(userRepository.findByMail(userToUpdate.getMail())).thenReturn(Optional.of(otherUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userToUpdate));
        assertEquals("L'adresse email est déjà utilisée par un autre utilisateur.", exception.getMessage());
    }
}