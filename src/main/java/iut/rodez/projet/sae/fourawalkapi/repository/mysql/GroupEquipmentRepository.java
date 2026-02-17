package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Gestion des dialogues bd pour les groupes d'équipements
 */
@Repository
public interface GroupEquipmentRepository extends JpaRepository<GroupEquipment, Long> {

    /**
     * Sans chargement de l'ensemble des équipements
     * @param id identifiant du groupe
     * @return groupe trouvé
     */

    Optional<GroupEquipment> findById(Long id);

    /**
     * Charge les groupes et les équipements les composants
     * @param id identifiant du groupe
     * @return groupe trouvé
     */
    @Query("SELECT g FROM GroupEquipment g LEFT JOIN FETCH g.items WHERE g.id = :id")
    Optional<GroupEquipment> findByIdWithItems(@Param("id") Long id);
}
