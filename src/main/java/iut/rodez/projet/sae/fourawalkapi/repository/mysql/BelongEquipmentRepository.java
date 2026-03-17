package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.BelongEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BelongEquipmentRepository extends JpaRepository<BelongEquipment, Long> {

    /**
     * Récupère l'ID du participant propriétaire d'un équipement pour une randonnée donnée.
     * Retourne null si aucun propriétaire n'est défini.
     */
    @Query("SELECT b.participant.id FROM BelongEquipment b WHERE b.hike.id = :hikeId AND b.equipment.id = :equipmentId")
    Long getIfExistParticipantForEquipmentAndHike(@Param("hikeId") Long hikeId, @Param("equipmentId") Long equipmentId);

    /**
     * Récupère la liste d'appartenance d'équipement pour une randonnée
     * @param hikeId identifiant de la randonnée
     * @return la liste d'appartenance d'équipement
     */
    List<BelongEquipment> findByHikeId(Long hikeId);

    /**
     * Supression par rapport à la liaison avec une randonnée
     * @param hikeId identifiant randonnée
     */
    void deleteByHikeId(Long hikeId);
}