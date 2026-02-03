package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import java.util.Set;
import java.util.stream.Collectors;

public class HikeResponseDto {
    private Long id;
    private String libelle;
    private int dureeJours;

    // Nouveaux types DTO pour le départ et l'arrivée
    private PointOfInterestResponseDto depart;
    private PointOfInterestResponseDto arrivee;

    private UserResponseDto creator;
    private Set<ParticipantResponseDto> participants;
    private Set<FoodProductResponseDto> foodCatalogue;
    private Set<EquipmentResponseDto> equipmentRequired;

    public HikeResponseDto(Hike hike) {
        this.id = hike.getId();
        this.libelle = hike.getLibelle();
        this.dureeJours = hike.getDureeJours();

        // Mapping des POI
        this.depart = new PointOfInterestResponseDto(hike.getDepart());
        this.arrivee = new PointOfInterestResponseDto(hike.getArrivee());

        if (hike.getCreator() != null) {
            this.creator = new UserResponseDto(hike.getCreator());
        }

        // Mapping des collections avec Streams
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

    // Getters (Indispensables pour éviter l'erreur "No acceptable representation")
    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public int getDureeJours() { return dureeJours; }
    public PointOfInterestResponseDto getDepart() { return depart; }
    public PointOfInterestResponseDto getArrivee() { return arrivee; }
    public UserResponseDto getCreator() { return creator; }
    public Set<ParticipantResponseDto> getParticipants() { return participants; }
    public Set<FoodProductResponseDto> getFoodCatalogue() { return foodCatalogue; }
    public Set<EquipmentResponseDto> getEquipmentRequired() { return equipmentRequired; }
}