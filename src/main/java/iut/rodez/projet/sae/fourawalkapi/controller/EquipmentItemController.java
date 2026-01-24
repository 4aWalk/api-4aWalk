package iut.rodez.projet.sae.fourawalkapi.controller;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.service.EquipmentItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipments")
public class EquipmentItemController {

    private final EquipmentItemService equipmentService;

    public EquipmentItemController(EquipmentItemService es) {
        this.equipmentService = es;
    }

    @GetMapping
    public List<EquipmentItem> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    @PostMapping
    public EquipmentItem createEquipment(@RequestBody EquipmentItem item) {
        return equipmentService.createEquipment(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}