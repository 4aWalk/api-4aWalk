package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.PointOfInterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointOfInterestService {

    private final HikeRepository hikeRepository;
    private final PointOfInterestRepository poiRepository;

    public PointOfInterestService(HikeRepository hr, PointOfInterestRepository pr) {
        this.hikeRepository = hr;
        this.poiRepository = pr;
    }

    @Transactional
    public PointOfInterest addPoiToHike(Long hikeId, PointOfInterest poi, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        PointOfInterest saved = poiRepository.save(poi);
        hike.getOptionalPoints().add(saved);
        hikeRepository.save(hike);
        return saved;
    }

    @Transactional
    public void removePoiFromHike(Long hikeId, Long poiId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId).orElseThrow();
        if (!hike.getCreator().getId().equals(userId)) throw new RuntimeException("Accès refusé");

        PointOfInterest poi = poiRepository.findById(poiId).orElseThrow();

        hike.getOptionalPoints().remove(poi);
        hikeRepository.save(hike);
        poiRepository.delete(poi);
    }
}