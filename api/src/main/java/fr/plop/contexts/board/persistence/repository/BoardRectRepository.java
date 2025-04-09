package fr.plop.contexts.board.persistence.repository;

import fr.plop.contexts.board.persistence.entity.BoardRectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRectRepository extends JpaRepository<BoardRectEntity, String> {

}
