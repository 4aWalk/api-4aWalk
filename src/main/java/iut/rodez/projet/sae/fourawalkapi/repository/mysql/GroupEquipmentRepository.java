package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.GroupEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupEquipmentRepository extends JpaRepository<GroupEquipment, Long> {
}
