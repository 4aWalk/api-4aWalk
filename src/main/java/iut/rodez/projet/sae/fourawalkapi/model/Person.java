package iut.rodez.projet.sae.fourawalkapi.model;

import java.time.LocalDate;

public interface Person {
    LocalDate getDateNaissance();
    String getNiveau(); // Sportif, Entrainé, Débutant
    String getMorphologie(); // Légère, Moyenne, Forte
}
