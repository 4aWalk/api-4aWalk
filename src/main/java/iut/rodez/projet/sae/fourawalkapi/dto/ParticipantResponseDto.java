package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

public class ParticipantResponseDto {
    private Long id;
    private String prenom;
    private String nom;
    private int age;
    private Level niveau;
    private Morphology morphologie;
    private boolean creator;
    private Long creatorId;
    private int besoinKcal;
    private int besoinEauLitre;
    private double capaciteEmportMaxKg;

    private BackpackResponseDto backpack; // L'objet DTO imbriqué

    // Infos calculées pour le monitoring
    private double chargeActuelleKg;
    private boolean enSurcharge;

    public ParticipantResponseDto(Participant p) {
        this.id = p.getId();
        this.prenom = p.getPrenom();
        this.nom = p.getNom();
        this.age = p.getAge();
        this.niveau = p.getNiveau();
        this.morphologie = p.getMorphologie();
        this.creator = p.getCreator();
        this.creatorId = p.getCreatorId();
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
    public String getPrenom() { return prenom; }
    public String getNom() { return nom; }
    public int getAge() { return age; }
    public Level getNiveau() { return niveau; }
    public Morphology getMorphologie() { return morphologie; }
    public boolean getIsCreator() { return creator; }
    public Long getCreatorId() { return creatorId; }
    public int getBesoinKcal() { return besoinKcal; }
    public int getBesoinEauLitre() { return besoinEauLitre; }
    public double getCapaciteEmportMaxKg() { return capaciteEmportMaxKg; }
    public BackpackResponseDto getBackpack() { return backpack; } // Getter pour l'objet sac
    public double getChargeActuelleKg() { return chargeActuelleKg; }
    public boolean getIsEnSurcharge() { return enSurcharge; }
}