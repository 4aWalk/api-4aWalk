package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.FoodProductRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BackpackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BackpackService {

    private final BackpackRepository backpackRepository;
    private final EquipmentItemRepository equipementRepository;
    private final FoodProductRepository foodRepository;

    public BackpackService(BackpackRepository backpackRepository, EquipmentItemRepository equipementRepository, FoodProductRepository foodRepository) {
        this.backpackRepository = backpackRepository;
        this.equipementRepository = equipementRepository;
        this.foodRepository = foodRepository;
    }

    public Optional<Backpack> getBackpackByParticipant(Long idParticipant) {
        return backpackRepository.findByOwnerId(idParticipant);
    }

    @Transactional
    public void clearBackpack(Long idParticipant) {
        backpackRepository.findByOwnerId(idParticipant).ifPresent(backpack -> {
            backpack.getEquipmentItems().clear();

            backpack.getFoodItems().clear();
            backpackRepository.save(backpack);
        });
    }

    public FoodProduct addFood(FoodProduct food) {
        return foodRepository.save(food);
    }

    public EquipmentItem addEquipement(EquipmentItem equipement) {
        return equipementRepository.save(equipement);
    }

    public void deleteFood(Long idItem) {
        foodRepository.deleteById(idItem);
    }

    public void deleteEquipement(Long idItem) {
        equipementRepository.deleteById(idItem);
    }
}