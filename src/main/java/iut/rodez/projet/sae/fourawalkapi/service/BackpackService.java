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
            // 1. Pour l'équipement (ManyToMany) : on vide juste la liste
            // Cela va supprimer les lignes dans la table de jointure 'backpack_equipment'
            backpack.getEquipmentItems().clear();

            // 2. Pour la nourriture (OneToMany vers BackpackFoodItem) :
            // Si tu as mis cascade = CascadeType.ALL + orphanRemoval = true,
            // vider la liste supprimera les items de la base.
            backpack.getFoodItems().clear();

            // 3. On sauvegarde le sac vidé
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