package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MapRepository extends JpaRepository<MapEntity, String> {

}
