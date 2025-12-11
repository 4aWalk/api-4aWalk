package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

/**
 * DTO pour recevoir les données d'inscription (POST /register).
 * Contient le mot de passe en clair avant hachage.
 */
public class UserRegistrationRequest {
    private String mail;
    private String password;
    private String nom;
    private String prenom;
    private int age;
    private String adresse;
    private Level niveau;
    private Morphology morphology;

    // Constructeur par défaut et Getters/Setters nécessaires pour Jackson/Spring
    public UserRegistrationRequest() {}

    // Getters
    public String getMail() { return mail; }
    public String getPassword() { return password; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public Level getNiveau() { return niveau; }

    public String getAdresse() {return adresse;}

    public Morphology getMorphologie() { return this.morphology; }
    // Setters
    public void setMail(String mail) { this.mail = mail; }
    public void setPassword(String password) { this.password = password; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setAge(int age) { this.age = age; }
    public void setNiveau(Level niveau) { this.niveau = niveau; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setMorphologie(Morphology morphology) { this.morphology = morphology; }
}
