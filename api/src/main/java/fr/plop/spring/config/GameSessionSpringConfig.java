package fr.plop.spring.config;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.core.domain.port.GameSessionGetPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import fr.plop.contexts.game.session.time.GameSessionTimerRemove;
import fr.plop.contexts.game.session.time.adapter.GameSessionTimerAdapter;
import fr.plop.contexts.game.session.time.persistence.GameSessionTimerMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameSessionSpringConfig {

    @Bean
    public GameSessionUseCase gameCreateUseCase(GameSessionUseCase.Port port, GameConfigCache cache) {
        return new GameSessionUseCase(port, cache);
    }

    @Bean
    public GameMoveUseCase gameMoveUseCase(GameMoveUseCase.OutPort outPort, GamePlayerGetPort gamePlayerGetPort, GameEventOrchestrator eventOrchestrator, PushPort pushPort, GameConfigCache cache) {
        return new GameMoveUseCase(outPort, gamePlayerGetPort, eventOrchestrator, pushPort, cache);
    }

    @Bean
    public GameOverUseCase gameOverUseCase(GameOverUseCase.OutputPort outputPort, PushPort pushPort, GameSessionTimerRemove timerRemove, GameConfigCache cache) {
        return new GameOverUseCase(outputPort, pushPort, timerRemove, cache);
    }

    @Bean
    public GameSessionTimer gameSessionTimer(GameSessionTimerMemoryRepository repository, GamePlayerRepository gamePlayerRepository, GameEventOrchestrator eventOrchestrator) {
        return new GameSessionTimerAdapter(repository, gamePlayerRepository, eventOrchestrator);
    }

    @Bean
    public GameSessionStartUseCase gameSessionStartUseCase(GameSessionStartUseCase.Port port, GameConfigCache cache, GameSessionGetPort get, GameSessionTimer timer, GameSessionScenarioGoalPort scenarioGoalPort) {
        return new GameSessionStartUseCase(port, cache, get, timer, scenarioGoalPort);
    }

}
