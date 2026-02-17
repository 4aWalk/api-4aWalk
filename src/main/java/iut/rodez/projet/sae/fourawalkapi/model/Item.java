package iut.rodez.projet.sae.fourawalkapi.model;

/**
 * Interface définissant le contrat pour tout objet pouvant être transporté dans un sac à dos
 */
public interface Item {


    String getNom();
    double getMasseGrammes();
    int getNbItem();

    void setNom(String nom);
    void setMasseGrammes(double masseGrammes);
    void setNbItem(int nbItem);
}