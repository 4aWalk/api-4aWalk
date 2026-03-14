package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;

/**
 * DTO ultra-léger pour l'affichage de la liste des randonnées (sans le N+1 SELECT).
 */
public record HikeSummaryDto(
        Long id,
        String libelle,
        int dureeJours,
        boolean isOptimize
        // Tu pourras ajouter le nom du départ/arrivée plus tard si besoin !
) {
    /**
     * Constructeur pratique pour convertir directement une Entité en DTO
     */
    public HikeSummaryDto(Hike hike) {
        this(
                hike.getId(),
                hike.getLibelle(),
                hike.getDureeJours(),
                hike.getOptimize()
        );
    }
}