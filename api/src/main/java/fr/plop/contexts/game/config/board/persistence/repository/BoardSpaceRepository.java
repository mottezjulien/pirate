package fr.plop.contexts.game.config.board.persistence.repository;

import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardSpaceRepository extends JpaRepository<BoardSpaceEntity, String> {

}
