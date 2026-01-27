package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;

public class EquipmentResponseDto {
    private Long id;
    private String nom;
    private double masseGrammes;
    private int nbItem;
    private TypeEquipment type;

    public EquipmentResponseDto(EquipmentItem item) {
        this.id = item.getId();
        this.nom = item.getNom();
        this.masseGrammes = item.getMasseGrammes();
        this.nbItem = item.getNbItem();
        this.type = item.getType();
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public double getMasseGrammes() { return masseGrammes; }
    public int getNbItem() { return nbItem; }
    public TypeEquipment getType() { return type; }
}