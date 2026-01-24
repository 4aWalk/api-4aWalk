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
        return foodRepository.save(food);
    }

    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }

    @Transactional
    public void addFoodToHike(Long hikeId, Long foodId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        FoodProduct fp = foodRepository.findById(foodId).orElseThrow();
        hike.getFoodCatalogue().add(fp);
        hikeRepository.save(hike);
    }

    @Transactional
    public void removeFoodFromHike(Long hikeId, Long foodId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        hike.getFoodCatalogue().removeIf(f -> f.getId().equals(foodId));
        hikeRepository.save(hike);
    }
}