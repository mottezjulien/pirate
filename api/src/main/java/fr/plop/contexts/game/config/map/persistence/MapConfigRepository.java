package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MapConfigRepository extends JpaRepository<MapConfigEntity, String> {

    @Query("FROM MapConfigEntity config" +
            " LEFT JOIN FETCH config.items item" +
            " LEFT JOIN FETCH item.map map" +
            " LEFT JOIN FETCH map.label" +
            " LEFT JOIN FETCH map.positions position" +
            " WHERE config.id = :id")
    Optional<MapConfigEntity> fullById(@Param("id") String id);

}
