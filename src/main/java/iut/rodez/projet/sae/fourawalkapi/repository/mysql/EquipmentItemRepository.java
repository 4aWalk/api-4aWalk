package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Gestion des dialogues bd pour les équipements
 */
@Repository
public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long> {
}
