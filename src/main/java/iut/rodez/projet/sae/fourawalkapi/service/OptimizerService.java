package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.model.enums.TypeEquipment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
public class OptimizerService {

    private static EquipmentItemService equipmentService = null;

    public OptimizerService(EquipmentItemService es) {
        this.equipmentService = es;
    }

    public static void optimizeFoodV1(Hike hike) {
        List<FoodProduct> foodList = new ArrayList<>(hike.getFoodCatalogue());
        List<Participant> participantList = new ArrayList<>(hike.getParticipants());
        if (participantList.isEmpty()) throw new RuntimeException("Aucun particpant trouvé pour l'optimisation");
        int participantIndex = 0;
        boolean allFoodadd = true;

        for (FoodProduct fp : foodList) {
            boolean assigned = false;
            for (int i = 0; i < participantList.size(); i++) {
                int currentIndex = (participantIndex + i) % participantList.size();
                Participant p = participantList.get(currentIndex);

                p.getBackpack().updateAndGetTotalMass();

                if (p.getBackpack().getTotalMassKg() + fp.getWeightKg() <= p.getCapaciteEmportMaxKg()) {

                    p.getBackpack().addFoodItems(fp);

                    participantIndex = (currentIndex + 1) % participantList.size();

                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                allFoodadd = false;
                break;
            }
        }
        if (!allFoodadd) {throw new RuntimeException("Toutes la nourriture n'as pas pu être placer dans les sac des participant avec l'optimiseur V1");}
    }

    public static void optimizeEquipmentV1(Hike hike) {
        List<EquipmentItem> equipmentList = new ArrayList<>(hike.getEquipmentRequired());
        List<Participant> participantList = new ArrayList<>(hike.getParticipants());
        if (participantList.isEmpty()) throw new RuntimeException("Aucun particpant trouvé pour l'optimisation");
        int participantIndex = 0;
        boolean allEquipementAdd = true;

        for (EquipmentItem e : equipmentList) {
            boolean assigned = false;
            for (int i = 0; i < participantList.size(); i++) {
                int currentIndex = (participantIndex + i) % participantList.size();
                Participant p = participantList.get(currentIndex);

                p.getBackpack().updateAndGetTotalMass();

                if (p.getBackpack().getTotalMassKg() + e.getWeightKg() <= p.getCapaciteEmportMaxKg()) {

                    p.getBackpack().addEquipmentItems(e);

                    participantIndex = (currentIndex + 1) % participantList.size();

                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                allEquipementAdd = false;
                break;
            }
        }
        if (!allEquipementAdd) {throw new RuntimeException("Tout les équipements n'ont pas pu être placer dans les sac des participant avec l'optimiseur V1");}
    }

    public static Hike optimizeEquipmentV2(Hike hike) {
        List<EquipmentItem> equipmentSoinList = equipmentService.getEquipmentByType(hike.getId(), TypeEquipment.SOIN);
        List<EquipmentItem> equipmentProgressionList = equipmentService.getEquipmentByType(hike.getId(), TypeEquipment.PROGRESSION);
        List<EquipmentItem> equipmentEauList = equipmentService.getEquipmentByType(hike.getId(), TypeEquipment.EAU);
        List<EquipmentItem> equipmentReposList = null;
        if (hike.getDureeJours() > 1)equipmentReposList = equipmentService.getEquipmentByType(hike.getId(), TypeEquipment.REPOS);

        optimizeEquipementSoinV2(equipmentSoinList);

        return new Hike();
    }

    private static void optimizeEquipementSoinV2(List<EquipmentItem> equipmentSoinList) {
        /*for (EquipmentItem e : equipmentSoinList) {
            if(e.TypeEquipment.SOIN){}
        }
        TreeSet<EquipmentItem> equipmentSoinSet = new TreeSet<>(equipmentSoinList);
    */}

    public static Hike optimizeFoodV2(Hike hike) {
        return new Hike();
    }
}
