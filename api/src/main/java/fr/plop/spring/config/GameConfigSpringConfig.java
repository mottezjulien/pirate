package fr.plop.spring.config;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.consequence.ConsequenceUseCase;
import fr.plop.contexts.game.config.consequence.handler.*;
import fr.plop.contexts.game.config.scenario.domain.usecase.PossibilityGetUseCase;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerActionPort;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfigSpringConfig {

    @Bean
    public PossibilityGetUseCase possibilityGetUseCase(GameSessionSituationGetPort situationGet, GamePlayerActionPort action, GameConfigCache cache, GameSessionScenarioGoalPort scenarioGoalPort) {
        return new PossibilityGetUseCase(situationGet, action, cache, scenarioGoalPort);
    }

    @Bean
    public ConsequenceUseCase ConsequenceUseCase(ConsequenceScenarioGoalHandler scenarioGoalHandler,
                                                 ConsequenceTalkHandler talkHandler,
                                                 ConsequenceOverHandler gameOverHandler,
                                                 ConsequenceObjectHandler objectHandler,
                                                 ConsequenceImageHandler imageHandler,
                                                 ConsequenceMetadataHandler metadataHandler) {
        ConsequenceUseCase consequenceUseCase = new ConsequenceUseCase();
        consequenceUseCase.registerHandler(scenarioGoalHandler);
        consequenceUseCase.registerHandler(talkHandler);
        consequenceUseCase.registerHandler(gameOverHandler);
        consequenceUseCase.registerHandler(objectHandler);
        consequenceUseCase.registerHandler(imageHandler);
        consequenceUseCase.registerHandler(metadataHandler);
        return consequenceUseCase;
    }

}
