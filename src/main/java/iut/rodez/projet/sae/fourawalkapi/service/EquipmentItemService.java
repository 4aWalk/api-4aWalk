package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentItemService {

    private final EquipmentItemRepository equipmentRepository;
    private final HikeRepository hikeRepository;

    public EquipmentItemService(EquipmentItemRepository er, HikeRepository hr) {
        this.equipmentRepository = er;
        this.hikeRepository = hr;
    }

    public List<EquipmentItem> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public EquipmentItem createEquipment(EquipmentItem item) {
        return equipmentRepository.save(item);
    }

    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Transactional
    public void addEquipmentToHike(Long hikeId, Long equipId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }

        EquipmentItem item = equipmentRepository.findById(equipId)
                .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

        hike.addEquipment(item);

        hikeRepository.save(hike);
    }

    @Transactional
    public void removeEquipmentFromHike(Long hikeId, Long equipId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }

        // 1. On doit récupérer l'item pour connaître son TYPE (Soin, Repos...)
        // afin de savoir dans quel groupe chercher.
        EquipmentItem itemToRemove = equipmentRepository.findById(equipId)
                .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

        // 2. On récupère le bon groupe dans la Map
        GroupEquipment group = hike.getEquipmentGroups().get(itemToRemove.getType());

        if (group != null) {
            // 3. On supprime l'item de la liste du groupe
            group.getItems().removeIf(item -> item.getId().equals(equipId));

        }

        hikeRepository.save(hike);
    }

    /**
     * Récupère la liste déjà optimisée (triée) pour un type donné
     */
    public List<EquipmentItem> getEquipmentByType(Long hikeId, TypeEquipment type) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        return hike.getOptimizedList(type);
    }
}