package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
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
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        EquipmentItem item = equipmentRepository.findById(equipId).orElseThrow();
        hike.getEquipmentRequired().add(item);
        hikeRepository.save(hike);
    }

    @Transactional
    public void removeEquipmentFromHike(Long hikeId, Long equipId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        hike.getEquipmentRequired().removeIf(e -> e.getId().equals(equipId));
        hikeRepository.save(hike);
    }
}