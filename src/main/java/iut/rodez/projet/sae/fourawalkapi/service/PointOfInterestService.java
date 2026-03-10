package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.exception.BusinessValidationException;
import iut.rodez.projet.sae.fourawalkapi.exception.ResourceNotFoundException;
import iut.rodez.projet.sae.fourawalkapi.exception.UnauthorizedAccessException;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.PointOfInterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service poi gérant la logique métier de base
 */
@Service
public class PointOfInterestService {

    private final HikeRepository hikeRepository;
    private final PointOfInterestRepository poiRepository;

    public PointOfInterestService(HikeRepository hr, PointOfInterestRepository pr) {
        this.hikeRepository = hr;
        this.poiRepository = pr;
    }

    /**
     * Met à jour l'intégralité des points d'intérêt d'une randonnée.
     * Supprime les anciens points et enregistre la nouvelle liste ordonnée.
     *
     * @param hikeId  Identifiant de la randonnée.
     * @param newPois Liste des nouveaux points d'intérêt.
     * @param userId  Identifiant de l'utilisateur propriétaire.
     * @return La liste des points d'intérêt sauvegardés et ordonnés.
     * @throws ResourceNotFoundException Si la ressources est introuvable
     * @throws UnauthorizedAccessException Si l'utilisateur n'est pas le créateur de la randonnée.
     */
    @Transactional
    public List<PointOfInterest> updateAllPois(Long hikeId, List<PointOfInterest> newPois, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new ResourceNotFoundException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Accès refusé : Vous n'êtes pas le propriétaire.");
        }

        // supprime les anciens points pour les supprimer de la base
        List<PointOfInterest> oldPois = new ArrayList<>(hike.getOptionalPoints());
        hike.getOptionalPoints().clear();
        poiRepository.deleteAll(oldPois);

        // Ajout des nouveaux points avec mise à jour de la séquence
        for (int i = 0; i < newPois.size(); i++) {
            validatePointOfInterest(newPois.get(i));
            PointOfInterest poi = newPois.get(i);
            poi.setId(null);
            poi.setSequence(i);

            PointOfInterest savedPoi = poiRepository.save(poi);
            hike.getOptionalPoints().add(savedPoi);
        }

        hikeRepository.save(hike);
        return hike.getOptionalPoints();
    }

    /**
     * Validation métier des information d'un poi
     * @param poi Point d'interêt vérifié
     */
    public void validatePointOfInterest(PointOfInterest poi) {
        if(poi.getNom() == null || poi.getNom().isEmpty()) {
            throw new BusinessValidationException("Le nom d'un point d'interêt" +
                    " est obligatoire");
        }

        if(poi.getLatitude() < -90 || poi.getLatitude() > 90) {
            throw new BusinessValidationException("La latitude doit être entre -90 et 90");
        }

        if(poi.getLongitude() < -180 || poi.getLatitude() > 180) {
            throw new BusinessValidationException("La longitude doit être entre -180 et 180");
        }
    }
}