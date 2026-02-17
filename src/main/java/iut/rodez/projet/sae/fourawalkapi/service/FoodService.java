package iut.rodez.projet.sae.fourawalkapi.service;

import iut.rodez.projet.sae.fourawalkapi.entity.FoodProduct;
import iut.rodez.projet.sae.fourawalkapi.entity.Hike;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.FoodProductRepository;
import iut.rodez.projet.sae.fourawalkapi.repository.mysql.HikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des produits alimentaires.
 * Gère le catalogue des nourritures (CRUD) et leur assignation aux randonnées.
 * S'assure que les produits respectent les contraintes nutritionnelles et de poids.
 */
@Service
public class FoodService {

    private final FoodProductRepository foodRepository;
    private final HikeRepository hikeRepository;

    /**
     * Constructeur avec injection de dépendances.
     * @param fr Repository pour la gestion des aliments.
     * @param hr Repository pour la récupération des randonnées.
     */
    public FoodService(FoodProductRepository fr, HikeRepository hr) {
        this.foodRepository = fr;
        this.hikeRepository = hr;
    }

    /**
     * Récupère l'intégralité du catalogue de nourriture.
     * @return La liste complète des produits alimentaires disponibles.
     */
    public List<FoodProduct> getAllFoods() {
        return foodRepository.findAll();
    }

    /**
     * Ajoute un nouvel aliment au catalogue global après validation.
     *
     * @param food L'objet FoodProduct à persister.
     * @return L'objet sauvegardé avec son ID généré.
     * @throws RuntimeException Si les règles de validation (poids/calories) ne sont pas respectées.
     */
    public FoodProduct createFood(FoodProduct food) {
        // Validation des règles métiers (bornes min/max) avant insertion en base
        validateFoodRules(food);
        return foodRepository.save(food);
    }

    /**
     * Supprime un aliment du catalogue par son identifiant.
     * @param id L'identifiant (Long) de l'aliment à supprimer.
     */
    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }

    /**
     * Associe une provision alimentaire à une randonnée spécifique.
     * Utilise une transaction pour garantir que l'ajout est atomique.
     *
     * @param hikeId L'ID de la randonnée cible.
     * @param foodId L'ID de l'aliment à ajouter au sac commun.
     * @param userId L'ID de l'utilisateur effectuant l'action (pour vérification des droits).
     * @throws RuntimeException Si la randonnée/aliment n'existe pas ou si l'utilisateur n'est pas le créateur.
     */
    @Transactional
    public void addFoodToHike(Long hikeId, Long foodId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        // Vérification de sécurité : Seul le créateur gère le stock de nourriture
        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé : Vous n'êtes pas le propriétaire de cette randonnée");
        }

        FoodProduct fp = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Aliment introuvable"));

        // Délégation à l'entité Hike pour la gestion de la collection
        hike.addFood(fp);

        // La sauvegarde cascade la mise à jour de la table de jointure
        hikeRepository.save(hike);
    }

    /**
     * Retire une provision alimentaire d'une randonnée.
     *
     * @param hikeId L'ID de la randonnée.
     * @param foodId L'ID de l'aliment à retirer.
     * @param userId L'ID de l'utilisateur (sécurité).
     * @throws RuntimeException Si l'accès est refusé ou les ressources introuvables.
     */
    @Transactional
    public void removeFoodFromHike(Long hikeId, Long foodId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new RuntimeException("Randonnée introuvable"));

        if (!hike.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Accès refusé");
        }

        FoodProduct food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Aliment introuvable"));

        // Suppression via la méthode helper de l'entité
        hike.removeFood(food);

        hikeRepository.save(hike);
    }

    /**
     * Centralise la validation des contraintes métiers sur la nourriture.
     * Vérifie la cohérence des données physiques (masse) et énergétiques (calories).
     *
     * @param f L'objet FoodProduct à valider.
     * @throws RuntimeException Si le poids ou les calories sont hors des bornes autorisées.
     */
    private void validateFoodRules(FoodProduct f) {
        // Règle 1 : Poids réaliste pour un item de randonnée (50g - 5kg)
        if (f.getMasseGrammes() < 50 || f.getMasseGrammes() > 5000) {
            throw new RuntimeException("La masse de la nourriture doit être comprise entre 50g et 5kg");
        }

        // Règle 2 : Apport calorique cohérent (50 kcal - 3000 kcal)
        if (f.getApportNutritionnelKcal() < 50 || f.getApportNutritionnelKcal() > 3000) {
            throw new RuntimeException("L'apport calorique doit être compris entre 50 et 3000 kcal");
        }
    }
}