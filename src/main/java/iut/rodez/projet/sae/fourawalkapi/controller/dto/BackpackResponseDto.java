package iut.rodez.projet.sae.fourawalkapi.controller.dto;

import java.util.List;

/**
 * DTO représentant l'état du sac à dos d'un participant.
 */
public class BackpackResponseDto {
    private Long participantId;
    private double poidsActuelKg;
    private double capaciteMaxKg;
    private double pourcentageRemplissage;
    private List<String> itemsContenus; // Liste des noms des objets (nourriture/équipement)

    public BackpackResponseDto(Long participantId, double poidsActuelKg, double capaciteMaxKg, List<String> itemsContenus) {
        this.participantId = participantId;
        this.poidsActuelKg = poidsActuelKg;
        this.capaciteMaxKg = capaciteMaxKg;
        this.itemsContenus = itemsContenus;
        this.pourcentageRemplissage = (poidsActuelKg / capaciteMaxKg) * 100;
    }

    public Long getParticipantId() { return participantId; }
    public double getPoidsActuelKg() { return poidsActuelKg; }
    public double getCapaciteMaxKg() { return capaciteMaxKg; }
    public double getPourcentageRemplissage() { return pourcentageRemplissage; }
    public List<String> getItemsContenus() { return itemsContenus; }
}