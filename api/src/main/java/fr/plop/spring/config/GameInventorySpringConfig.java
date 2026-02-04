package fr.plop.spring.config;


import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryUseCase;
import fr.plop.contexts.game.instance.push.PushPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameInventorySpringConfig {

    @Bean
    public GameInstanceInventoryUseCase instanceInventoryUseCase(
            GameInstanceInventoryUseCase.Port port,
            GameConfigCache cache,
            GameEventOrchestrator eventOrchestrator,
            PushPort pushPort) {
        return new GameInstanceInventoryUseCase(port, cache, eventOrchestrator, pushPort);
    }

}
