package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TalkItemRepository extends JpaRepository<TalkItemEntity, String> {

    @Query("FROM TalkItemEntity talk_item" +
            " LEFT JOIN FETCH talk_item.value" +
            " LEFT JOIN FETCH talk_item.characterImage character_image" +
            " LEFT JOIN FETCH character_image.character character" +
            " WHERE talk_item.id = :talkId")
    Optional<TalkItemEntity> fullById(@Param("talkId") String talkId);

}