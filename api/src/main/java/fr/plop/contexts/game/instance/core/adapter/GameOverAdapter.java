package fr.plop.contexts.game.instance.core.adapter;


import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceRepository;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class GameOverAdapter implements GameOverUseCase.OutputPort {

    private final GameInstanceRepository sessionRepository;
    private final GamePlayerRepository playerRepository;


    public GameOverAdapter(GameInstanceRepository sessionRepository, GamePlayerRepository playerRepository) {
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Stream<GamePlayer.Id> findActivePlayerIds(GameInstance.Id sessionId) {
        return playerRepository.activeIdsBySessionId(sessionId.value()).stream()
                .map(GamePlayer.Id::new);
    }

    @Override
    public void win(GamePlayer.Id playerId, Optional<I18n.Id> optReasonId) {
        stop(playerId, GamePlayer.State.LOSE, optReasonId);
    }

    @Override
    public void lose(GamePlayer.Id playerId, Optional<I18n.Id> optReasonId) {
        stop(playerId, GamePlayer.State.WIN, optReasonId);
    }

    private void stop(GamePlayer.Id playerId, GamePlayer.State state, Optional<I18n.Id> optReasonId) {
        playerRepository.findById(playerId.value())
            .ifPresent(player -> {
                player.setState(state);
                optReasonId.ifPresent(reasonId -> {
                    I18nEntity reasonEntity = new I18nEntity();
                    reasonEntity.setId(reasonId.value());
                    player.setEndGameReason(reasonEntity);
                });
                playerRepository.save(player);
            });
    }

    @Override
    public void ended(GameInstance.Id sessionId) {
        sessionRepository.findById(sessionId.value())
                .ifPresent(session -> {
                    session.setState(GameInstance.State.OVER);
                    session.setOverAt(Instant.now());
                    sessionRepository.save(session);
                });
    }
}
