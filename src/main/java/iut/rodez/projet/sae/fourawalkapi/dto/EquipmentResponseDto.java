package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;

public class EquipmentResponseDto {
    private Long id;
    private String nom;
    private String description;
    private double masseGrammes;
    private int nbItem;
    private TypeEquipment type;
    private double masseAVide;

    public EquipmentResponseDto(EquipmentItem item) {
        this.id = item.getId();
        this.nom = item.getNom();
        this.description = item.getDescription();
        this.masseGrammes = item.getMasseGrammes();
        this.nbItem = item.getNbItem();
        this.type = item.getType();
        this.masseAVide = item.getMasseAVide();
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public double getMasseGrammes() { return masseGrammes; }
    public int getNbItem() { return nbItem; }
    public TypeEquipment getType() { return type; }
    public double getMasseAVide() { return masseAVide; }
}