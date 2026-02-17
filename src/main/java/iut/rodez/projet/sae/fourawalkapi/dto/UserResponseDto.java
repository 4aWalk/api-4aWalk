package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;


/**
 * Data transfert object utilisé dans les communications d'objet utilisateur avec le client
 */
public class UserResponseDto {
    // Le mot de passe n'est jamais communiquer au client
    private Long id;
    private String mail;
    private String nom;
    private String prenom;
    private int age;
    private String adresse;
    private Level niveau;
    private Morphology morphologie;


    /**
     * Mapper entity to dto
     * @param user utilisateur à mapper
     */
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.mail = user.getMail();
        this.nom = user.getNom();
        this.prenom = user.getPrenom();
        this.age = user.getAge();
        this.adresse = user.getAdresse();
        this.niveau = user.getNiveau();
        this.morphologie = user.getMorphologie();
    }

    // Getters
    public Long getId() { return id; }
    public String getMail() { return mail; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public String getAdresse() { return adresse; }
    public Level getNiveau() { return niveau; }
    public Morphology getMorphologie() { return morphologie; }
}