package fr.plop;

import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import fr.plop.contexts.game.session.adapter.GameSessionTimerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class GameSpringConfig {

    @Bean
    public ConnectionCreateAuthUseCase connectionCreateAuthUseCase(ConnectionCreateAuthUseCase.DataOutPort port) {
        return new ConnectionCreateAuthUseCase(port);
    }

    @Bean
    public ConnectUseCase connectUseCase(ConnectUseCase.OutPort port) {
        return new ConnectUseCase(port);
    }

    @Bean
    public GameSessionCreateUseCase gameCreateUseCase(GameSessionCreateUseCase.DataOutput dataOutput) {
        return new GameSessionCreateUseCase(dataOutput);
    }

    @Bean
    public GameEventBroadCast gameEventBroadCast(GameEventBroadCastIntern.OutPort outPort) {
        return new GameEventBroadCastIntern(outPort);
    }

    @Bean
    public GameMoveUseCase gameMoveUseCase(GameMoveUseCase.OutPort outPort, GameEventBroadCast broadCast, PushPort pushPort, GameSessionTimer timer) {
        return new GameMoveUseCase(outPort, broadCast, pushPort, timer);
    }

    @Bean
    public GameOverUseCase gameOverUseCase(GameOverUseCase.OutputPort outputPort, PushPort pushPort) {
        return new GameOverUseCase(outputPort, pushPort);
    }

    @Lazy
    @Bean
    public GameSessionTimer gameSessionTimer(GamePlayerRepository gamePlayerRepository, GameEventBroadCast broadCast) {
        return new GameSessionTimerAdapter(gamePlayerRepository, broadCast);
    }

    @Bean
    public GameSessionStartUseCase gameSessionStartUseCase(GameSessionStartUseCase.DataOutput dataOutput, GameSessionTimer gameSessionTimer) {
        return new GameSessionStartUseCase(dataOutput, gameSessionTimer);
    }

}
