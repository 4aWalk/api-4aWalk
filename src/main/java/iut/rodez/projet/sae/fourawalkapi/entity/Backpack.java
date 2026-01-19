package iut.rodez.projet.sae.fourawalkapi.entity;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import jakarta.persistence.*;
import java.util.Set;

/** Représente le chargement du sac à dos d'un participant (Résultat de l'optimisation) */
@Entity
@Table(name = "backpacks")
public class Backpack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Poids total réel porté (pour vérification)
    private double totalMassKg;

    // Le propriétaire du sac (relation One-to-One)
    @OneToOne
    @JoinColumn(name = "participant_id")
    private Participant owner;

    // Contenu : Liste des produits alimentaires à emporter
    // Utiliser une table de jointure pour stocker les quantités
    @OneToMany(mappedBy = "backpack", cascade = CascadeType.ALL)
    private Set<BackpackFoodItem> foodItems;

    // Contenu : Liste de l'équipement
    @ManyToMany
    @JoinTable(
            name = "backpack_equipment",
            joinColumns = @JoinColumn(name = "backpack_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<EquipmentItem> equipmentItems;

    public Backpack() {}
    public Backpack(Participant owner) {
        this.owner = owner;
    }

    public Object getId() {
        return this.id;
    }
}