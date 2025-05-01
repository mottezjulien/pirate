package fr.plop;

import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.event.domain.GameEventBroadCast;
import fr.plop.contexts.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.event.domain.usecase.action.GameEventScenarioSuccessGoalAction;
import fr.plop.contexts.game.domain.usecase.GameConnectUseCase;
import fr.plop.contexts.game.domain.usecase.GameCreateUseCase;
import fr.plop.contexts.game.domain.usecase.GameMoveUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameSpringConfig {


    @Bean
    public ConnectUseCase connectUseCase(ConnectUseCase.OutPort port) {
        return new ConnectUseCase(port);
    }

    @Bean
    public GameCreateUseCase gameCreateUseCase(GameCreateUseCase.DataOutput port) {
        return new GameCreateUseCase(port);
    }

    @Bean
    public GameConnectUseCase gameConnectUseCase(GameConnectUseCase.OutPort port) {
        return new GameConnectUseCase(port);
    }

    @Bean
    public GameEventBroadCast gameEventBroadCast(GameEventBroadCastIntern.OutPort outPort,
                                                 GameEventScenarioSuccessGoalAction successGoalAction) {
        return new GameEventBroadCastIntern(outPort, successGoalAction);
    }

    @Bean
    public GameMoveUseCase gameMoveUseCase(GameMoveUseCase.OutPort outPort, GameEventBroadCast broadCast) {
        return new GameMoveUseCase(outPort, broadCast);
    }

}
