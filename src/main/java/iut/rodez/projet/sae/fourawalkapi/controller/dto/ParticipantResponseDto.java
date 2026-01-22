package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

public class ParticipantResponseDto {
    private Long id;
    private String nomComplet;
    private int age;
    private Level niveau;
    private Morphology morphologie;
    private int besoinKcal;
    private int besoinEauLitre;
    private double capaciteEmportMaxKg;

    // On ajoute les infos calculées pour le monitoring dans Postman
    private double chargeActuelleKg;
    private boolean enSurcharge;

    public ParticipantResponseDto(Participant p) {
        this.id = p.getId();
        this.nomComplet = p.getNomComplet();
        this.age = p.getAge();
        this.niveau = p.getNiveau();
        this.morphologie = p.getMorphologie();
        this.besoinKcal = p.getBesoinKcal();
        this.besoinEauLitre = p.getBesoinEauLitre();
        this.capaciteEmportMaxKg = p.getCapaciteEmportMaxKg();

        // Calculs basés sur la logique métier de l'entité
        if (p.getBackpack() != null) {
            this.chargeActuelleKg = p.getBackpack().getTotalMassKg();
            this.enSurcharge = p.isOverloaded();
        } else {
            this.chargeActuelleKg = 0.0;
            this.enSurcharge = false;
        }
    }

    // --- GETTERS (Cruciaux pour Jackson) ---
    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public int getAge() { return age; }
    public Level getNiveau() { return niveau; }
    public Morphology getMorphologie() { return morphologie; }
    public int getBesoinKcal() { return besoinKcal; }
    public int getBesoinEauLitre() { return besoinEauLitre; }
    public double getCapaciteEmportMaxKg() { return capaciteEmportMaxKg; }
    public double getChargeActuelleKg() { return chargeActuelleKg; }
    public boolean isEnSurcharge() { return enSurcharge; }
}