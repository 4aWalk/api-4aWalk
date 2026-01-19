package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.repository.FoodItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.EquipementItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.BackpackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BackpackService {

    private final BackpackRepository backpackRepository;
    private final EquipementItemRepository equipementRepository;
    private final FoodItemRepository foodRepository;

    public BackpackService(BackpackRepository backpackRepository, EquipementItemRepository equipementRepository, FoodItemRepository foodRepository) {
        this.backpackRepository = backpackRepository;
        this.equipementRepository = equipementRepository;
        this.foodRepository = foodRepository;
    }

    public Optional<Backpack> getBackpackByParticipant(Long idParticipant) {
        return backpackRepository.findByParticipantId(idParticipant);
    }

    @Transactional
    public void clearBackpack(Long idParticipant) {
        backpackRepository.findByParticipantId(idParticipant).ifPresent(backpack -> {
            foodRepository.deleteByBackpackId(backpack.getId());
        });
        backpackRepository.findByParticipantId(idParticipant).ifPresent(backpack -> {
            equipementRepository.deleteByBackpackId(backpack.getId());
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