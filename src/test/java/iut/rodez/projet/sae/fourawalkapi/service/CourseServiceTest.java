package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.document.Course;
import iut.rodez.projet.sae.fourawalkapi.dto.CourseResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mongo.CourseRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour le service CourseService.
 * Vérifie la logique métier des parcours (sessions de marche en temps réel),
 * la synchronisation entre MySQL (Hike) et MongoDB (Course), et les règles de sécurité.
 */
class CourseServiceTest {

    private CourseRepository courseRepository;
    private HikeRepository hikeRepository;
    private CourseService courseService;

    private Hike mockHike;
    private Course mockCourse;
    private CourseResponseDto mockDto;

    /**
     * Initialisation des bouchons (mocks) et des données de test avant chaque scénario.
     */
    @BeforeEach
    void setUp() {
        courseRepository = mock(CourseRepository.class);
        hikeRepository = mock(HikeRepository.class);
        courseService = new CourseService(courseRepository, hikeRepository);

        // --- Données factices pour les tests ---
        User creatorUser = new User();
        creatorUser.setId(10L);

        mockHike = new Hike();
        mockHike.setId(100L);
        mockHike.setCreator(creatorUser);

        mockCourse = new Course();
        mockCourse.setId("mongo-id-123");
        mockCourse.setHikeId(100L);
        mockCourse.setFinished(false);
        mockCourse.setPaused(false);

        mockCourse.addCoordinate(44.35, 2.57); // Point initial (Départ)

        // DTO entrant simulé
        mockDto = mock(CourseResponseDto.class);
        when(mockDto.getHikeId()).thenReturn(100L);
        CourseResponseDto.CoordinateDto pointDto = mock(CourseResponseDto.CoordinateDto.class);
        when(mockDto.getPath()).thenReturn(List.of(pointDto));
    }

    // ==========================================
    // TESTS : LECTURE DES PARCOURS
    // ==========================================

    /**
     * Teste la récupération réussie d'un parcours existant via son ID MongoDB.
     */
    @Test
    void getCourseById_Found() {
        // Given : Le parcours existe dans la base NoSQL
        when(courseRepository.findById("mongo-id-123")).thenReturn(Optional.of(mockCourse));

        // When : On demande la récupération du parcours
        CourseResponseDto result = courseService.getCourseById("mongo-id-123");

        // Then : Le DTO est bien retourné et correspond au document
        assertNotNull(result);
        verify(courseRepository).findById("mongo-id-123");
    }

    /**
     * Teste la récupération de l'historique complet des parcours d'un utilisateur.
     * Vérifie la jointure applicative entre MySQL (Hikes) et MongoDB (Courses).
     */
    @Test
    void getCoursesByUser_Success() {
        // Given : L'utilisateur a créé une randonnée (MySQL), qui contient un parcours (MongoDB)
        when(hikeRepository.findByCreatorId(10L)).thenReturn(List.of(mockHike));
        when(courseRepository.findByHikeId(100L)).thenReturn(List.of(mockCourse));

        // When : On récupère l'historique de l'utilisateur
        List<CourseResponseDto> result = courseService.getCoursesByUser(10L);

        // Then : La liste contient bien le parcours associé
        assertEquals(1, result.size());
        verify(hikeRepository).findByCreatorId(10L);
        verify(courseRepository).findByHikeId(100L);
    }

    // ==========================================
    // TESTS : CRÉATION DE PARCOURS
    // ==========================================

    /**
     * Teste la création d'une nouvelle session de marche avec succès.
     * Vérifie que le parcours est bien créé et sauvegardé lorsque la randonnée
     * associée a déjà été optimisée (le flag optimize est à false).
     */
    @Test
    void createCourse_Success_WhenHikeIsOptimized() {
        // Given : La randonnée ne requiert pas d'optimisation (getOptimize == false)
        mockHike.setOptimize(false); // Utilisation du setter classique

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        CourseResponseDto result = courseService.createCourse(mockDto, 10L);

        // Then
        assertNotNull(result);
        verify(courseRepository).save(any(Course.class));
    }

