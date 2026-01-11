package iut.rodez.projet.sae.fourawalkapi.model;

import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;

public interface Person {
    int getAge();
    Level getNiveau(); // Sportif, Entrainé, Débutant
    Morphology getMorphologie(); // Légère, Moyenne, Forte

    Long getId();
}
