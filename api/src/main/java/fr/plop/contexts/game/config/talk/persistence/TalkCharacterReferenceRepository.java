package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
public interface TalkCharacterReferenceRepository extends JpaRepository<TalkCharacterReferenceEntity, String> {

}