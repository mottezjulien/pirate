package fr.plop.contexts.game.config.scenario.persistence.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<ScenarioConfigEntity, String> {

}
