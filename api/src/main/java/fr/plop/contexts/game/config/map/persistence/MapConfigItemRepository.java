package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MapConfigItemRepository extends JpaRepository<MapConfigItemAbstractEntity, String> {

}
