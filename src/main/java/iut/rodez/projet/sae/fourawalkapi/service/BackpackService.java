package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Backpack;
import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.model.Item;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.BelongEquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BackpackService {

    private final BelongEquipmentRepository belongEquipmentRepository;

    /**
     * Injection de dépendance
     * @param belongEquipmentRepository repository d'appartenance d'équipement
     */
    public BackpackService(BelongEquipmentRepository belongEquipmentRepository) {
        this.belongEquipmentRepository = belongEquipmentRepository;
    }
    /**
     * Détermine si l'objet a un propriétaire et retourne son sac.
     * Restreint aux équipements de type VETEMENT et REPOS.
     */
    public Backpack getPreferredOwnerBackpack(Item item, List<Backpack> backpacks, Long hikeId) {
        // On vérifie que c'est bien un équipement (et non de la nourriture)
        if (item instanceof EquipmentItem equipmentItem) {
            TypeEquipment type = equipmentItem.getType();

            // Si c'est un vêtement ou du repos
            if (type == TypeEquipment.VETEMENT || type == TypeEquipment.REPOS) {
                Long ownerId = belongEquipmentRepository.getIfExistParticipantForEquipmentAndHike(hikeId, item.getId());

                for(Backpack backpack : backpacks) {
                    if(backpack.getOwner().getId().equals(ownerId)) {return backpack;}
                }

            }
        }
        return null; // Aucun propriétaire ou type non éligible
    }
}
