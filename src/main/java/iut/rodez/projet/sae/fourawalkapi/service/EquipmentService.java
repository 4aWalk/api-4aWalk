package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.EquipmentItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentItemRepository equipmentItemRepository;

    public EquipmentService(EquipmentItemRepository equipmentItemRepository) {
        this.equipmentItemRepository = equipmentItemRepository;
    }

    public List<EquipmentItem> findAll() {
        return equipmentItemRepository.findAll();
    }

    public void validateEquipment(EquipmentItem item) {
        if (item.getMasseGrammes() <= 0) {
            throw new IllegalArgumentException("L'équipement " + item.getNom() + " doit avoir un poids positif.");
        }
    }
}
