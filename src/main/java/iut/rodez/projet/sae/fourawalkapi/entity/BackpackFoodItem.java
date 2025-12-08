package iut.rodez.projet.sae.fourawalkapi.entity;

import jakarta.persistence.*;

/** * Entité de jointure pour stocker la quantité d'un produit alimentaire
 * transportée dans un sac donné (Nécessaire car on porte X sachets de riz).
 */
@Entity
@Table(name = "backpack_food_items")
public class BackpackFoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Le sac à dos auquel cet aliment appartient
    @ManyToOne
    @JoinColumn(name = "backpack_id")
    private Backpack backpack;

    // Le produit alimentaire concerné
    @ManyToOne
    @JoinColumn(name = "food_product_id")
    private FoodProduct foodProduct;

    // La quantité d'unités (sachets, boîtes) transportées
    private int quantity;

    public BackpackFoodItem() {}
    public BackpackFoodItem(Backpack backpack, FoodProduct foodProduct, int quantity) {
        this.backpack = backpack;
        this.foodProduct = foodProduct;
        this.quantity = quantity;
    }
}
