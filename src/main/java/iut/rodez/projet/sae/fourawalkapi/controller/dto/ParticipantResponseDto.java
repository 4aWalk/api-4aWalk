package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class ParticipantResponseDto {
    private Long id;
    private String nomComplet;
    private int age;
    private Level niveau;
    private Morphology morphologie;
    private int besoinKcal;
    private int besoinEau;
    private double capaciteEmportMaxKg;

    public ParticipantResponseDto(Participant participant) {
        this.id =  participant.getId();
        this.nomComplet = participant.getNomComplet();
        this.age = participant.getAge();
        this.niveau = participant.getNiveau();
        this.morphologie = participant.getMorphologie();
        this.besoinEau = participant.getBesoinEauLitre();
        this.capaciteEmportMaxKg = participant.getCapaciteEmportMaxKg();
    }

    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public int getAge() { return age; }
    public Level getNiveau() { return niveau; }
    public Morphology getMorphologie() { return morphologie; }
    public int getBesoinEau() { return besoinEau; }
    public double getCapaciteEmportMaxKg() { return capaciteEmportMaxKg; }
}
