package iut.rodez.projet.sae.fourawalkapi.repository.mysql;

import iut.rodez.projet.sae.fourawalkapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Gestion des dialogues bd pour les utilisateurs
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Obtient un utilisateur à partir d'un email
     * @param mail mail recherché
     * @return l'utilisateur trouvé
     */
    Optional<User> findByMail(String mail);
}