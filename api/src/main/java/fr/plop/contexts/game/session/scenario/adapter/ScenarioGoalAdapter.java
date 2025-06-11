package fr.plop.contexts.game.session.scenario.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class ScenarioGoalAdapter {
    private final ScenarioGoalRepository goalRepository;

    public ScenarioGoalAdapter(ScenarioGoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public void updateStateOrCreateGoal(GamePlayer.Id playerId, PossibilityConsequence.Goal consequence) {
        Optional<ScenarioGoalEntity> optEntity = goalRepository.byPlayerIdAndStepId(playerId.value(), consequence.stepId().value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(consequence.state());
            goalRepository.save(entity);
        }, () -> create(playerId, consequence));
    }

    private void create(GamePlayer.Id playerId, PossibilityConsequence.Goal consequence) {
        ScenarioGoalEntity entity = new ScenarioGoalEntity();
        entity.setId(StringTools.generate());
        entity.setState(consequence.state());

        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);

        ScenarioStepEntity stepEntity = new ScenarioStepEntity();
        stepEntity.setId(consequence.stepId().value());
        entity.setStep(stepEntity);

        goalRepository.save(entity);
    }

}
