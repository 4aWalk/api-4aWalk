package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class HikeResponseDto {
    private Long id;
    private String libelle;
    private String depart;
    private String arrivee;
    private int dureeJours;
    private UserResponseDto creator;
    private Set<ParticipantResponseDto> participants;

    public HikeResponseDto(Hike hike) {
        this.id = hike.getId();
        this.libelle = hike.getLibelle();
        this.depart = hike.getDepart();
        this.arrivee = hike.getArrivee();
        this.dureeJours = hike.getDureeJours();
        if (hike.getCreator() != null) {
            this.creator = new UserResponseDto(hike.getCreator());
        }
        this.participants = hike.getParticipants().stream()
                .map(ParticipantResponseDto::new)
                .collect(Collectors.toSet());
    }
    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public String getDepart() { return depart; }
    public String getArrivee() { return arrivee; }
    public int getDureeJours() { return dureeJours; }
    public UserResponseDto getCreator() { return creator; }
    public Set<ParticipantResponseDto> getParticipants() { return participants; }
}
