package fr.plop.contexts.game.session.core.adapter;


import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Stream;

@Component
public class GameOverAdapter implements GameOverUseCase.OutputPort {

    private final GameSessionRepository sessionRepository;
    private final GamePlayerRepository playerRepository;


    public GameOverAdapter(GameSessionRepository sessionRepository, GamePlayerRepository playerRepository) {
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Stream<GamePlayer.Id> findActivePlayerIds(GameSession.Id sessionId) {
        return playerRepository.activeIdsBySessionId(sessionId.value()).stream()
                .map(GamePlayer.Id::new);
    }

    @Override
    public void win(GamePlayer.Id playerId, I18n.Id reasonId) {

        playerRepository.findById(playerId.value())
                .ifPresent(player -> {
                    player.setState(GamePlayer.State.WIN);
                    I18nEntity reasonEntity = new I18nEntity();
                    reasonEntity.setId(reasonId.value());
                    player.setEndGameReason(reasonEntity);
                    playerRepository.save(player);
                });
    }

    @Override
    public void ended(GameSession.Id sessionId) {
        sessionRepository.findById(sessionId.value())
                .ifPresent(session -> {
                    session.setState(GameSession.State.OVER);
                    session.setOverAt(Instant.now());
                    sessionRepository.save(session);
                });
    }
}
