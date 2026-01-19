package fr.plop.contexts.game.config.template.persistence;

import fr.plop.contexts.game.config.Image.persistence.ImageConfigRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardConfigRepository;
import fr.plop.contexts.game.config.inventory.persistence.GameConfigInventoryRepository;
import fr.plop.contexts.game.config.map.persistence.MapConfigRepository;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigRepository;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {

    String FROM = "FROM TemplateEntity template";

    String FETCH_FULL = " LEFT JOIN FETCH template.scenario scenario" + ScenarioConfigRepository.FETCH_ALL +
            " LEFT JOIN FETCH template.board board" + BoardConfigRepository.FETCH_ALL +
            " LEFT JOIN FETCH template.map map" + MapConfigRepository.FETCH_ALL +
            " LEFT JOIN FETCH template.talk talk" + TalkConfigRepository.FETCH_ALL +
            " LEFT JOIN FETCH template.image image" + ImageConfigRepository.FETCH_ALL +
            " LEFT JOIN FETCH template.inventory inventory" + GameConfigInventoryRepository.FETCH_ALL;

    String WHERE_TEMPLATE_ID = " WHERE template.id = :id";

    @Query(FROM + FETCH_FULL + WHERE_TEMPLATE_ID)
    List<TemplateEntity> fullById(@Param("id") String id);

    @Query(FROM + " WHERE LOWER(REPLACE(template.code, ' ', '')) LIKE LOWER(CONCAT(:pattern, '%'))")
    List<TemplateEntity> findLikeLowerCode(@Param("pattern") String pattern);

}
