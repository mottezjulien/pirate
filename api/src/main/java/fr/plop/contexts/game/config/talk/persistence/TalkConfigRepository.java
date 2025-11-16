package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TalkConfigRepository extends JpaRepository<TalkConfigEntity, String> {

    String FETCH_ALL = " LEFT JOIN FETCH talk.items item" +
            " LEFT JOIN FETCH item.value" /*+
            " LEFT JOIN FETCH TREAT(item AS TalkItemMultipleOptionsEntity).options opt" +
            " LEFT JOIN FETCH opt.value"*/;

    @Query("SELECT DISTINCT talk FROM TalkConfigEntity talk"
             + FETCH_ALL + " WHERE talk.id = :id")
    Optional<TalkConfigEntity> fullById(@Param("id") String id);

}