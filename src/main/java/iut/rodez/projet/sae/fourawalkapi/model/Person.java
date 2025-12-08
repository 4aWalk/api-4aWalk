package iut.rodez.projet.sae.fourawalkapi.model;

import java.time.LocalDate;

public interface Person {
    int getAge();
    String getNiveau(); // Sportif, Entrainé, Débutant
    String getMorphologie(); // Légère, Moyenne, Forte
}
