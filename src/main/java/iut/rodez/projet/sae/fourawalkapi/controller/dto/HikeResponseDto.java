package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
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
    private Set<FoodProductResponseDto> foodCatalogue;
    private Set<EquipmentResponseDto> equipmentRequired;

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

        this.foodCatalogue = hike.getFoodCatalogue().stream()
                .map(FoodProductResponseDto::new)
                .collect(Collectors.toSet());

        this.equipmentRequired = hike.getEquipmentRequired().stream()
                .map(EquipmentResponseDto::new)
                .collect(Collectors.toSet());
    }

    // Getters pour Jackson
    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public String getDepart() { return depart; }
    public String getArrivee() { return arrivee; }
    public int getDureeJours() { return dureeJours; }
    public UserResponseDto getCreator() { return creator; }
    public Set<ParticipantResponseDto> getParticipants() { return participants; }
    public Set<FoodProductResponseDto> getFoodCatalogue() { return foodCatalogue; }
    public Set<EquipmentResponseDto> getEquipmentRequired() { return equipmentRequired; }
}