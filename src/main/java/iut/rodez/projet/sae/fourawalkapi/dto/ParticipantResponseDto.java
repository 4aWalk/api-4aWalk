package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

/**
 * Data transfert object utilisé dans les communications d'objet participant avec le client
 */
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
    private BackpackResponseDto backpack;

    /**
     * Mapper entity to dto
     * @param participant participant à mapper
     */
    public ParticipantResponseDto(Participant participant) {
        this.id = participant.getId();
        this.prenom = participant.getPrenom();
        this.nom = participant.getNom();
        this.age = participant.getAge();
        this.niveau = participant.getNiveau();
        this.morphologie = participant.getMorphologie();
        this.creator = participant.getCreator();
        this.creatorId = participant.getCreatorId();
        this.besoinKcal = participant.getBesoinKcal();
        this.besoinEauLitre = participant.getBesoinEauLitre();
        this.capaciteEmportMaxKg = participant.getCapaciteEmportMaxKg();
        if (participant.getBackpack() != null) {
            this.backpack = new BackpackResponseDto(participant.getBackpack());
        } else {
            this.backpack = null;
        }
    }

    // Getters
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
    public BackpackResponseDto getBackpack() { return backpack; }
}