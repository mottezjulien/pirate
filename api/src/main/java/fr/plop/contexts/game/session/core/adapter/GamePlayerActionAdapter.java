package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GamePlayerActionAdapter implements GamePlayerActionPort {

    private final GamePlayerActionRepository actionRepository;

    public GamePlayerActionAdapter(GamePlayerActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    @Override
    public List<GameAction> findByPlayerId(GamePlayer.Id id) {
        return actionRepository.fullByPlayerId(id.value())
                .stream().map(GamePlayerActionEntity::toModel).toList();
    }

    @Override
    public void save(GamePlayer.Id playerId, Possibility.Id possibilityId, GameSessionTimeUnit timeUnit) {
        GamePlayerActionEntity entity = new GamePlayerActionEntity();
        entity.setId(StringTools.generate());
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);
        ScenarioPossibilityEntity possibilityEntity = new ScenarioPossibilityEntity();
        possibilityEntity.setId(possibilityId.value());
        entity.setPossibility(possibilityEntity);
        entity.setDate(Instant.now());
        entity.setTimeInMinutes(timeUnit.toMinutes());
        actionRepository.save(entity);
    }
}
