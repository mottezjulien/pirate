package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.persistence.entity.BoardConfigEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardRectEntity;
import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.board.persistence.repository.BoardConfigRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardRectRepository;
import fr.plop.contexts.game.config.board.persistence.repository.BoardSpaceRepository;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.map.persistence.*;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.*;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.condition.ScenarioPossibilityConditionEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.condition.ScenarioPossibilityConditionRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.ScenarioPossibilityConsequenceRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity.ScenarioPossibilityConsequenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence.ScenarioPossibilityRecurrenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence.ScenarioPossibilityRecurrenceRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.ScenarioPossibilityTriggerEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.ScenarioPossibilityTriggerRepository;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.persistence.*;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.i18n.persistence.I18nRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateInitDataAdapter implements TemplateInitUseCase.OutPort {

    private final I18nRepository i18nRepository;

    private final TalkItemMultipleOptionsRepository talkOptionsRepository;
    private final TalkOptionRepository talkOptionItemRepository;

    private final TemplateRepository templateRepository;

    private final BoardConfigRepository boardRepository;
    private final BoardSpaceRepository boardSpaceRepository;
    private final BoardRectRepository boardRectRepository;

    private final ScenarioConfigRepository scenarioRepository;
    private final ScenarioStepRepository scenarioStepRepository;
    private final ScenarioTargetRepository scenarioTargetRepository;
    private final ScenarioPossibilityRepository possibilityRepository;
    private final ScenarioPossibilityRecurrenceRepository recurrenceRepository;
    private final ScenarioPossibilityTriggerRepository triggerRepository;
    private final ScenarioPossibilityConditionRepository conditionRepository;
    private final ScenarioPossibilityConsequenceRepository consequenceRepository;

    private final MapConfigRepository mapConfigRepository;
    private final MapItemRepository mapItemRepository;
    private final MapPositionRepository mapPositionRepository;

    private final TalkItemRepository talkItemRepository;
    private final TalkConfigRepository talkConfigRepository;

    public TemplateInitDataAdapter(I18nRepository i18nRepository, TalkItemMultipleOptionsRepository talkOptionsRepository, TalkOptionRepository talkOptionItemRepository, TemplateRepository templateRepository, BoardConfigRepository boardRepository, BoardSpaceRepository boardSpaceRepository, BoardRectRepository boardRectRepository, ScenarioConfigRepository scenarioRepository, ScenarioStepRepository scenarioStepRepository, ScenarioTargetRepository scenarioTargetRepository, ScenarioPossibilityRepository possibilityRepository, ScenarioPossibilityRecurrenceRepository recurrenceRepository, ScenarioPossibilityTriggerRepository triggerRepository, ScenarioPossibilityConditionRepository conditionRepository, ScenarioPossibilityConsequenceRepository consequenceRepository, MapConfigRepository mapConfigRepository, MapItemRepository mapItemRepository, MapPositionRepository mapPositionRepository, TalkItemRepository talkItemRepository, TalkConfigRepository talkConfigRepository) {
        this.i18nRepository = i18nRepository;
        this.talkOptionsRepository = talkOptionsRepository;
        this.talkOptionItemRepository = talkOptionItemRepository;
        this.templateRepository = templateRepository;
        this.boardRepository = boardRepository;
        this.boardSpaceRepository = boardSpaceRepository;
        this.boardRectRepository = boardRectRepository;
        this.scenarioRepository = scenarioRepository;
        this.scenarioStepRepository = scenarioStepRepository;
        this.scenarioTargetRepository = scenarioTargetRepository;
        this.possibilityRepository = possibilityRepository;
        this.recurrenceRepository = recurrenceRepository;
        this.triggerRepository = triggerRepository;
        this.conditionRepository = conditionRepository;
        this.consequenceRepository = consequenceRepository;
        this.mapConfigRepository = mapConfigRepository;
        this.mapItemRepository = mapItemRepository;
        this.mapPositionRepository = mapPositionRepository;
        this.talkItemRepository = talkItemRepository;
        this.talkConfigRepository = talkConfigRepository;
    }


    @Override
    public boolean isEmpty() {
        return templateRepository.count() == 0;
    }

    @Override
    public void deleteAll() {
        templateRepository.deleteAll();

        mapPositionRepository.deleteAll();
        mapItemRepository.deleteAll();
        mapConfigRepository.deleteAll();

        possibilityRepository.deleteAll();
        conditionRepository.deleteAll();
        consequenceRepository.deleteAll();
        triggerRepository.deleteAll();
        recurrenceRepository.deleteAll();

        // Purge Talk data (order matters: join -> options -> items -> config)
        talkOptionsRepository.deleteAll();
        talkOptionItemRepository.deleteAll();
        talkItemRepository.deleteAll();
        talkConfigRepository.deleteAll();

        scenarioTargetRepository.deleteAll();
        scenarioStepRepository.deleteAll();
        scenarioRepository.deleteAll();

        boardRectRepository.deleteAll();
        boardSpaceRepository.deleteAll();
        boardRepository.deleteAll();

        i18nRepository.deleteAll();
    }

    @Override
    public void create(Template template) {
        TemplateEntity templateEntity = new TemplateEntity();
        templateEntity.setId(template.id().value());
        templateEntity.setCode(template.code().value());
        templateEntity.setLabel(template.label());
        templateEntity.setVersion(template.version());
        templateEntity.setDurationInMinute(template.maxDuration().toMinutes());
        // Persist Talk first so consequences can reference existing TalkItemEntity
        templateEntity.setTalk(createTalk(template.talk()));
        templateEntity.setBoard(createBoard(template.board()));
        templateEntity.setScenario(createScenario(template.scenario()));
        templateEntity.setMap(createMap(template.map()));
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
        targetEntity.setId(target.id().value());
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

        ScenarioPossibilityRecurrenceAbstractEntity recurrenceEntity = ScenarioPossibilityRecurrenceAbstractEntity.fromModel(possibility.recurrence());
        possibilityEntity.setRecurrence(recurrenceRepository.save(recurrenceEntity));

        ScenarioPossibilityTriggerEntity triggerEntity = ScenarioPossibilityTriggerEntity.fromModel(possibility.trigger());
        possibilityEntity.setTrigger(triggerRepository.save(triggerEntity));

        possibility.conditions().forEach(condition -> {
            ScenarioPossibilityConditionEntity conditionEntity = ScenarioPossibilityConditionEntity
                    .fromModel(condition);
            possibilityEntity.getConditions().add(conditionRepository.save(conditionEntity));
        });

        possibility.consequences().forEach(consequence -> {
            ScenarioPossibilityConsequenceAbstractEntity consequenceEntity = ScenarioPossibilityConsequenceAbstractEntity.fromModel(consequence);
            if (consequence instanceof Consequence.DisplayMessage message) {
                createI18n(message.value());
            }
            possibilityEntity.getConsequences().add(consequenceRepository.save(consequenceEntity));
        });

        possibilityRepository.save(possibilityEntity);
    }

    private TalkConfigEntity createTalk(TalkConfig talk) {
        TalkConfigEntity config = new TalkConfigEntity();
        config.setId(talk.id().value());
        talkConfigRepository.save(config);
        talk.items().forEach(item -> createTalkItem(item, config));
        return config;
    }

    private void createTalkItem(TalkItem item, TalkConfigEntity config) {
        if (item instanceof TalkItem.Simple simple) {
            I18nEntity value = createI18n(simple.value());
            TalkItemEntity e = new TalkItemEntity();
            e.setId(simple.id().value());
            e.setConfig(config);
            e.setValue(value);
            talkItemRepository.save(e);
        } else if (item instanceof TalkItem.Options mo) {
            TalkItemMultipleOptionsEntity e = new TalkItemMultipleOptionsEntity();
            e.setId(mo.id().value());
            e.setConfig(config);
            e.setValue(createI18n(mo.value()));
            List<TalkItem.Options.Option> options = mo.options().toList();
            for(int i = 0; i < options.size(); ++i) {
                e.getOptions().add(createTalkOption(options.get(i), i));
            }
            talkOptionsRepository.save(e);
        }
    }

    private TalkOptionEntity createTalkOption(TalkItem.Options.Option talkOption, int index) {
        TalkOptionEntity entity = new TalkOptionEntity();
        entity.setId(talkOption.id().value());
        entity.setValue(createI18n(talkOption.value()));
        entity.setOrder(index);
        talkOption.optNextId().ifPresent(nextId -> entity.setNullableNextId(nextId.value()));
        return talkOptionItemRepository.save(entity);
    }




    private MapConfigEntity createMap(MapConfig mapConfig) {
        MapConfigEntity mapConfigEntity = new MapConfigEntity();
        mapConfigEntity.setId(mapConfig.id().value());
        mapConfigRepository.save(mapConfigEntity);
        mapConfig.items().forEach(item -> {
            MapItemEntity mapItemEntity = new MapItemEntity();
            mapItemEntity.setId(item.id().value());
            mapItemEntity.setLabel(createI18n(item.label()));
            MapItem.Image image = item.image();
            mapItemEntity.setImageType(image.type());
            mapItemEntity.setImageValue(image.value());
            mapItemEntity.setImageSizeWidth(image.size().width());
            mapItemEntity.setImageSizeHeight(image.size().height());

            mapItemEntity.setPriority(item.priority());
            mapItemRepository.save(mapItemEntity);

            item.positions().forEach(position -> {
                MapPositionEntity entity = new MapPositionEntity();
                entity.setId(StringTools.generate());
                entity.setLabel(position.label());
                entity.setMap(mapItemEntity);

                switch (position) {
                    case MapItem.Position.Zone zone -> {
                        entity.setType(MapPositionEntity.Type.ZONE);
                        entity.setTop(zone.top());
                        entity.setLeft(zone.left());
                        entity.setBottom(zone.bottom());
                        entity.setRight(zone.right());
                    }
                    case MapItem.Position.Point point -> {
                        entity.setType(MapPositionEntity.Type.POINT);
                        entity.setX(point.x());
                        entity.setY(point.y());
                    }
                }
                mapPositionRepository.save(entity);

                position.spaceIds().forEach(boardId -> {
                    BoardSpaceEntity space = new BoardSpaceEntity();
                    space.setId(boardId.value());
                    entity.getSpaces().add(space);
                });
                mapPositionRepository.save(entity);
            });

        });

        return mapConfigEntity;
    }

    private I18nEntity createI18n(I18n i18n) {
        I18nEntity entity = new I18nEntity();
        entity.setId(i18n.id().value());
        entity.setDescription(i18n.description());
        entity.setJsonValues(i18n.jsonValues());
        return i18nRepository.save(entity);
    }

}
