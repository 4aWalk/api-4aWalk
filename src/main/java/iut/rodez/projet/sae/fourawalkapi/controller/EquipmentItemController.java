package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.EquipmentResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlleur gérant tous les endpoints d'équipements
 */
@RestController
@RequestMapping("/equipments")
public class EquipmentItemController {

    private final EquipmentService equipmentService;

    /**
     * Injection de dépendance
     * @param es equipement service
     */
    public EquipmentItemController(EquipmentService es) {
        this.equipmentService = es;
    }

    /**
     * Liste tous les équipements disponibles du catalogue
     * @return Liste de tous les équipements
     */
    @GetMapping
    public List<EquipmentResponseDto> getAllEquipment() {
        return equipmentService.getAllEquipment().stream()
                .map(EquipmentResponseDto::new)
                .toList();
    }

    /**
     * Création d'un nouvel équipement dans le catalogue
     * @param item nouvel équipement créer
     * @return l'équipement créer
     */
    @PostMapping
    public EquipmentResponseDto createEquipment(@RequestBody EquipmentItem item) {
        EquipmentItem savedItem = equipmentService.createEquipment(item);
        return new EquipmentResponseDto(savedItem);
    }

    /**
     * Suppression d'un équipement dans le catalogue
     * @param id identifiant de l'équipement à supprimer
     * @return Code retour de la suppression
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}