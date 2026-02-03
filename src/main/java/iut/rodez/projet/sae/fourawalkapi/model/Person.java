package iut.rodez.projet.sae.fourawalkapi.model;

import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

public interface Person {
    String getPrenom();
    void setPrenom(String prenom);
    String getNom();
    void setNom(String nom);
    int getAge();
    void setAge(int age);
    Level getNiveau(); // Sportif, Entrainé, Débutant
    void setNiveau(Level niveau);
    Morphology getMorphologie(); // Légère, Moyenne, Forte
    void setMorphologie(Morphology morphologi);
    Long getId();
    void setId(Long id);
}
