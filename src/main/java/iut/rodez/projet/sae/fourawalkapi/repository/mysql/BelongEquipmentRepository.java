package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.BelongEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BelongEquipmentRepository extends JpaRepository<BelongEquipment, Long> {

    /**
     * Récupère l'ID du participant propriétaire d'un équipement pour une randonnée donnée.
     * Retourne null si aucun propriétaire n'est défini.
     */
    @Query("SELECT b.participant.id FROM BelongEquipment b WHERE b.hike.id = :hikeId AND b.equipment.id = :equipmentId")
    Long getIfExistParticipantForEquipmentAndHike(@Param("hikeId") Long hikeId, @Param("equipmentId") Long equipmentId);
}