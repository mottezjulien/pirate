package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TalkConfigRepository extends JpaRepository<TalkConfigEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH talk.items talk_item" +
            " LEFT JOIN FETCH talk_item.value talk_value";
    @Query("SELECT DISTINCT talk FROM TalkConfigEntity talk"
             + FETCH_ALL + " WHERE talk.id = :id")
    Optional<TalkConfigEntity> fullById(@Param("id") String id);

}