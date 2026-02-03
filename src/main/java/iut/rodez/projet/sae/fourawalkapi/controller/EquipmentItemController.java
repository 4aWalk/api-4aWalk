package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.dto.EquipmentResponseDto;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.service.EquipmentItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equipments")
public class EquipmentItemController {

    private final EquipmentItemService equipmentService;

    public EquipmentItemController(EquipmentItemService es) {
        this.equipmentService = es;
    }

    @GetMapping
    public List<EquipmentResponseDto> getAllEquipment() {
        // Transformation de la liste d'entités en liste de DTOs
        return equipmentService.getAllEquipment().stream()
                .map(EquipmentResponseDto::new) // Utilise ton constructeur DTO
                .collect(Collectors.toList());
    }

    @PostMapping
    public EquipmentResponseDto createEquipment(@RequestBody EquipmentItem item) {
        EquipmentItem savedItem = equipmentService.createEquipment(item);
        return new EquipmentResponseDto(savedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}