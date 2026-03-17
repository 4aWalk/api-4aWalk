package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HikeResponseDto {
    private Long id;
    private String libelle;
    private int dureeJours;
    private PointOfInterestResponseDto depart;
    private PointOfInterestResponseDto arrivee;
    private Set<PointOfInterestResponseDto> points;
    private UserResponseDto creator;
    private Set<ParticipantResponseDto> participants;
    private Set<FoodProductResponseDto> foodCatalogue;
    private Map<TypeEquipment, GroupEquipmentResponseDto> equipmentGroups;
    private boolean optimize;

    public HikeResponseDto(Hike hike, Map<Long, List<Participant>> equipmentOwners) {
        this.id = hike.getId();
        this.libelle = hike.getLibelle();
        this.dureeJours = hike.getDureeJours();
        this.depart = new PointOfInterestResponseDto(hike.getDepart());
        this.arrivee = new PointOfInterestResponseDto(hike.getArrivee());
        this.points = hike.getOptionalPoints().stream()
                .map(PointOfInterestResponseDto::new)
                .collect(Collectors.toSet());

        if (hike.getCreator() != null) {
            this.creator = new UserResponseDto(hike.getCreator());
        }

        this.participants = hike.getParticipants().stream()
                .map(participant -> new ParticipantResponseDto(participant, equipmentOwners))
                .collect(Collectors.toSet());

        this.foodCatalogue = hike.getFoodCatalogue().stream()
                .map(FoodProductResponseDto::new)
                .collect(Collectors.toSet());

        if (hike.getEquipmentGroups() != null) {
            this.equipmentGroups = hike.getEquipmentGroups().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new GroupEquipmentResponseDto(entry.getValue(), equipmentOwners)
                    ));
        }
        this.optimize = hike.getOptimize();
    }

    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public int getDureeJours() { return dureeJours; }
    public PointOfInterestResponseDto getDepart() { return depart; }
    public PointOfInterestResponseDto getArrivee() { return arrivee; }
    public UserResponseDto getCreator() { return creator; }
    public Set<PointOfInterestResponseDto> getPoints() { return points; }
    public Set<ParticipantResponseDto> getParticipants() { return participants; }
    public Set<FoodProductResponseDto> getFoodCatalogue() { return foodCatalogue; }
    public Map<TypeEquipment, GroupEquipmentResponseDto> getEquipmentGroups() { return equipmentGroups; }
    public boolean isOptimize() { return optimize; }
}