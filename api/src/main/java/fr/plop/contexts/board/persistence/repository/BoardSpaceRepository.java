package fr.plop.contexts.board.persistence.repository;

import fr.plop.contexts.board.persistence.entity.BoardSpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardSpaceRepository extends JpaRepository<BoardSpaceEntity, String> {

}
