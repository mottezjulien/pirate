package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
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
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.i18n.persistence.I18nRepository;
import org.springframework.stereotype.Component;

@Component
public class TemplateInitDataAdapter implements TemplateInitUseCase.OutPort {

    private final I18nRepository i18nRepository;

    private final TemplateRepository templateRepository;

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

    private final TemplateInitDataBoardAdapter boardAdapter;

    private final TemplateInitDataTalkAdapter talkAdapter;
    private final TemplateInitDataImageAdapter imageAdapter;

    public TemplateInitDataAdapter(I18nRepository i18nRepository, TemplateRepository templateRepository, ScenarioConfigRepository scenarioRepository, ScenarioStepRepository scenarioStepRepository, ScenarioTargetRepository scenarioTargetRepository, ScenarioPossibilityRepository possibilityRepository, ScenarioPossibilityRecurrenceRepository recurrenceRepository, ScenarioPossibilityTriggerRepository triggerRepository, ScenarioPossibilityConditionRepository conditionRepository, ScenarioPossibilityConsequenceRepository consequenceRepository, MapConfigRepository mapConfigRepository, MapItemRepository mapItemRepository, MapPositionRepository mapPositionRepository, TemplateInitDataBoardAdapter boardAdapter, TemplateInitDataTalkAdapter talkAdapter, TemplateInitDataImageAdapter imageAdapter) {
        this.i18nRepository = i18nRepository;
        this.templateRepository = templateRepository;
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
        this.boardAdapter = boardAdapter;
        this.talkAdapter = talkAdapter;
        this.imageAdapter = imageAdapter;
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

        talkAdapter.deleteAll();

        imageAdapter.deleteAll();

        scenarioTargetRepository.deleteAll();
        scenarioStepRepository.deleteAll();
        scenarioRepository.deleteAll();

        boardAdapter.deleteAll();

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
        templateEntity.setTalk(talkAdapter.createTalk(template.talk()));
        templateEntity.setBoard(boardAdapter.createBoard(template.board()));
        templateEntity.setImage(imageAdapter.createImage(template.image()));
        templateEntity.setScenario(createScenario(template.scenario()));
        templateEntity.setMap(createMap(template.map()));
        templateRepository.save(templateEntity);
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

    /*private TalkConfigEntity createTalk(TalkConfig talk) {
        TalkConfigEntity config = new TalkConfigEntity();
        config.setId(talk.id().value());
        talkConfigRepository.save(config);
        List<TalkCharacter.Reference> savedCharacterReferences = createTalkCharacterReferences(talk.items());
        talk.items().forEach(item -> createTalkItem(item, config, savedCharacterReferences));
        return config;
    }

    private List<TalkCharacter.Reference> createTalkCharacterReferences(List<TalkItem> items) {
        Map<TalkCharacter, List<TalkCharacter.Reference>> allSaved = new HashMap<>();
        for (TalkItem item: items) {
            TalkCharacter itemCharacter = item.character();
            TalkCharacter savedCharacter = allSaved.keySet().stream()
                    .filter(itemCharacter::hasSameName)
                    .findFirst()
                    .orElseGet(() -> {
                        TalkCharacterEntity entity = TalkCharacterEntity.fromModel(itemCharacter);
                        talkCharacterRepository.save(entity);
                        allSaved.put(itemCharacter, new ArrayList<>());
                        return itemCharacter;
                    });
            List<TalkCharacter.Reference> savedReferences = allSaved.get(savedCharacter);
            savedReferences.stream()
                    .filter(reference -> reference.hasSameValue(item.characterReference()))
                    .findFirst()
                    .orElseGet(() -> {
                        TalkCharacterReferenceEntity entity = TalkCharacterReferenceEntity.fromModel(item.characterReference());
                        talkCharacterReferenceRepository.save(entity);
                        savedReferences.add(item.characterReference());
                        allSaved.put(itemCharacter, savedReferences);
                    });
        }


    }

    private void createTalkItem(TalkItem item, TalkConfigEntity config, List<TalkCharacter.Reference> savedCharacterReferences) {
        I18nEntity value = createI18n(item.value());
        TalkItemEntity entity = switch (item) {
            case TalkItem.Simple ignored -> new TalkItemEntity();
            case TalkItem.Continue _continue -> {
                TalkItemContinueEntity continueEntity = new TalkItemContinueEntity();
                continueEntity.setNextId(_continue.nextId().value());
                yield continueEntity;
            }
            case TalkItem.Options model -> {
                TalkItemMultipleOptionsEntity optionsEntity = new TalkItemMultipleOptionsEntity();
                List<TalkItem.Options.Option> options = model.options().toList();
                for(int i = 0; i < options.size(); i++) {
                    optionsEntity.getOptions().add(createTalkOption(options.get(i), i));
                }
                yield optionsEntity;
            }
        };
        entity.setId(item.id().value());
        entity.setConfig(config);
        entity.setValue(value);
        entity.setCharacterReference(findTalkCharacterReference(item.characterReference(), savedCharacterReferences));
        talkItemRepository.save(entity);
    }

    private TalkCharacterReferenceEntity findTalkCharacterReference(TalkCharacter.Reference characterReference, List<TalkCharacter.Reference> savedCharacterReferences) {
        TalkCharacter.Reference foundSaved = savedCharacterReferences.stream().filter(savedReference -> characterReference.value().equals(savedReference.value())
                && characterReference.character().name().equals(savedReference.character().name())).findFirst().orElseThrow();
        TalkCharacterReferenceEntity entity = new TalkCharacterReferenceEntity();
        entity.setId(foundSaved.id().value());
        return entity;
    }

    private TalkOptionEntity createTalkOption(TalkItem.Options.Option talkOption, int index) {
        TalkOptionEntity entity = new TalkOptionEntity();
        entity.setId(talkOption.id().value());
        entity.setValue(createI18n(talkOption.value()));
        entity.setOrder(index);
        talkOption.optNextId().ifPresent(nextId -> entity.setNullableNextId(nextId.value()));
        return talkOptionItemRepository.save(entity);
    }*/


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
        return i18nRepository.save(I18nEntity.fromModel(i18n));
    }

}
