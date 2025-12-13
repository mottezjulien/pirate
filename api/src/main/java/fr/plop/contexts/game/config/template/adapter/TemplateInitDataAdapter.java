package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.Image.persistence.ImageGenericRepository;
import fr.plop.contexts.game.config.Image.persistence.ImageObjectRepository;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.*;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
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
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.i18n.persistence.I18nRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

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
    private final ScenarioPossibilityConsequenceRepository consequenceRepository;
    private final TemplateInitDataBoardAdapter boardAdapter;
    private final TemplateInitDataMapAdapter mapAdapter;
    private final TemplateInitDataTalkAdapter talkAdapter;
    private final TemplateInitDataImageAdapter imageAdapter;
    private final TemplateInitDataConditionAdapter conditionAdapter;

    private final ImageGenericRepository imageGenericRepository;
    private final ImageObjectRepository imageObjectRepository;

    public TemplateInitDataAdapter(I18nRepository i18nRepository, TemplateRepository templateRepository, ScenarioConfigRepository scenarioRepository, ScenarioStepRepository scenarioStepRepository, ScenarioTargetRepository scenarioTargetRepository, ScenarioPossibilityRepository possibilityRepository, ScenarioPossibilityRecurrenceRepository recurrenceRepository, ScenarioPossibilityTriggerRepository triggerRepository, ScenarioPossibilityConsequenceRepository consequenceRepository, TemplateInitDataBoardAdapter boardAdapter, TemplateInitDataMapAdapter mapAdapter, TemplateInitDataTalkAdapter talkAdapter, TemplateInitDataImageAdapter imageAdapter, TemplateInitDataConditionAdapter conditionAdapter, ImageGenericRepository imageGenericRepository, ImageObjectRepository imageObjectRepository) {
        this.i18nRepository = i18nRepository;
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
        templateRepository.deleteAll();

        mapAdapter.deleteAll();

        possibilityRepository.deleteAll();
        consequenceRepository.deleteAll();
        triggerRepository.deleteAll();
        recurrenceRepository.deleteAll();
        conditionAdapter.deleteAll();

        talkAdapter.deleteAll();

        imageAdapter.deleteAll();

        scenarioTargetRepository.deleteAll();
        scenarioStepRepository.deleteAll();
        scenarioRepository.deleteAll();

        boardAdapter.deleteAll();

        imageObjectRepository.deleteAll();
        imageGenericRepository.deleteAll();

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
        templateEntity.setMap(mapAdapter.create(template.map()));
        templateRepository.save(templateEntity);
    }

    private ScenarioConfigEntity createScenario(ScenarioConfig scenario) {
        ScenarioConfigEntity scenarioEntity = new ScenarioConfigEntity();
        scenarioEntity.setId(scenario.id().value());
        scenarioEntity.setLabel(scenario.label());
        scenarioRepository.save(scenarioEntity);

        AtomicInteger order = new AtomicInteger();
        scenario.steps().forEach(step -> {
            ScenarioStepEntity stepEntity = new ScenarioStepEntity();
            stepEntity.setId(step.id().value());
            stepEntity.setLabel(createI18n(step.label()));
            stepEntity.setScenario(scenarioEntity);
            stepEntity.setOrder(order.getAndIncrement());
            scenarioStepRepository.save(stepEntity);
            step.targets().forEach(target -> createTarget(target, stepEntity));
            step.possibilities().forEach(possibility -> createPossibility(possibility, stepEntity));
        });
        return scenarioEntity;
    }

    private void createTarget(ScenarioConfig.Target target, ScenarioStepEntity stepEntity) {
        ScenarioTargetEntity targetEntity = new ScenarioTargetEntity();
        targetEntity.setId(target.id().value());
        targetEntity.setLabel(createI18n(target.label()));
        target.desc().ifPresent(desc ->
                targetEntity.setDescription(createI18n(desc)));
        targetEntity.setStep(stepEntity);
        targetEntity.setOptional(target.optional());
        scenarioTargetRepository.save(targetEntity);
    }

    private void createPossibility(Possibility possibility, ScenarioStepEntity stepEntity) {

        ScenarioPossibilityEntity possibilityEntity = new ScenarioPossibilityEntity();
        possibilityEntity.setId(possibility.id().value());
        possibilityEntity.setStep(stepEntity);
        possibilityRepository.save(possibilityEntity);

        ScenarioPossibilityRecurrenceAbstractEntity recurrenceEntity = ScenarioPossibilityRecurrenceAbstractEntity.fromModel(possibility.recurrence());
        possibilityEntity.setRecurrence(recurrenceRepository.save(recurrenceEntity));

        ScenarioPossibilityTriggerEntity triggerEntity = ScenarioPossibilityTriggerEntity.fromModel(possibility.trigger());
        possibilityEntity.setTrigger(triggerRepository.save(triggerEntity));

        possibility.optCondition()
                .ifPresent(condition -> possibilityEntity.setNullableCondition(conditionAdapter.create(condition)));

        possibility.consequences().forEach(consequence -> {
            ScenarioPossibilityConsequenceAbstractEntity consequenceEntity = ScenarioPossibilityConsequenceAbstractEntity.fromModel(consequence);
            if (consequence instanceof Consequence.DisplayMessage message) {
                createI18n(message.value());
            }
            possibilityEntity.getConsequences().add(consequenceRepository.save(consequenceEntity));
        });

        possibilityRepository.save(possibilityEntity);
    }

    private I18nEntity createI18n(I18n i18n) {
        return i18nRepository.save(I18nEntity.fromModel(i18n));
    }

}
