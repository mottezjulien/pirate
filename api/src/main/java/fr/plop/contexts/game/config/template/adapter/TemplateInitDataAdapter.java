package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardRectEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.board.persistence.repository.BoardRectRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardConfigRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardSpaceRepository;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import fr.plop.contexts.i18n.persistence.I18nRepository;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioRepository;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepRepository;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.condition.ScenarioPossibilityConditionRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity.ScenarioPossibilityConditionAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.ScenarioPossibilityConsequenceRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity.ScenarioPossibilityConsequenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.ScenarioPossibilityTriggerRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity.ScenarioPossibilityTriggerAbstractEntity;
import fr.plop.contexts.game.config.template.domain.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

@Component
public class TemplateInitDataAdapter implements TemplateInitUseCase.OutPort {

    private final I18nRepository i18nRepository;

    private final TemplateRepository templateRepository;

    private final BoardConfigRepository boardRepository;
    private final BoardSpaceRepository boardSpaceRepository;
    private final BoardRectRepository boardRectRepository;

    private final ScenarioRepository scenarioRepository;
    private final ScenarioStepRepository scenarioStepRepository;
    private final ScenarioTargetRepository scenarioTargetRepository;
    private final ScenarioPossibilityRepository possibilityRepository;
    private final ScenarioPossibilityTriggerRepository triggerRepository;
    private final ScenarioPossibilityConditionRepository conditionRepository;
    private final ScenarioPossibilityConsequenceRepository consequenceRepository;

    public TemplateInitDataAdapter(I18nRepository i18nRepository, TemplateRepository templateRepository, BoardConfigRepository boardRepository, BoardSpaceRepository boardSpaceRepository, BoardRectRepository boardRectRepository, ScenarioRepository scenarioRepository, ScenarioStepRepository scenarioStepRepository, ScenarioTargetRepository scenarioTargetRepository, ScenarioPossibilityRepository possibilityRepository, ScenarioPossibilityTriggerRepository triggerRepository, ScenarioPossibilityConditionRepository conditionRepository, ScenarioPossibilityConsequenceRepository consequenceRepository) {
        this.i18nRepository = i18nRepository;
        this.templateRepository = templateRepository;
        this.boardRepository = boardRepository;
        this.boardSpaceRepository = boardSpaceRepository;
        this.boardRectRepository = boardRectRepository;
        this.scenarioRepository = scenarioRepository;
        this.scenarioStepRepository = scenarioStepRepository;
        this.scenarioTargetRepository = scenarioTargetRepository;
        this.possibilityRepository = possibilityRepository;
        this.triggerRepository = triggerRepository;
        this.conditionRepository = conditionRepository;
        this.consequenceRepository = consequenceRepository;
    }


    @Override
    public boolean isEmpty() {
        return templateRepository.count() == 0;
    }

    @Override
    public void createAll(Template template) {
        TemplateEntity templateEntity = new TemplateEntity();
        templateEntity.setId(template.id().value());
        templateEntity.setCode(template.code().value());
        templateEntity.setLabel(template.label());
        templateEntity.setVersion(template.version());
        templateEntity.setBoard(createBoard(template.board()));
        templateEntity.setScenario(createScenario(template.scenario()));
        templateRepository.save(templateEntity);
    }


    private BoardConfigEntity createBoard(BoardConfig board) {
        BoardConfigEntity boardEntity = new BoardConfigEntity();
        boardEntity.setId(board.id().value());
        boardRepository.save(boardEntity);
        board.spaces().forEach(space -> {
            BoardSpaceEntity spaceEntity = new BoardSpaceEntity();
            spaceEntity.setId(space.id().value());
            spaceEntity.setBoard(boardEntity);
            spaceEntity.setLabel(space.label());
            spaceEntity.setPriority(space.priority().ordinal());
            boardSpaceRepository.save(spaceEntity);
            space.rects().forEach(rect -> {
                BoardRectEntity rectEntity = new BoardRectEntity();
                rectEntity.setId(StringTools.generate());
                rectEntity.setSpace(spaceEntity);
                rectEntity.setTopRightLatitude(rect.topRight().lat());
                rectEntity.setTopRightLongitude(rect.topRight().lng());
                rectEntity.setBottomLeftLatitude(rect.bottomLeft().lat());
                rectEntity.setBottomLeftLongitude(rect.bottomLeft().lng());
                boardRectRepository.save(rectEntity);
            });
        });
        return boardEntity;
    }


    private ScenarioConfigEntity createScenario(ScenarioConfig scenario) {
        ScenarioConfigEntity scenarioEntity = new ScenarioConfigEntity();
        scenarioEntity.setId(scenario.id().value());
        scenarioEntity.setLabel(scenario.label());
        scenarioRepository.save(scenarioEntity);

        scenario.steps().forEach(step -> {
            ScenarioStepEntity stepEntity = new ScenarioStepEntity();
            stepEntity.setId(step.id().value());
            step.label().ifPresent(label -> stepEntity.setLabel(createI18n(label)));
            stepEntity.setScenario(scenarioEntity);
            scenarioStepRepository.save(stepEntity);
            step.targets().forEach(target -> createTarget(target, stepEntity));
            step.possibilities().forEach(possibility -> createPossibility(possibility, stepEntity));
        });
        return scenarioEntity;
    }

    private void createTarget(ScenarioConfig.Target target, ScenarioStepEntity stepEntity) {
        ScenarioTargetEntity targetEntity = new ScenarioTargetEntity();
        targetEntity.setId(StringTools.generate());
        target.label().ifPresent(label ->
                targetEntity.setLabel(createI18n(label)));
        target.desc().ifPresent(desc ->
                targetEntity.setDescription(createI18n(desc)));
        targetEntity.setStep(stepEntity);
        targetEntity.setOptional(target.optional());
        scenarioTargetRepository.save(targetEntity);
    }

    private void createPossibility(Possibility possibility, ScenarioStepEntity stepEntity) {

        ScenarioPossibilityEntity possibilityEntity = new ScenarioPossibilityEntity();
        possibilityEntity.setId(possibility.id().value());
        possibilityEntity.setConditionType(possibility.conditionType());
        possibilityEntity.setStep(stepEntity);
        possibilityRepository.save(possibilityEntity);

        ScenarioPossibilityTriggerAbstractEntity triggerEntity = ScenarioPossibilityTriggerAbstractEntity.fromModel(possibility.trigger());
        possibilityEntity.setTrigger(triggerRepository.save(triggerEntity));

        possibility.conditions().forEach(condition -> {
            ScenarioPossibilityConditionAbstractEntity conditionEntity = ScenarioPossibilityConditionAbstractEntity
                    .fromModel(condition);
            possibilityEntity.getConditions().add(conditionRepository.save(conditionEntity));
        });

        possibility.consequences().forEach(consequence -> {
            ScenarioPossibilityConsequenceAbstractEntity consequenceEntity = ScenarioPossibilityConsequenceAbstractEntity.fromModel(consequence);
            if(consequence instanceof PossibilityConsequence.Alert alert) {
                createI18n(alert.message());
            }
            possibilityEntity.getConsequences().add(consequenceRepository.save(consequenceEntity));
        });

        possibilityRepository.save(possibilityEntity);
    }

    private I18nEntity createI18n(I18n i18n) {
        I18nEntity entity = new I18nEntity();
        entity.setId(i18n.id().value());
        entity.setDescription(i18n.description());
        entity.setJsonValues(i18n.jsonValues());
        return i18nRepository.save(entity);
    }

}
