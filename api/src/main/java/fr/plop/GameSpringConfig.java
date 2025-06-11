package fr.plop;

import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameCreateSessionUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.push.PushPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameSpringConfig {


    @Bean
    public ConnectUseCase connectUseCase(ConnectUseCase.OutPort port) {
        return new ConnectUseCase(port);
    }

    @Bean
    public GameCreateSessionUseCase gameCreateUseCase(GameCreateSessionUseCase.DataOutput port) {
        return new GameCreateSessionUseCase(port);
    }

    @Bean
    public GameEventBroadCast gameEventBroadCast(GameEventBroadCastIntern.OutPort outPort) {
        return new GameEventBroadCastIntern(outPort);
    }

    @Bean
    public GameMoveUseCase gameMoveUseCase(GameMoveUseCase.OutPort outPort, GameEventBroadCast broadCast, PushPort pushPort) {
        return new GameMoveUseCase(outPort, broadCast, pushPort);
    }

    @Bean
    public GameOverUseCase gameOverUseCase(GameOverUseCase.OutputPort outputPort, PushPort pushPort) {
        return new GameOverUseCase(outputPort, pushPort);
    }

}
