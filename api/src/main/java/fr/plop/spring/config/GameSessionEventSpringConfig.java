package fr.plop.spring.config;

import fr.plop.contexts.game.config.consequence.ConsequenceUseCase;
import fr.plop.contexts.game.config.scenario.domain.usecase.PossibilityGetUseCase;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.session.event.domain.*;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class GameSessionEventSpringConfig {

    @Bean
    public GameEventQueue eventQueue() {
        return new GameEventQueue();
    }

    @Bean
    public GameEventListenerPossibility listenerPossibility(GameSessionTimerGet timer, PossibilityGetUseCase possibilityGetUseCase, ConsequenceUseCase consequenceUseCase, GamePlayerActionPort action) {
        return new GameEventListenerPossibility(timer, possibilityGetUseCase, consequenceUseCase, action);
    }

    @Bean
    public GameEventOrchestrator eventOrchestrator(GameEventQueue eventQueue,
                                                   GameEventOrchestratorLazy orchestratorLazy,
                                                   GameEventListenerPossibility listenerPossibility) {
        ExecutorService eventExecutorService = Executors.newFixedThreadPool(5);
        GameEventOrchestratorInternal orchestrator = new GameEventOrchestratorInternal(eventExecutorService, eventQueue);

        orchestrator.registerListener(listenerPossibility);

        orchestratorLazy.set(orchestrator);

        return orchestrator;
    }

}
