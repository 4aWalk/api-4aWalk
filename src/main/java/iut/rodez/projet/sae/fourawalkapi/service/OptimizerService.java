package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.EquipmentItem;
import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;

import java.util.ArrayList;
import java.util.List;

public class OptimizerService {
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
}
