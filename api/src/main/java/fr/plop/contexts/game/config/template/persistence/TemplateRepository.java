package fr.plop.contexts.game.config.template.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {

    @Query("FROM TemplateEntity template" +

            " LEFT JOIN FETCH template.scenario scenario" +
            " LEFT JOIN FETCH scenario.steps step" +
            " LEFT JOIN FETCH step.targets target" +
            " LEFT JOIN FETCH target.label label" +
            " LEFT JOIN FETCH target.description description" +
            " LEFT JOIN FETCH step.possibilities possibility" +
            " LEFT JOIN FETCH possibility.recurrence recurrence" +
            " LEFT JOIN FETCH possibility.trigger trigger" +
            " LEFT JOIN FETCH possibility.conditions condition" +
            " LEFT JOIN FETCH possibility.consequences consequence" +

            " LEFT JOIN FETCH template.board board" +
            " LEFT JOIN FETCH board.spaces space" +
            " LEFT JOIN FETCH space.rects rect" +

            " LEFT JOIN FETCH template.map map_config" +
            " LEFT JOIN FETCH map_config.items map_config_item" +
            " LEFT JOIN FETCH map_config_item.map map" +
            " LEFT JOIN FETCH map.label map_label" +

            " WHERE template.code = :code")
    List<TemplateEntity> findByCodeFetchAll(String code);

}
