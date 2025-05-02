package fr.plop.contexts.game.config.board.persistence.repository;

import fr.plop.contexts.game.config.board.persistence.entity.BoardRectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRectRepository extends JpaRepository<BoardRectEntity, String> {

}
