package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.commun.persistence.GameEntity;
import fr.plop.contexts.game.commun.persistence.GameProjectEntity;
import fr.plop.contexts.game.commun.persistence.GameProjectRepository;
import fr.plop.contexts.game.commun.persistence.GameRepository;
import fr.plop.contexts.game.config.Image.persistence.ImageGenericRepository;
import fr.plop.contexts.game.config.Image.persistence.ImageObjectRepository;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.*;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityConfigEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.ScenarioPossibilityConsequenceRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity.ConsequenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence.ScenarioPossibilityRecurrenceAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence.ScenarioPossibilityRecurrenceRepository;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.ScenarioPossibilityTriggerEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.ScenarioPossibilityTriggerRepository;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.TemplateInitUseCase;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import fr.plop.contexts.game.presentation.domain.Presentation;
import fr.plop.contexts.game.presentation.persistence.GamePresentationEntity;
import fr.plop.contexts.game.presentation.persistence.GamePresentationRepository;
import fr.plop.generic.position.Location;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.i18n.persistence.I18nRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TemplateInitDataAdapter implements TemplateInitUseCase.OutPort {

    private final I18nRepository i18nRepository;

    private final GameRepository gameRepository;
    private final GameProjectRepository gameProjectRepository;
    private final GamePresentationRepository presentationRepository;
    private final TemplateRepository templateRepository;
    private final ScenarioConfigRepository scenarioRepository;
    private final ScenarioStepRepository scenarioStepRepository;
    private final ScenarioTargetRepository scenarioTargetRepository;
    private final ScenarioPossibilityRepository possibilityRepository;
    private final ScenarioPossibilityRecurrenceRepository recurrenceRepository;
    private final ScenarioPossibilityTriggerRepository triggerRepository;
    private final ScenarioPossibilityConsequenceRepository consequenceRepository;
    private final TemplateInitDataBoardAdapter boardAdapter;
    private final TemplateInitDataMapAdapter mapAdapter;
    private final TemplateInitDataTalkAdapter talkAdapter;
    private final TemplateInitDataImageAdapter imageAdapter;

    private final TemplateInitDataInventoryAdapter inventoryAdapter;
    private final TemplateInitDataConditionAdapter conditionAdapter;

    private final ImageGenericRepository imageGenericRepository;
    private final ImageObjectRepository imageObjectRepository;

    public TemplateInitDataAdapter(I18nRepository i18nRepository, GameRepository gameRepository, GameProjectRepository gameProjectRepository, GamePresentationRepository presentationRepository, TemplateRepository templateRepository, ScenarioConfigRepository scenarioRepository, ScenarioStepRepository scenarioStepRepository, ScenarioTargetRepository scenarioTargetRepository, ScenarioPossibilityRepository possibilityRepository, ScenarioPossibilityRecurrenceRepository recurrenceRepository, ScenarioPossibilityTriggerRepository triggerRepository, ScenarioPossibilityConsequenceRepository consequenceRepository, TemplateInitDataBoardAdapter boardAdapter, TemplateInitDataMapAdapter mapAdapter, TemplateInitDataTalkAdapter talkAdapter, TemplateInitDataImageAdapter imageAdapter, TemplateInitDataInventoryAdapter inventoryAdapter, TemplateInitDataConditionAdapter conditionAdapter, ImageGenericRepository imageGenericRepository, ImageObjectRepository imageObjectRepository) {
        this.i18nRepository = i18nRepository;
        this.gameRepository = gameRepository;
        this.gameProjectRepository = gameProjectRepository;
        this.presentationRepository = presentationRepository;
        this.templateRepository = templateRepository;
        this.scenarioRepository = scenarioRepository;
        this.scenarioStepRepository = scenarioStepRepository;
        this.scenarioTargetRepository = scenarioTargetRepository;
        this.possibilityRepository = possibilityRepository;
        this.recurrenceRepository = recurrenceRepository;
        this.triggerRepository = triggerRepository;
        this.consequenceRepository = consequenceRepository;
        this.boardAdapter = boardAdapter;
        this.mapAdapter = mapAdapter;
        this.talkAdapter = talkAdapter;
        this.imageAdapter = imageAdapter;
        this.inventoryAdapter = inventoryAdapter;
        this.conditionAdapter = conditionAdapter;
        this.imageGenericRepository = imageGenericRepository;
        this.imageObjectRepository = imageObjectRepository;
    }


    @Override
    public boolean isEmpty() {
        return templateRepository.count() == 0;
    }


    @Override
    public void deleteAll() {

        presentationRepository.deleteAll();


        templateRepository.deleteAll();

        inventoryAdapter.deleteAll();

        mapAdapter.deleteAll();

        possibilityRepository.deleteAll();
        consequenceRepository.deleteAll();
        triggerRepository.deleteAll();
        recurrenceRepository.deleteAll();

        talkAdapter.deleteAll();

        imageAdapter.deleteAll();

        scenarioTargetRepository.deleteAll();
        scenarioStepRepository.deleteAll();
        scenarioRepository.deleteAll();

        boardAdapter.deleteAll();

        imageObjectRepository.deleteAll();
        imageGenericRepository.deleteAll();

        conditionAdapter.deleteAll();

        i18nRepository.deleteAll();

        gameRepository.deleteAll();
        gameProjectRepository.deleteAll();
    }

    @Override
    public Game.Id findOrCreateGame(GameProject.Code code, Game.Version version) {
        GameProjectEntity gameProjectEntity = gameProjectRepository.findByCode(code.value())
                .orElseGet(() -> {
                    GameProjectEntity entity = new GameProjectEntity();
                    entity.setId(StringTools.generate());
                    entity.setCode(code.value());
                    return gameProjectRepository.save(entity);
                });
        GameEntity gameEntity = gameRepository.findByProjectAndVersion(gameProjectEntity, version.value())
                .orElseGet(() -> {
                    GameEntity entity = new GameEntity();
                    entity.setId(StringTools.generate());
                    entity.setProject(gameProjectEntity);
                    entity.setVersion(version.value());
                    return gameRepository.save(entity);
                });
        return new Game.Id(gameEntity.getId());
    }

    public void createOrUpdate(Game.Id gameId, Presentation presentation) {
        GamePresentationEntity entity = presentationRepository
                .findByGameId(gameId.value()).stream()
                .findFirst()
                .orElseGet(() -> {
                    GamePresentationEntity newEntity = new GamePresentationEntity();
                    newEntity.setId(presentation.id().value());
                    newEntity.setGame(GameEntity.fromModelId(gameId));
                    return newEntity;
                });
        entity.setLabel(createI18n(presentation.label()));
        entity.setDescription(createI18n(presentation.description()));

        entity.setLevel(presentation.level().value());
        entity.setVisibility(presentation.visibility());

        entity.setParticipantMin(presentation.participantMin());
        entity.setParticipantMax(presentation.participantMax());
        entity.setParticipantTypes(new HashSet<>(presentation.participantTypes()));

        entity.setGameTypes(new HashSet<>(presentation.gameTypes()));

        //entity.setAuthor(); //TODO

        Location departure = presentation.departure();
        entity.setDepartureAddress(departure.address().toString());
        entity.setDepartureBottomLeftLat(departure.rectangle().bottomLeft().lat());
        entity.setDepartureBottomLeftLng(departure.rectangle().bottomLeft().lng());
        entity.setDepartureTopRightLat(departure.rectangle().topRight().lat());
        entity.setDepartureTopRightLng(departure.rectangle().topRight().lng());
        presentationRepository.save(entity);
    }

    @Override
    public void createOrUpdate(Game.Id gameId, Template template) {

        TemplateEntity templateEntity = new TemplateEntity();
        templateEntity.setId(template.id().value());

        templateEntity.setGame(GameEntity.fromModelId(gameId));

        /*templateEntity.setCode(template.code().value());
        templateEntity.setLabel(template.label());
        templateEntity.setVersion(template.version());

        templateEntity.setLevel(template.level().value());
        templateEntity.setDescription(template.description());

        Location departure = template.descriptor().departure();
        templateEntity.setDepartureAddress(departure.address().toString());
        templateEntity.setDepartureBottomLeftLat(departure.rect().bottomLeft().lat());
        templateEntity.setDepartureBottomLeftLng(departure.rect().bottomLeft().lng());
        templateEntity.setDepartureTopRightLat(departure.rect().topRight().lat());
        templateEntity.setDepartureTopRightLng(departure.rect().topRight().lng());*/

        templateEntity.setDurationInMinute(template.maxDuration().toMinutes());
        templateEntity.setTalk(talkAdapter.createTalk(template.talk()));
        templateEntity.setBoard(boardAdapter.createBoard(template.board()));
        templateEntity.setImage(imageAdapter.createImage(template.image()));
        templateEntity.setScenario(createScenario(template.scenario()));
        templateEntity.setMap(mapAdapter.create(template.map()));
        templateEntity.setInventory(inventoryAdapter.create(template.inventory()));
        templateRepository.save(templateEntity);
    }

    private ScenarioConfigEntity createScenario(ScenarioConfig scenario) {
        ScenarioConfigEntity scenarioEntity = new ScenarioConfigEntity();
        scenarioEntity.setId(scenario.id().value());
        scenarioEntity.setLabel(scenario.label());
        scenarioRepository.save(scenarioEntity);
        scenario.genericPossibilities().forEach(possibility -> createPossibility(possibility, scenarioEntity, Optional.empty()));

        AtomicInteger order = new AtomicInteger();
        scenario.steps().forEach(step -> {
            ScenarioStepEntity stepEntity = new ScenarioStepEntity();
            stepEntity.setId(step.id().value());
            stepEntity.setLabel(createI18n(step.label()));
            stepEntity.setScenario(scenarioEntity);
            stepEntity.setOrder(order.getAndIncrement());
            scenarioStepRepository.save(stepEntity);
            step.targets().forEach(target -> createTarget(target, stepEntity));
            step.possibilities().forEach(possibility -> createPossibility(possibility, scenarioEntity, Optional.of(stepEntity)));
        });
        return scenarioEntity;
    }

    private void createTarget(ScenarioConfig.Target target, ScenarioStepEntity stepEntity) {
        ScenarioTargetEntity targetEntity = new ScenarioTargetEntity();
        targetEntity.setId(target.id().value());
        targetEntity.setLabel(createI18n(target.label()));
        target.optDescription().ifPresent(desc -> targetEntity.setDescription(createI18n(desc)));
        targetEntity.setStep(stepEntity);
        targetEntity.setOptional(target.optional());
        scenarioTargetRepository.save(targetEntity);
    }

    private void createPossibility(Possibility possibility, ScenarioConfigEntity scenarioEntity, Optional<ScenarioStepEntity> optStepEntity) {

        ScenarioPossibilityAbstractEntity possibilityEntity;
        if (optStepEntity.isPresent()) {
            ScenarioPossibilityStepEntity stepPossibility = new ScenarioPossibilityStepEntity();
            stepPossibility.setStep(optStepEntity.get());
            possibilityEntity = stepPossibility;
        } else {
            ScenarioPossibilityConfigEntity configPossibility = new ScenarioPossibilityConfigEntity();
            configPossibility.setConfig(scenarioEntity);
            possibilityEntity = configPossibility;
        }
        possibilityEntity.setId(possibility.id().value());
        possibilityRepository.save(possibilityEntity);

        ScenarioPossibilityRecurrenceAbstractEntity recurrenceEntity = ScenarioPossibilityRecurrenceAbstractEntity.fromModel(possibility.recurrence());
        possibilityEntity.setRecurrence(recurrenceRepository.save(recurrenceEntity));

        ScenarioPossibilityTriggerEntity triggerEntity = persistTriggerRecursive(possibility.trigger());
        possibilityEntity.setTrigger(triggerEntity);

        possibility.optCondition()
                .ifPresent(condition -> possibilityEntity.setNullableCondition(conditionAdapter.create(condition)));

        possibility.consequences().forEach(consequence -> {
            ConsequenceAbstractEntity consequenceEntity = ConsequenceAbstractEntity.fromModel(consequence);
            if (consequence instanceof Consequence.DisplayAlert message) {
                createI18n(message.value());
            }
            if (consequence instanceof Consequence.DisplayConfirm confirm) {
                createI18n(confirm.message());
            }
            possibilityEntity.getConsequences().add(consequenceRepository.save(consequenceEntity));
        });

        possibilityRepository.save(possibilityEntity);
    }

    private ScenarioPossibilityTriggerEntity persistTriggerRecursive(PossibilityTrigger trigger) {
        ScenarioPossibilityTriggerEntity triggerEntity = ScenarioPossibilityTriggerEntity.fromModel(trigger);

        switch (trigger) {
            case PossibilityTrigger.And and -> {
                triggerEntity.getSubs().clear();
                and.triggers().forEach(child -> {
                    ScenarioPossibilityTriggerEntity childEntity = persistTriggerRecursive(child);
                    triggerEntity.getSubs().add(childEntity);
                });
            }
            case PossibilityTrigger.Or or -> {
                triggerEntity.getSubs().clear();
                or.triggers().forEach(child -> {
                    ScenarioPossibilityTriggerEntity childEntity = persistTriggerRecursive(child);
                    triggerEntity.getSubs().add(childEntity);
                });
            }
            case PossibilityTrigger.Not not -> {
                triggerEntity.getSubs().clear();
                ScenarioPossibilityTriggerEntity childEntity = persistTriggerRecursive(not.trigger());
                triggerEntity.getSubs().add(childEntity);
            }
            default -> {
            }
        }

        return triggerRepository.save(triggerEntity);
    }

    private I18nEntity createI18n(I18n i18n) {
        return i18nRepository.save(I18nEntity.fromModel(i18n));
    }

}
