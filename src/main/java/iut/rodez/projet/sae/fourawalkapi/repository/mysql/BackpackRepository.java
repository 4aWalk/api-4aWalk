package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Gestion des dialogues bd pour les sac à dos
 */
public interface BackpackRepository extends JpaRepository<Backpack, Long> {

    /**
     * Charge un sac à dos avec les groupes d'équipement chargés
     * @param id identifiant du sac à dos
     * @return sac à dos trouvé
     */
    @Query("SELECT b FROM Backpack b " +
            "LEFT JOIN FETCH b.groupEquipments ge " +
            "LEFT JOIN FETCH ge.items " +
            "WHERE b.id = :id")
    Optional<Backpack> findByIdWithFullContent(@Param("id") Long id);
}
