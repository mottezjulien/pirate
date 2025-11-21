package fr.plop.contexts.game.config.template.persistence;

import fr.plop.contexts.game.config.Image.persistence.ImageConfigRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardConfigRepository;
import fr.plop.contexts.game.config.map.persistence.MapConfigRepository;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigRepository;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {

    @Query("FROM TemplateEntity template" +

            " LEFT JOIN FETCH template.scenario scenario" +
            ScenarioConfigRepository.FETCH_ALL +

            " LEFT JOIN FETCH template.board board" +
            BoardConfigRepository.FETCH_ALL +

            " LEFT JOIN FETCH template.map map" +
            MapConfigRepository.FETCH_ALL +

            " LEFT JOIN FETCH template.talk talk" +
            TalkConfigRepository.FETCH_ALL +

            " LEFT JOIN FETCH template.image image" +
            ImageConfigRepository.FETCH_ALL +

            " WHERE template.code = :code")
    List<TemplateEntity> findByCodeFetchAll(String code);

}
