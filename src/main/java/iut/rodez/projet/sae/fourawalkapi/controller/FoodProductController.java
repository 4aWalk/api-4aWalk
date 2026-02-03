package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.controller.dto.FoodProductResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.service.FoodProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/foods")
public class FoodProductController {

    private final FoodProductService foodService;

    public FoodProductController(FoodProductService fs) {
        this.foodService = fs;
    }

    @GetMapping
    public List<FoodProductResponseDto> getAllFoods() {
        return foodService.getAllFoods().stream()
                .map(FoodProductResponseDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping
    public FoodProductResponseDto createFood(@RequestBody FoodProduct food) {
        FoodProduct savedFood = foodService.createFood(food);
        return new FoodProductResponseDto(savedFood);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}