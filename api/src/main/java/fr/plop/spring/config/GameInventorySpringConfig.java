package fr.plop.spring.config;


import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameInventorySpringConfig {

    @Bean
    public GameSessionInventoryUseCase gameSessionInventoryUseCase(GameSessionInventoryUseCase.Port port, GameConfigCache cache) {
        return new GameSessionInventoryUseCase(port, cache);
    }

}
