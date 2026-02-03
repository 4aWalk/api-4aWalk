package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

public class ParticipantResponseDto {
    private Long id;
    private int age;
    private Level niveau;
    private Morphology morphologie;
    private boolean creator;
    private int besoinKcal;
    private int besoinEauLitre;
    private double capaciteEmportMaxKg;

    private BackpackResponseDto backpack; // L'objet DTO imbriqué

    // Infos calculées pour le monitoring
    private double chargeActuelleKg;
    private boolean enSurcharge;

    public ParticipantResponseDto(Participant p) {
        this.id = p.getId();
        this.age = p.getAge();
        this.niveau = p.getNiveau();
        this.morphologie = p.getMorphologie();
        this.creator = p.getCreator();
        this.besoinKcal = p.getBesoinKcal();
        this.besoinEauLitre = p.getBesoinEauLitre();
        this.capaciteEmportMaxKg = p.getCapaciteEmportMaxKg();

        // Gestion du Sac à dos
        if (p.getBackpack() != null) {
            // CORRECTION ICI : On passe l'entité entière au constructeur du DTO
            this.backpack = new BackpackResponseDto(p.getBackpack());

            // On récupère les valeurs directement depuis notre DTO tout neuf
            this.chargeActuelleKg = this.backpack.getPoidsActuelKg();

            // Calcul de la surcharge (Poids du sac > Capacité du PARTICIPANT)
            this.enSurcharge = this.chargeActuelleKg > this.capaciteEmportMaxKg;
        } else {
            this.backpack = null;
            this.chargeActuelleKg = 0.0;
            this.enSurcharge = false;
        }
    }

    // --- GETTERS ---
    public Long getId() { return id; }
    public int getAge() { return age; }
    public Level getNiveau() { return niveau; }
    public Morphology getMorphologie() { return morphologie; }
    public boolean isCreator() { return creator; }
    public int getBesoinKcal() { return besoinKcal; }
    public int getBesoinEauLitre() { return besoinEauLitre; }
    public double getCapaciteEmportMaxKg() { return capaciteEmportMaxKg; }
    public BackpackResponseDto getBackpack() { return backpack; } // Getter pour l'objet sac
    public double getChargeActuelleKg() { return chargeActuelleKg; }
    public boolean isEnSurcharge() { return enSurcharge; }
}