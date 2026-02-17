package iut.rodez.projet.sae.fourawalkapi.model;

import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

/**
 * Interface utilisé pour représenter une tiers physique
 */
public interface Person {
    String getPrenom();
    void setPrenom(String prenom);
    String getNom();
    void setNom(String nom);
    int getAge();
    void setAge(int age);
    Level getNiveau(); // Sportif, Entrainé, Débutant
    void setNiveau(Level niveau);
    Morphology getMorphologie();
    void setMorphologie(Morphology morphologie);
    Long getId();
    void setId(Long id);
}
