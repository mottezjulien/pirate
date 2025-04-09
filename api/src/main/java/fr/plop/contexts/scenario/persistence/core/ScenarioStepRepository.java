package fr.plop.contexts.scenario.persistence.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioStepRepository extends JpaRepository<ScenarioStepEntity, String> {

}
