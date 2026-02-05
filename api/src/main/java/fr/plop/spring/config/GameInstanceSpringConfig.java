package fr.plop.spring.config;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceMoveUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceStartUseCase;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.scenario.domain.GameInstanceScenarioGoalPort;
import fr.plop.contexts.game.instance.talk.GameInstanceTalkUseCase;
import fr.plop.contexts.game.instance.time.GameInstanceTimer;
import fr.plop.contexts.game.instance.time.GameInstanceTimerRemove;
import fr.plop.contexts.game.instance.time.adapter.GameInstanceTimerAdapter;
import fr.plop.contexts.game.instance.time.persistence.GameInstanceTimerMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameInstanceSpringConfig {

    @Bean
    public GameInstanceUseCase useCase(GameInstanceUseCase.Port port, GameConfigCache cache) {
        return new GameInstanceUseCase(port, cache);
    }

    @Bean
    public GameInstanceStartUseCase startUseCase(GameInstanceStartUseCase.Port port, GameConfigCache cache, GameInstanceTimer timer, GameInstanceScenarioGoalPort scenarioGoalPort) {
        return new GameInstanceStartUseCase(port, cache, timer, scenarioGoalPort);
    }

    @Bean
    public GameInstanceMoveUseCase moveUseCase(GameInstanceMoveUseCase.OutPort outPort, GamePlayerGetPort gamePlayerGetPort, GameEventOrchestrator eventOrchestrator, PushPort pushPort) {
        return new GameInstanceMoveUseCase(outPort, gamePlayerGetPort, eventOrchestrator, pushPort);
    }

    @Bean
    public GameOverUseCase gameOverUseCase(GameOverUseCase.OutputPort outputPort, PushPort pushPort, GameInstanceTimerRemove timerRemove, GameConfigCache cache) {
        return new GameOverUseCase(outputPort, pushPort, timerRemove, cache);
    }

    @Bean
    public GameInstanceTimer timer(GameInstanceTimerMemoryRepository repository, GamePlayerRepository gamePlayerRepository, GameEventOrchestrator eventOrchestrator) {
        return new GameInstanceTimerAdapter(repository, gamePlayerRepository, eventOrchestrator);
    }

    @Bean
    public GameInstanceTalkUseCase talkUseCase(GameInstanceTalkUseCase.Port port) {
        return new GameInstanceTalkUseCase(port);
    }

}
