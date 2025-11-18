package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkCharacterRepository extends JpaRepository<TalkCharacterEntity, String> {

}