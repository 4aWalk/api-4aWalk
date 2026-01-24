package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.service.FoodProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
public class FoodProductController {

    private final FoodProductService foodService;

    public FoodProductController(FoodProductService fs) {
        this.foodService = fs;
    }

    @GetMapping
    public List<FoodProduct> getAllFoods() {
        return foodService.getAllFoods();
    }

    @PostMapping
    public FoodProduct createFood(@RequestBody FoodProduct food) {
        return foodService.createFood(food);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}