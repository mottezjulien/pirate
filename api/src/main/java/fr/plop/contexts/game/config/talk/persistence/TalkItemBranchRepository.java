package fr.plop.contexts.game.config.talk.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalkItemBranchRepository extends JpaRepository<TalkItemBranchEntity, String> {
}
