package fr.plop.contexts.game.config.scenario.persistence.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioStepRepository extends JpaRepository<ScenarioStepEntity, String> {

}
