package fr.plop.contexts.game.commun.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, String> {

    Optional<GameEntity> findByProjectAndVersion(GameProjectEntity project, String version);

}
