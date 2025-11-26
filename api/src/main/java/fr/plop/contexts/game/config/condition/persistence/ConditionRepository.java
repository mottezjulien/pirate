package fr.plop.contexts.game.config.condition.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<ConditionEntity, String> {

}
