package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfert object utilisé dans les communications d'objet equipement avec le client
 */
public class EquipmentResponseDto {
    private Long id;
    private String nom;
    private String description;
    private double masseGrammes;
    private int nbItem;
    private TypeEquipment type;
    private double masseAVide;
    private List<Long> ownerIds;
    private List<String> ownerNames;

    /**
     * Mapper entity to dto (Utilisé pour le Catalogue global, sans propriétaire)
     * @param item équipement à mapper
     */
    public EquipmentResponseDto(EquipmentItem item) {
        this.id = item.getId();
        this.nom = item.getNom();
        this.description = item.getDescription();
        this.masseGrammes = item.getMasseGrammes();
        this.nbItem = item.getNbItem();
        this.type = item.getType();
        this.masseAVide = item.getMasseAVide();
        this.ownerIds = new ArrayList<>();
        this.ownerNames = new ArrayList<>();
    }

    /**
     * Mapper entity to dto avec Propriétaires (Utilisé pour l'affichage d'une Randonnée)
     * @param item équipement à mapper
     * @param owners liste des participants à qui appartient l'équipement
     */
    public EquipmentResponseDto(EquipmentItem item, List<Participant> owners) {
        this(item);

        if (owners != null && !owners.isEmpty()) {
            this.ownerIds = owners.stream()
                    .map(Participant::getId)
                    .toList();
            // On concatène le prénom et le nom pour faciliter la lecture au front
            this.ownerNames = owners.stream()
                    .map(o -> o.getPrenom() + " " + o.getNom())
                    .toList();
        }
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public double getMasseGrammes() { return masseGrammes; }
    public int getNbItem() { return nbItem; }
    public TypeEquipment getType() { return type; }
    public double getMasseAVide() { return masseAVide; }
    public List<Long> getOwnerIds() { return ownerIds; }
    public List<String> getOwnerNames() { return ownerNames; }
}