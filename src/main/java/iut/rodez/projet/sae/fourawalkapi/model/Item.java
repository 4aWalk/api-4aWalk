package iut.rodez.projet.sae.fourawalkapi.model;

/**
 * Interface définissant le contrat pour tout objet pouvant être transporté
 * dans un sac à dos (Backpack).
 * Utilisée pour l'unification du calcul des charges (UC 2.1.4).
 */
public interface Item {

    /**
     * @return Le nom ou la désignation de l'objet.
     */
    String getNom();


    /**
     * @return La masse de l'objet exprimée en grammes.
     */
    double getMasseGrammes();

    int getNbItem();
}