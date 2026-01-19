package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;

import java.util.List;

public class HikeResponseDto {
    private Long id;
    private String libelle;
    private String depart;
    private String arrivee;
    private int dureeJours;
    private User creator;
    private List<Participant> participants;

    public HikeResponseDto(Hike hike) {
        this.id = hike.getId();
        this.libelle = hike.getLibelle();
        this.depart = hike.getDepart();
        this.arrivee = hike.getArrivee();
        this.dureeJours = hike.getDureeJours();
        this.creator = hike.getCreator();
        this.participants = hike.getParticipants();
    }
}
