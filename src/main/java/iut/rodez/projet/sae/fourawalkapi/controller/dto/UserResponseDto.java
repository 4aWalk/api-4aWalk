package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;


/**
 * DTO pour renvoyer les données d'un utilisateur au client (après inscription ou connexion).
 * Le mot de passe (haché) est omis pour des raisons de sécurité.
 */
public class UserResponseDto {
    private Long id;
    private String mail;
    private String nom;
    private String prenom;
    private int age;
    private String adresse;
    private Level niveau;
    private Morphology morphologie;


    // Constructeur pour mapper l'Entité User vers ce DTO
    public UserResponseDto(User user) {
        this.mail = user.getMail();
        this.nom = user.getNom();
        this.prenom = user.getPrenom();
        this.age = user.getAge();
        this.adresse = user.getAdresse();
        this.niveau = user.getNiveau();
        this.morphologie = user.getMorphologie();
    }

    public Long getId() { return id; }
    public String getMail() { return mail; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public String getAdresse() { return adresse; }
    public Level getNiveau() { return niveau; }
    public Morphology getMorphologie() { return morphologie; }
}