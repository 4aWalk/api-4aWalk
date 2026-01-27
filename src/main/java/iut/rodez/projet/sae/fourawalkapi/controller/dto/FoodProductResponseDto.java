package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;

public class FoodProductResponseDto {
    private Long id;
    private String nom;
    private double masseGrammes;
    private String conditionnement;
    private double apportNutritionnelKcal;
    private double prixEuro;
    private int nbItem;

    public FoodProductResponseDto(FoodProduct food) {
        this.id = food.getId();
        this.nom = food.getNom();
        this.masseGrammes = food.getMasseGrammes();
        this.conditionnement = food.getConditionnement();
        this.apportNutritionnelKcal = food.getApportNutritionnelKcal();
        this.prixEuro = food.getPrixEuro();
        this.nbItem = food.getNbItem();
    }

    // Getters obligatoires pour la sérialisation JSON
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public double getMasseGrammes() { return masseGrammes; }
    public String getConditionnement() { return conditionnement; }
    public double getApportNutritionnelKcal() { return apportNutritionnelKcal; }
    public double getPrixEuro() { return prixEuro; }
    public int getNbItem() { return nbItem; }
}