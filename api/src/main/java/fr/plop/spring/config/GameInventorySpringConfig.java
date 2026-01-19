package fr.plop.spring.config;


import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.consequence.ConsequenceUseCase;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import fr.plop.contexts.game.session.push.PushPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class GameInventorySpringConfig {

    @Bean
    public GameSessionInventoryUseCase gameSessionInventoryUseCase(
            GameSessionInventoryUseCase.Port port,
            GameConfigCache cache,
            @Lazy ConsequenceUseCase consequenceUseCase,
            GameEventOrchestrator eventOrchestrator,
            PushPort pushPort) {
        return new GameSessionInventoryUseCase(port, cache, consequenceUseCase, eventOrchestrator, pushPort);
    }

}
