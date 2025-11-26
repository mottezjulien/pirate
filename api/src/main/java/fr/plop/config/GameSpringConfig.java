package fr.plop.config;

import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.port.GameSessionGetPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionPushAdapter;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionScenarioAdapter;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.game.session.scenario.domain.usecase.ScenarioSessionPlayerGetUseCase;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import fr.plop.contexts.game.session.time.GameSessionTimerRemove;
import fr.plop.contexts.game.session.time.adapter.GameSessionTimerAdapter;
import fr.plop.contexts.game.session.time.persistence.GameSessionTimerMemoryRepository;
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
    public GameSessionCreateUseCase gameCreateUseCase(GameSessionCreateUseCase.Port port, GameConfigCache cache) {
        return new GameSessionCreateUseCase(port, cache);
    }

    @Bean
    public GameEventBroadCast gameEventBroadCast(GameEventBroadCastIntern.Port port, GameSessionSituationGetPort situationGetPort, GameEventActionPushAdapter pushAdapter,
                                                 GameEventActionScenarioAdapter scenarioAdapter) {
        return new GameEventBroadCastIntern(port, situationGetPort, pushAdapter, scenarioAdapter);
    }

    @Bean
    public GameMoveUseCase gameMoveUseCase(GameMoveUseCase.OutPort outPort, GameEventBroadCast broadCast, PushPort pushPort) {
        return new GameMoveUseCase(outPort, broadCast, pushPort);
    }

    @Bean
    public GameOverUseCase gameOverUseCase(GameOverUseCase.OutputPort outputPort, PushPort pushPort, GameSessionTimerRemove timerRemove, GameConfigCache cache) {
        return new GameOverUseCase(outputPort, pushPort, timerRemove, cache);
    }

    @Bean
    public GameSessionTimer gameSessionTimer(GameSessionTimerMemoryRepository repository, GamePlayerRepository gamePlayerRepository, GameEventBroadCast broadCast) {
        return new GameSessionTimerAdapter(repository, gamePlayerRepository, broadCast);
    }

    @Bean
    public GameSessionStartUseCase gameSessionStartUseCase(GameSessionStartUseCase.Port port, GameSessionGetPort get, GameSessionTimer timer) {
        return new GameSessionStartUseCase(port, get, timer);
    }

    @Bean
    public ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase(ScenarioSessionPlayerGetUseCase.Port port) {
        return new ScenarioSessionPlayerGetUseCase(port);
    }

}
