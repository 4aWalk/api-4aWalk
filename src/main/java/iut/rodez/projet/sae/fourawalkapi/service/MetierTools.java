package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.entity.Participant;
import iut.rodez.projet.sae.fourawalkapi.entity.PointOfInterest;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Level;
import iut.rodez.projet.sae.fourawalkapi.model.enums.Morphology;
import java.util.Set;

class MetierTools {

    public static void validateHikeForOptimize(Hike hike) {
        PointOfInterest pDepart = hike.getDepart();
        PointOfInterest pArrive = hike.getArrivee();
        double distanceRando = pDepart.distanceTo(pArrive.getLatitude(), pArrive.getLongitude());

        // Validation de la distance de la randonné cohérente
        validateDistanceHike(distanceRando, hike.getDureeJours(), getParticipantWithBadStat(hike));

        // Validation des informations participants
        for (Participant p : hike.getParticipants()) {
            validateKcalParticipant(p, distanceRando);
            validateEauParticipant(p, distanceRando);
            validatePoidsParticipant(p);
        }

    }



    private static Participant getParticipantWithBadStat(Hike hike) {
        if (hike.getParticipants().size()==0) {throw new RuntimeException("Aucun participant n'a été trouvé dans la randonné");}
        if (hike.getParticipants().size()==1) {return hike.getParticipants().iterator().next();}
        Set<Participant> participants = hike.getParticipants();
        Participant participant =  participants.iterator().next();
        double scoreInitial = 1.0;
        for (Participant p : participants) {
            double tempScore = 1.0;
            if(p.getNiveau() == Level.DEBUTANT){tempScore =- 0.2;}
            if(p.getNiveau() == Level.ENTRAINE){tempScore =- 0.1;}
            if(p.getMorphologie() == Morphology.MOYENNE){tempScore =- 0.1;}
            if(p.getMorphologie() == Morphology.FORTE){tempScore =- 0.2;}
            if(p.getAge() < 16) {tempScore =- 0.3;}
            else if(p.getAge() > 50 && p.getAge() < 71){tempScore =- 0.2;}
            else if(p.getAge() > 70){tempScore =- 0.3;}
            else if(tempScore < scoreInitial){scoreInitial = tempScore; participant = p;}
        }
        return participant;
    }

    private static void validateDistanceHike(double distanceHike, int nbJour, Participant participantReferent){
        if (nbJour <1 || nbJour > 3){throw new IllegalArgumentException("Le nombre de jour de la randonné n'est pas valide");}
        double distanceAVerifier = distanceHike/nbJour;
        double distanceQuotidienne = 25;
        if(participantReferent.getNiveau() == Level.ENTRAINE){distanceQuotidienne =+ 5;}
        if(participantReferent.getNiveau() == Level.SPORTIF){distanceQuotidienne =+ 10;}
        if(participantReferent.getMorphologie() == Morphology.LEGERE){distanceQuotidienne =+ 5;}
        if(participantReferent.getMorphologie() == Morphology.FORTE){distanceQuotidienne =- 5;}
        if(participantReferent.getAge() < 16){distanceQuotidienne =- 5;}
        else if(participantReferent.getAge() > 50 && participantReferent.getAge() < 71){distanceQuotidienne =- 5;}
        else if(participantReferent.getAge() > 70){distanceQuotidienne =- 10;}

        if(distanceQuotidienne > distanceAVerifier - distanceAVerifier * 0.1 &&
                distanceQuotidienne < distanceAVerifier + distanceAVerifier * 0.1) {
            throw new RuntimeException("Les la distance de la randonné " + distanceHike + "km est aberrant");
        }

    }

    private static void validateKcalParticipant(Participant participant, double distanceRandonne) {
        if(participant.getBesoinKcal() <= 0){
            throw new RuntimeException("Les besoins caloriques d'un participant n'a pas été définies");
        }
        double besoinInitial = 2400 + distanceRandonne * 50;
        if(participant.getNiveau() == Level.DEBUTANT){besoinInitial =+ 200;}
        if(participant.getNiveau() == Level.SPORTIF){besoinInitial =- 200;}
        if(participant.getMorphologie() == Morphology.LEGERE){besoinInitial =- 200;}
        if(participant.getMorphologie() == Morphology.FORTE){besoinInitial =+ 200;}
        if(participant.getAge() < 16){besoinInitial =- 300;}
        else if(participant.getAge() < 31){besoinInitial =+ 100;}
        else if(participant.getAge() > 50 && participant.getAge() < 71){besoinInitial =- 100;}
        else if(participant.getAge() > 70){besoinInitial =- 300;}

        if(participant.getBesoinKcal() > besoinInitial - besoinInitial * 0.1 &&
            participant.getBesoinKcal() < besoinInitial + besoinInitial * 0.1) {
            throw new RuntimeException("Les besoins kcal " + participant.getBesoinKcal() + "kcal/j est aberrant");
        }
    }

    private static void validateEauParticipant(Participant participant, double distanceRandonne) {
        if(participant.getBesoinEauLitre() <= 0){
            throw new RuntimeException("Les besoins en eau d'un participant n'a pas été définies");
        }
        double besoinInitial = 2 + distanceRandonne * 0.1;
        if(participant.getNiveau() == Level.DEBUTANT){besoinInitial =+ 0.5;}
        if(participant.getNiveau() == Level.SPORTIF){besoinInitial =- 0.25;}
        if(participant.getMorphologie() == Morphology.LEGERE){besoinInitial =- 0.25;}
        if(participant.getMorphologie() == Morphology.FORTE){besoinInitial =+ 0.5;}
        if(participant.getAge() < 16){besoinInitial =- 0.5;}
        else if(participant.getAge() < 31){besoinInitial =- 0.25;}
        else if(participant.getAge() > 50 && participant.getAge() < 71){besoinInitial =+ 0.25;}
        else if (participant.getAge() > 70){besoinInitial =+ 0.5;}
        if(participant.getBesoinEauLitre() > besoinInitial - besoinInitial * 0.1 &&
                participant.getBesoinEauLitre() < besoinInitial + besoinInitial * 0.1) {
            throw new RuntimeException("Les besoins en eau " + participant.getBesoinEauLitre() + "L/j est aberrant");
        }
    }

    private static void validatePoidsParticipant(Participant participant) {
        if(participant.getCapaciteEmportMaxKg() <= 0){
            throw new RuntimeException("La capacité du sac d'un participant n'a pas été définie");
        }
        double besoinInitial = 15;
        if(participant.getNiveau() == Level.DEBUTANT){besoinInitial =- 3;}
        if(participant.getNiveau() == Level.SPORTIF){besoinInitial =+ 3;}
        if(participant.getMorphologie() == Morphology.LEGERE){besoinInitial =- 3;}
        if(participant.getMorphologie() == Morphology.FORTE){besoinInitial =+ 3;}
        if(participant.getAge() < 16){besoinInitial =- 5;}
        else if(participant.getAge() < 31){besoinInitial =- 3;}
        else if(participant.getAge() < 51){besoinInitial =+ 1;}
        else if(participant.getAge() < 71){besoinInitial =- 3;}
        else {besoinInitial =- 5;}
        if(participant.getCapaciteEmportMaxKg() > besoinInitial - besoinInitial * 0.1 &&
                participant.getCapaciteEmportMaxKg() < besoinInitial + besoinInitial * 0.1) {
            throw new RuntimeException("La capacité du sac à dos " + participant.getBesoinEauLitre() + "kg est aberrant");
        }
    }
}
