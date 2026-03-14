package iut.rodez.projet.sae.fourawalkapi.dto;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;

public record HikeSummaryDto(
        Long id,
        String libelle,
        int dureeJours,
        boolean isOptimize,
        int nbParticipants
) {
    /**
     * Version allégé utilisé dans le endpoint hikes/my
     * @param hike hike allégé
     */
    public HikeSummaryDto(Hike hike) {
        this(
                hike.getId(),
                hike.getLibelle(),
                hike.getDureeJours(),
                hike.getOptimize(),
                hike.getParticipants() != null ? hike.getParticipants().size() : 0
        );
    }
}