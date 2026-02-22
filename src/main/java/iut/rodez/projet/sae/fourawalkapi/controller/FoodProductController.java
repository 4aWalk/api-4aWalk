package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.FoodProductResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlleur de tous les endpoints permettant de gérer la nourriture
 */
@RestController
@RequestMapping("/foods")
public class FoodProductController {

    private final FoodService foodService;

    /**
     * Injection de dépendance
     * @param fs nourriture service
     */
    public FoodProductController(FoodService fs) {
        this.foodService = fs;
    }

    /**
     * Récupération de l'ensemble de la nourriture du catalogue
     * @return la liste de toutes les nourritures existantes
     */
    @GetMapping
    public List<FoodProductResponseDto> getAllFoods() {
        return foodService.getAllFoods().stream()
                .map(FoodProductResponseDto::new)
                .toList();
    }

    /**
     * Création d'une nouvelle nourriture
     * @param food La nourriture à créer
     * @return La nourriture créer
     */
    @PostMapping
    public FoodProductResponseDto createFood(@RequestBody FoodProduct food) {
        FoodProduct savedFood = foodService.createFood(food);
        return new FoodProductResponseDto(savedFood);
    }

    /**
     * Supression d'un nourriture
     * @param id L'identifiant de la nourriture à supprimer
     * @return Code retour de la suppression
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}