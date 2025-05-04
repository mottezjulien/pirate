package fr.plop.contexts.game.config.scenario.persistence.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ScenarioStepRepository extends JpaRepository<ScenarioStepEntity, String> {

    /*@Query("FROM ScenarioStepEntity step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            " LEFT JOIN FETCH possibility.trigger trigger" +
            " LEFT JOIN FETCH possibility.conditions condition" +
            " WHERE step.id = :id")
    Optional<ScenarioStepEntity> allById(String id);*/

}
