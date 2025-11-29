package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MapConfigRepository extends JpaRepository<MapConfigEntity, String> {

    String FETCH_ALL = " LEFT JOIN FETCH map.items map_item";

}
