package fr.plop.contexts.game.commun.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameProjectRepository extends JpaRepository<GameProjectEntity, String> {

    Optional<GameProjectEntity> findByCode(String code);

}
