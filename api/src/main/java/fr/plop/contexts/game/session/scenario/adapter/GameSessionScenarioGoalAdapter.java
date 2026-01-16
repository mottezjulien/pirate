package fr.plop.contexts.game.session.scenario.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestratorLazy;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class GameSessionScenarioGoalAdapter implements GameSessionScenarioGoalPort {
    private final GameEventOrchestratorLazy orchestrator;
    private final ScenarioGoalStepRepository goalStepRepository;
    private final ScenarioGoalTargetRepository goalTargetRepository;

    public GameSessionScenarioGoalAdapter(GameEventOrchestratorLazy orchestrator, ScenarioGoalStepRepository goalStepRepository, ScenarioGoalTargetRepository goalTargetRepository) {
        this.orchestrator = orchestrator;
        this.goalStepRepository = goalStepRepository;
        this.goalTargetRepository = goalTargetRepository;
    }

    @Override
    public ScenarioSessionPlayer findByPlayerId(GamePlayer.Id playerId) {
        return new ScenarioSessionPlayer(findSteps(playerId), findTargets(playerId));
    }

    public List<ScenarioConfig.Step.Id> findActiveSteps(GamePlayer.Id playerId) {
        return findSteps(playerId).entrySet().stream()
                .filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE)
                .map(Map.Entry::getKey).toList();
    }

    @Override
    public List<ScenarioConfig.Target.Id> findActiveTargets(GamePlayer.Id playerId) {
        return findTargets(playerId).entrySet().stream()
                .filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE || entry.getValue() == ScenarioSessionState.SUCCESS)
                .map(Map.Entry::getKey).toList();
    }

    public Map<ScenarioConfig.Step.Id, ScenarioSessionState> findSteps(GamePlayer.Id playerId) {
        return goalStepRepository.byPlayerIdFetchStep(playerId.value())
                .stream().collect(Collectors.toMap(
                        entity -> new ScenarioConfig.Step.Id(entity.getStep().getId()),
                        ScenarioGoalStepEntity::getState,
                        (existing, replacement) -> existing));
    }

    public Map<ScenarioConfig.Target.Id, ScenarioSessionState> findTargets(GamePlayer.Id playerId) {
        return goalTargetRepository.byPlayerIdFetchTarget(playerId.value())
                .stream().collect(Collectors.toMap(
                        entity -> new ScenarioConfig.Target.Id(entity.getTarget().getId()),
                        ScenarioGoalTargetEntity::getState,
                        (existing, replacement) -> existing));
    }

    public void saveStep(GameSessionContext context, ScenarioConfig.Step.Id stepId, ScenarioSessionState state) {
        Optional<ScenarioGoalStepEntity> optEntity = goalStepRepository.byPlayerIdAndStepId(context.playerId().value(), stepId.value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(state);
            goalStepRepository.save(entity);
        }, () -> goalStepRepository.save(ScenarioGoalStepEntity.build(context.playerId(), stepId, state)));
        if (state == ScenarioSessionState.ACTIVE) {
            orchestrator.fire(context, new GameEvent.GoalActive(stepId));
        }
    }

    public void saveTarget(GamePlayer.Id playerId, ScenarioConfig.Target.Id targetId, ScenarioSessionState state) {
        Optional<ScenarioGoalTargetEntity> optEntity = goalTargetRepository.byPlayerIdAndTargetId(playerId.value(), targetId.value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(state);
            goalTargetRepository.save(entity);
            
            if (state == ScenarioSessionState.SUCCESS) {
                String stepId = entity.getTarget().getStep().getId();
                List<ScenarioGoalTargetEntity> otherTargets = goalTargetRepository.byPlayerIdAndStepIdFetchTarget(playerId.value(), stepId);
                otherTargets.stream()
                    .filter(t -> !t.getTarget().getId().equals(targetId.value()))
                    .filter(t -> t.getState() != ScenarioSessionState.SUCCESS)
                    .forEach(t -> {
                        t.setState(ScenarioSessionState.FAILURE);
                        goalTargetRepository.save(t);
                    });
            }
        }, () -> goalTargetRepository.save(ScenarioGoalTargetEntity.build(playerId, targetId, state)));
    }

}
