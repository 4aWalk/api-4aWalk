package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.FoodProductRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodProductService {

    private final FoodProductRepository foodRepository;
    private final HikeRepository hikeRepository;

    public FoodProductService(FoodProductRepository fr, HikeRepository hr) {
        this.foodRepository = fr;
        this.hikeRepository = hr;
    }

    public List<FoodProduct> getAllFoods() {
        return foodRepository.findAll();
    }

    public FoodProduct createFood(FoodProduct food) {
        // Validation des règles métiers avant insertion
        validateFoodRules(food);
        return foodRepository.save(food);
    }

    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }

    @Transactional
    public void addFoodToHike(Long hikeId, Long foodId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow(() -> new RuntimeException("Randonnée introuvable"));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        FoodProduct fp = foodRepository.findById(foodId).orElseThrow(() -> new RuntimeException("Aliment introuvable"));

        hike.getFoodCatalogue().add(fp);
        hikeRepository.save(hike);
    }

    @Transactional
    public void removeFoodFromHike(Long hikeId, Long foodId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow(() -> new RuntimeException("Randonnée introuvable"));
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        hike.getFoodCatalogue().removeIf(f -> f.getId().equals(foodId));
        hikeRepository.save(hike);
    }

    /**
     * Valide les contraintes métiers sur la nourriture
     */
    private void validateFoodRules(FoodProduct f) {
        // Poids : 50g - 5000g
        if (f.getMasseGrammes() < 50 || f.getMasseGrammes() > 5000) {
            throw new RuntimeException("La masse de la nourriture doit être comprise entre 50g et 5kg");
        }

        // Calories : 50 kcal - 3000 kcal
        if (f.getApportNutritionnelKcal() < 50 || f.getApportNutritionnelKcal() > 3000) {
            throw new RuntimeException("L'apport calorique doit être compris entre 50 et 3000 kcal");
        }

    }
}