package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkItemRepository extends JpaRepository<TalkItemEntity, String> {

    /*@Query("FROM TalkItemEntity talk_item" +
            " LEFT JOIN FETCH talk_item.value" +
            " LEFT JOIN FETCH talk_item.characterReference character_reference" +
            " LEFT JOIN FETCH character_reference.character character" +
            " WHERE talk_item.id = :talkId")
    Optional<TalkItemEntity> fullById(@Param("talkId") String talkId);*/

}