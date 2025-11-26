package fr.plop.contexts.game.session.scenario.adapter;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.scenario.domain.usecase.ScenarioSessionPlayerGetUseCase;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetRepository;
import org.springframework.stereotype.Component;


import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScenarioSessionPlayerGetAdapter implements ScenarioSessionPlayerGetUseCase.Port {

    private final GameConfigCache cache;
    private final ScenarioGoalStepRepository stepRepository;
    private final ScenarioGoalTargetRepository targetRepository;

    public ScenarioSessionPlayerGetAdapter(GameConfigCache cache, ScenarioGoalStepRepository stepRepository, ScenarioGoalTargetRepository targetRepository) {
        this.cache = cache;
        this.stepRepository = stepRepository;
        this.targetRepository = targetRepository;
    }

    @Override
    public ScenarioConfig scenario(GameSession.Id sessionId) {
        return cache.scenario(sessionId);
    }

    @Override
    public Map<ScenarioConfig.Step.Id, ScenarioSessionState> steps(GamePlayer.Id playerId) {
        return stepRepository.byPlayerIdFetchStep(playerId.value())
                .stream().collect(Collectors.toMap(
                        entity -> new ScenarioConfig.Step.Id(entity.getStep().getId()),
                        ScenarioGoalStepEntity::getState));
    }

    @Override
    public Map<ScenarioConfig.Target.Id, ScenarioSessionState> targets(GamePlayer.Id playerId) {
        return targetRepository.byPlayerIdFetchTarget(playerId.value())
                .stream().collect(Collectors.toMap(
                entity -> new ScenarioConfig.Target.Id(entity.getTarget().getId()),
                ScenarioGoalTargetEntity::getState));
    }
}
