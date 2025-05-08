package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

@Component
public class GameEventActionScenarioGoal {
    private final ScenarioGoalRepository goalRepository;

    public GameEventActionScenarioGoal(ScenarioGoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public void updateOrCreate(GamePlayer.Id playerId, PossibilityConsequence.Goal consequence) {
        updateStateOrCreate(playerId, consequence.stepId(), consequence.state());

    }

    private void updateStateOrCreate(GamePlayer.Id playerId, ScenarioConfig.Step.Id id, ScenarioGoal.State state) {
        goalRepository.byPlayerIdAndStepId(playerId.value(), id.value())
                .ifPresentOrElse(goalEntity -> updateState(goalEntity, state),
                        () -> create(playerId, id, state));
    }

    private void updateState(ScenarioGoalEntity goalEntity, ScenarioGoal.State state) {
        goalEntity.setState(state);
        goalRepository.save(goalEntity);
    }

    private void create(GamePlayer.Id playerId, ScenarioConfig.Step.Id stepId, ScenarioGoal.State state) {
        ScenarioGoalEntity entity = new ScenarioGoalEntity();
        entity.setId(StringTools.generate());
        entity.setState(state);

        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);

        ScenarioStepEntity stepEntity = new ScenarioStepEntity();
        stepEntity.setId(stepId.value());
        entity.setStep(stepEntity);

        goalRepository.save(entity);
    }



}
