package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;

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
    private Long ownerId;
    private String ownerName;

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
        this.ownerId = null;
        this.ownerName = null;
    }

    /**
     * Mapper entity to dto avec Propriétaire (Utilisé pour l'affichage d'une Randonnée)
     * @param item équipement à mapper
     * @param owner participant à qui appartient l'équipement
     */
    public EquipmentResponseDto(EquipmentItem item, Participant owner) {
        this(item);

        if (owner != null) {
            this.ownerId = owner.getId();
            // On concatène le prénom et le nom pour faciliter la lecture au front
            this.ownerName = owner.getPrenom() + " " + owner.getNom();
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
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
}