    /**
     * Teste l'échec de la création d'une session de marche.
     * Vérifie qu'une SecurityException est levée et qu'aucune sauvegarde n'est
     * effectuée en base de données si la randonnée n'a pas encore été optimisée
     * (le flag optimize est à true).
     */
    @Test
    void createCourse_ThrowsException_WhenHikeIsNotOptimized() {
        // Given : La randonnée n'est pas optimisée (getOptimize == false)
        mockHike.setOptimize(false); // Utilisation du setter classique

        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then
        SecurityException ex = assertThrows(SecurityException.class,
                () -> courseService.createCourse(mockDto, 10L)
        );

        assertTrue(ex.getMessage().contains("Vous devez d'abord vérifier et optimiser les sacs"));

        // On s'assure qu'aucune sauvegarde n'a été tentée en base de données
        verify(courseRepository, never()).save(any(Course.class));
    }

    /**
     * Vérifie le rejet de la création si l'ID de la randonnée parente est absent.
     */
    @Test
    void createCourse_NullHikeId_ThrowsException() {
        // Given : Un DTO incomplet (sans référence à la randonnée SQL)
        when(mockDto.getHikeId()).thenReturn(null);

        // When & Then : Une IllegalArgumentException doit être levée
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> courseService.createCourse(mockDto, 10L));
        assertTrue(ex.getMessage().contains("sans l'ID de la randonnée"));
    }

    /**
     * Vérifie la sécurité : empêche un utilisateur de démarrer un parcours sur la randonnée d'un autre.
     */
    @Test
    void createCourse_WrongUser_ThrowsSecurityException() {
        // Given : La randonnée appartient à l'utilisateur ID=10
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'utilisateur ID=99 tente de lancer le parcours -> Accès refusé
        SecurityException ex = assertThrows(SecurityException.class,
                () -> courseService.createCourse(mockDto, 99L));
        assertTrue(ex.getMessage().contains("Accès refusé"));
    }

    /**
     * Vérifie qu'il est impossible de créer un parcours sans fournir le premier point GPS (départ).
     */
    @Test
    void createCourse_NoInitialCoordinate_ThrowsException() {
        // Given : Un DTO dont la liste des coordonnées (path) est vide
        when(mockDto.getPath()).thenReturn(new ArrayList<>());
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : Le système bloque la création car il ne peut pas définir le point de départ
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> courseService.createCourse(mockDto, 10L));
    }

    // ==========================================
    // TESTS : MISE À JOUR (AJOUT DE POINTS)
    // ==========================================

    /**
     * Teste l'ajout d'un lot de coordonnées avec succès.
     * Vérifie que les points sont bien ajoutés au parcours et sauvegardés en base
     * lorsque la randonnée associée a déjà été optimisée (le flag optimize est à false).
     */
    @Test
    void addPointsToCourse_Success_WhenHikeIsOptimized() {
        // Given : Configuration des données d'entrée
        String courseId = "course-123";
        Long userId = 10L;

        // Utilisation du constructeur avec paramètres
        CourseResponseDto.CoordinateDto point = new CourseResponseDto.CoordinateDto(45.0, 3.0);
        List<CourseResponseDto.CoordinateDto> newPoints = List.of(point);

        // Configuration du mock Course
        Course mockCourse = mock(Course.class);
        when(mockCourse.getHikeId()).thenReturn(100L);

        // La randonnée ne requiert pas d'optimisation
        mockHike.setOptimize(false);

        // Simulation des retours de la base de données
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // Mock de la méthode mapToDto
        when(courseRepository.save(any(Course.class))).thenReturn(mockCourse);

        // When : L'utilisateur tente d'ajouter les points
        courseService.addPointsToCourse(courseId, newPoints, userId);

        // Then : Le point est ajouté et le parcours est sauvegardé
        verify(mockCourse, times(1)).addCoordinate(45.0, 3.0);
        verify(courseRepository).save(mockCourse);
    }

    /**
     * Teste l'échec de l'ajout de coordonnées au parcours.
     * Vérifie qu'une SecurityException est levée et qu'aucune sauvegarde n'est
     * effectuée en base de données si la randonnée n'a pas encore été optimisée
     * (le flag optimize est à true).
     */
    @Test
    void addPointsToCourse_ThrowsException_WhenHikeIsNotOptimized() {
        // Given : Configuration des données d'entrée
        String courseId = "course-123";
        Long userId = 10L;
        List<CourseResponseDto.CoordinateDto> newPoints = new ArrayList<>(); // Contenu peu importe, ça doit bloquer avant

        // Configuration du mock Course
        Course mockCourse = mock(Course.class);
        when(mockCourse.getHikeId()).thenReturn(100L);

        // La randonnée n'est PAS optimisée (getOptimize == true)
        mockHike.setOptimize(true);

        // Simulation des retours de la base de données
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'exception de sécurité doit être levée
        SecurityException ex = assertThrows(SecurityException.class,
                () -> courseService.addPointsToCourse(courseId, newPoints, userId)
        );

        // Vérification du message d'erreur
        assertTrue(ex.getMessage().contains("Vous devez d'abord vérifier et optimiser les sacs"));

        // Vérification cruciale : on s'assure que rien n'a été ajouté ni sauvegardé par erreur
        verify(mockCourse, never()).addCoordinate(anyDouble(), anyDouble());
        verify(courseRepository, never()).save(any(Course.class));
    }

    // ==========================================
    // TESTS : CLÔTURE ET GESTION D'ÉTAT
    // ==========================================

    /**
     * Teste la fin d'une randonnée et la création automatique du POI d'arrivée.
     */
    @Test
    void finishCourse_Success() {
        // Given : Un parcours en cours
        when(courseRepository.findById("mongo-id-123")).thenReturn(Optional.of(mockCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        // When : L'utilisateur signale la fin de sa marche
        Course result = courseService.finishCourse("mongo-id-123");

        // Then : Le statut passe à terminé, la pause est désactivée, et l'arrivée est générée
        assertTrue(result.isFinished());
        assertFalse(result.isPaused());
        assertNotNull(result.getArrivee(), "Le point d'arrivée doit être généré à partir du dernier point GPS");
        verify(courseRepository).save(mockCourse);
    }

    /**
     * Vérifie l'idempotence : on ne peut pas terminer un parcours déjà archivé.
     */
    @Test
    void finishCourse_AlreadyFinished_ThrowsException() {
        // Given : Le parcours est déjà marqué comme terminé
        mockCourse.setFinished(true);
        when(courseRepository.findById("mongo-id-123")).thenReturn(Optional.of(mockCourse));

        // When & Then : La tentative de clôture est rejetée
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.finishCourse("mongo-id-123"));
        assertEquals("Action illégale : Cette course est déjà terminée.", ex.getMessage());
    }

    /**
     * Teste la mise en pause (ou la reprise) du suivi GPS.
     */
    @Test
    void setPauseState_ToggleSuccess() {
        // Given : Un parcours actif (non en pause)
        assertFalse(mockCourse.isPaused());
        when(courseRepository.findById("mongo-id-123")).thenReturn(Optional.of(mockCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        // When : On déclenche le bouton pause
        Course result = courseService.setPauseState("mongo-id-123");

        // Then : L'état a bien basculé à "True" (en pause)
        assertTrue(result.isPaused());
    }

    /**
     * Vérifie qu'il est impossible de modifier l'état (pause) d'un historique de marche.
     */
    @Test
    void setPauseState_AlreadyFinished_ThrowsException() {
        // Given : Le parcours fait partie des archives (terminé)
        mockCourse.setFinished(true);
        when(courseRepository.findById("mongo-id-123")).thenReturn(Optional.of(mockCourse));

        // When & Then : Impossible de basculer l'état de pause
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.setPauseState("mongo-id-123"));
        assertEquals("Action illégale : Impossible de modifier une course terminée.", ex.getMessage());
    }
}