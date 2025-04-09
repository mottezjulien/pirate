package fr.plop.contexts.scenario.persistence.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<ScenarioEntity, String> {

}
