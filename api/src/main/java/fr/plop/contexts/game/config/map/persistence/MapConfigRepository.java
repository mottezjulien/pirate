package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MapConfigRepository extends JpaRepository<MapConfigEntity, String> {

    String FETCH_ALL =" LEFT JOIN FETCH map.items map_item" +
            " LEFT JOIN FETCH map_item.label map_label";

    @Query("FROM MapConfigEntity map" +
            FETCH_ALL +
            " WHERE map.id = :id")
    Optional<MapConfigEntity> fullById(@Param("id") String id);

}
