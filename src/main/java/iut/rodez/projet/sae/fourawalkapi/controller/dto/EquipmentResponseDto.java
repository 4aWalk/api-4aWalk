package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;

public class EquipmentResponseDto {
    private Long id;
    private String nom;
    private double masseGrammes;
    private boolean permetRepos;
    private String description;

    public EquipmentResponseDto(EquipmentItem item) {
        this.id = item.getId();
        this.nom = item.getNom();
        this.masseGrammes = item.getMasseGrammes();
        this.permetRepos = item.isPermetRepos();
        this.description = item.getDescription();
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public double getMasseGrammes() { return masseGrammes; }
    public boolean isPermetRepos() { return permetRepos; }
    public String getDescription() { return description; }
}