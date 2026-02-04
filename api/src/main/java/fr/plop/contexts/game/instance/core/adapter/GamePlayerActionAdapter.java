package fr.plop.contexts.game.instance.core.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityAbstractEntity;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityRepository;
import fr.plop.contexts.game.instance.core.domain.model.GameAction;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerActionEntity;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GamePlayerActionAdapter implements GamePlayerActionPort {

    private final GamePlayerActionRepository actionRepository;
    private final ScenarioPossibilityRepository possibilityRepository;

    public GamePlayerActionAdapter(GamePlayerActionRepository actionRepository, ScenarioPossibilityRepository possibilityRepository) {
        this.actionRepository = actionRepository;
        this.possibilityRepository = possibilityRepository;
    }

    @Override
    public List<GameAction> findByPlayerId(GamePlayer.Id id) {
        return actionRepository.fullByPlayerId(id.value())
                .stream().map(GamePlayerActionEntity::toModel).toList();
    }

    @Override
    public void save(GamePlayer.Id playerId, Possibility.Id possibilityId, GameInstanceTimeUnit timeUnit) {
        GamePlayerActionEntity entity = new GamePlayerActionEntity();
        entity.setId(StringTools.generate());
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);
        ScenarioPossibilityAbstractEntity possibilityEntity = possibilityRepository.getReferenceById(possibilityId.value());
        entity.setPossibility(possibilityEntity);
        entity.setDate(Instant.now());
        entity.setTimeInMinutes(timeUnit.toMinutes());
        actionRepository.save(entity);
    }
}
