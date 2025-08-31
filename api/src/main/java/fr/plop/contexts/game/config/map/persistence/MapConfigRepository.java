package fr.plop.contexts.game.config.map.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MapConfigRepository extends JpaRepository<MapConfigEntity, String> {

    @Query("FROM MapConfigEntity config" +
            " LEFT JOIN FETCH config.items item" +
            " LEFT JOIN FETCH item.label" +
            " LEFT JOIN FETCH item.positions position" +
            " WHERE config.id = :id")
    Optional<MapConfigEntity> fullById(@Param("id") String id);

}
