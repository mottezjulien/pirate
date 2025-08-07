package fr.plop;

import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionPostUseCase;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public GameSessionPostUseCase gameCreateUseCase(GameSessionPostUseCase.DataOutput port,
                                                    GameSessionTimer timer) {
        return new GameSessionPostUseCase(port, timer);
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

}
