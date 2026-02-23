package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.FoodProductRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour FoodService.
 * Valide rigoureusement le respect des bornes nutritionnelles et physiques (masse/calories),
 * ainsi que la sécurité d'accès lors de l'ajout/retrait de provisions d'une randonnée.
 */
class FoodServiceTest {

    private FoodProductRepository foodRepository;
    private HikeRepository hikeRepository;
    private FoodService foodService;

    private Hike mockHike;
    private FoodProduct validFood;

    /**
     * Initialisation du contexte de test avant chaque méthode.
     */
    @BeforeEach
    void setUp() {
        foodRepository = mock(FoodProductRepository.class);
        hikeRepository = mock(HikeRepository.class);
        foodService = new FoodService(foodRepository, hikeRepository);

        // --- Utilisateur propriétaire ---
        User creatorUser = new User();
        creatorUser.setId(10L);

        // --- Randonnée cible (Mockée pour tester le Domain Driven Design) ---
        mockHike = mock(Hike.class);
        when(mockHike.getId()).thenReturn(100L);
        when(mockHike.getCreator()).thenReturn(creatorUser);

        // --- Aliment standard valide ---
        validFood = new FoodProduct();
        validFood.setId(300L);
        validFood.setMasseGrammes(500); // 500g (Valide)
        validFood.setApportNutritionnelKcal(1500); // 1500 kcal (Valide)
    }

    // ==========================================
    // TESTS : CRÉATION ET VALIDATION (RÈGLES MÉTIER)
    // ==========================================

    /**
     * Teste la création d'un aliment parfaitement dans les normes.
     */
    @Test
    void createFood_Success() {
        // Given : Un aliment valide préparé dans le setup()
        when(foodRepository.save(any(FoodProduct.class))).thenReturn(validFood);

        // When : Le service tente de le créer
        FoodProduct result = foodService.createFood(validFood);

        // Then : La validation est passée et le repository a été appelé
        assertNotNull(result);
        verify(foodRepository).save(validFood);
    }

    /**
     * Vérifie la limite basse de la masse (interdit < 50g).
     */
    @Test
    void createFood_MassTooLow_ThrowsException() {
        // Given : Un aliment pesant 49g (limite absolue à 50g)
        FoodProduct invalidFood = new FoodProduct();
        invalidFood.setMasseGrammes(49);
        invalidFood.setApportNutritionnelKcal(1000); // Les kcal sont valides pour isoler l'erreur

        // When & Then : La création échoue
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.createFood(invalidFood));
        assertTrue(ex.getMessage().contains("entre 50g et 5kg"));

        // Sécurité : Vérifie que le code s'est arrêté avant l'insertion en BDD
        verify(foodRepository, never()).save(any());
    }

    /**
     * Vérifie la limite haute de la masse (interdit > 5000g).
     */
    @Test
    void createFood_MassTooHigh_ThrowsException() {
        // Given : Un aliment trop lourd (5001g)
        FoodProduct invalidFood = new FoodProduct();
        invalidFood.setMasseGrammes(5001);
        invalidFood.setApportNutritionnelKcal(1000);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.createFood(invalidFood));
        assertTrue(ex.getMessage().contains("entre 50g et 5kg"));
    }

    /**
     * Vérifie la limite basse des calories (interdit < 50 kcal).
     */
    @Test
    void createFood_KcalTooLow_ThrowsException() {
        // Given : Un aliment valide en poids, mais hypocalorique (49 kcal)
        FoodProduct invalidFood = new FoodProduct();
        invalidFood.setMasseGrammes(500);
        invalidFood.setApportNutritionnelKcal(49); // Limite à 50 kcal

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.createFood(invalidFood));
        assertTrue(ex.getMessage().contains("entre 50 et 3000 kcal"));
    }

    /**
     * Vérifie la limite haute des calories (interdit > 3000 kcal).
     */
    @Test
    void createFood_KcalTooHigh_ThrowsException() {
        // Given : Un aliment valide en poids, mais hypercalorique (3001 kcal)
        FoodProduct invalidFood = new FoodProduct();
        invalidFood.setMasseGrammes(500);
        invalidFood.setApportNutritionnelKcal(3001);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.createFood(invalidFood));
        assertTrue(ex.getMessage().contains("entre 50 et 3000 kcal"));
    }

    // ==========================================
    // TESTS : LECTURE ET SUPPRESSION
    // ==========================================

    @Test
    void getAllFoods_Success() {
        // Given : Le catalogue contient des produits
        when(foodRepository.findAll()).thenReturn(List.of(validFood, new FoodProduct()));

        // When
        List<FoodProduct> result = foodService.getAllFoods();

        // Then
        assertEquals(2, result.size());
        verify(foodRepository).findAll();
    }

    @Test
    void deleteFood_Success() {
        // Given l'ID d'un produit
        // When
        foodService.deleteFood(300L);
        // Then
        verify(foodRepository).deleteById(300L);
    }

    // ==========================================
    // TESTS : AJOUT AUX PROVISIONS (ASSOCIATION)
    // ==========================================

    /**
     * Teste l'ajout nominal d'un aliment à une randonnée par son propriétaire.
     */
    @Test
    void addFoodToHike_Success() {
        // Given : Randonnée et produit existent
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(foodRepository.findById(300L)).thenReturn(Optional.of(validFood));

        // When : L'utilisateur ID=10 ajoute l'aliment
        foodService.addFoodToHike(100L, 300L, 10L);

        // Then : Délégation à l'entité (DDD) et sauvegarde
        verify(mockHike).addFood(validFood);
        verify(hikeRepository).save(mockHike);
    }

    /**
     * Vérifie la prévention des failles de sécurité (IDOR) :
     * Empêche l'utilisateur B de modifier le sac de l'utilisateur A.
     */
    @Test
    void addFoodToHike_WrongUserAccess_ThrowsException() {
        // Given : La randonnée appartient à l'utilisateur ID=10
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : L'utilisateur ID=99 tente l'ajout -> Bloqué
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.addFoodToHike(100L, 300L, 99L));
        assertEquals("Accès refusé : Vous n'êtes pas le propriétaire de cette randonnée", ex.getMessage());

        verify(mockHike, never()).addFood(any()); // L'entité n'est jamais touchée
    }

    @Test
    void addFoodToHike_FoodNotFound_ThrowsException() {
        // Given : Randonnée OK, mais l'aliment ciblé n'existe pas
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(foodRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.addFoodToHike(100L, 999L, 10L));
        assertEquals("Aliment introuvable", ex.getMessage());
    }

    // ==========================================
    // TESTS : RETRAIT DES PROVISIONS (DISSOCIATION)
    // ==========================================

    @Test
    void removeFoodFromHike_Success() {
        // Given : Randonnée et produit existants
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));
        when(foodRepository.findById(300L)).thenReturn(Optional.of(validFood));

        // When : Retrait initié par le créateur
        foodService.removeFoodFromHike(100L, 300L, 10L);

        // Then
        verify(mockHike).removeFood(validFood);
        verify(hikeRepository).save(mockHike);
    }

    @Test
    void removeFoodFromHike_WrongUserAccess_ThrowsException() {
        // Given
        when(hikeRepository.findById(100L)).thenReturn(Optional.of(mockHike));

        // When & Then : Tentative de vol de nourriture par l'utilisateur 99 !
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> foodService.removeFoodFromHike(100L, 300L, 99L));
        assertEquals("Accès refusé", ex.getMessage());
    }
}