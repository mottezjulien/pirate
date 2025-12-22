package fr.plop.contexts.game.session.scenario.presenter;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.subs.i18n.domain.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/sessions/{sessionId}")
public class GameSessionScenarioController {

    private final ConnectAuthGameSessionUseCase authGameSessionUseCase;
    private final GameSessionScenarioGoalPort scenarioGoalPort;
    private final GameConfigCache cache;

    public GameSessionScenarioController(ConnectAuthGameSessionUseCase authGameSessionUseCase, GameSessionScenarioGoalPort scenarioGoalPort, GameConfigCache cache) {
        this.authGameSessionUseCase = authGameSessionUseCase;
        this.scenarioGoalPort = scenarioGoalPort;
        this.cache = cache;
    }


    @GetMapping({"/goals", "/goals/"})
    public Stream<GameGoalResponseDTO> goals(@RequestHeader("Authorization") String rawSessionToken,
                                             @RequestHeader("Language") String languageStr,
                                             @PathVariable("sessionId") String sessionIdStr) {

        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {

            final GameSessionContext context = authGameSessionUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));

            final ScenarioSessionPlayer scenarioSessionPlayer = scenarioGoalPort.findByPlayerId(context.playerId());
            final ScenarioConfig scenario = cache.scenario(sessionId);

            return scenario.orderedSteps()
                    .filter(step -> scenarioSessionPlayer.isStepPresent(step.id()))
                    .map(step -> GameGoalResponseDTO.fromModel(step, scenarioSessionPlayer, language));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }


    public record GameGoalResponseDTO(String id, String label, String state,
                                      List<GameTargetSimpleResponseDTO> targets) {
        public static GameGoalResponseDTO fromModel(ScenarioConfig.Step step, ScenarioSessionPlayer scenarioSessionPlayer, Language language) {
            List<GameTargetSimpleResponseDTO> targets = step.targets().stream()
                    .map(target -> GameTargetSimpleResponseDTO.fromModel(target, scenarioSessionPlayer, language)).toList();
            return new GameGoalResponseDTO(step.id().value(), step.label().value(language),
                    scenarioSessionPlayer.optStepState(step.id()).orElseThrow().name(), targets);
        }
    }

    public record GameTargetSimpleResponseDTO(String id, String label, boolean done, boolean optional) {
        public static GameTargetSimpleResponseDTO fromModel(ScenarioConfig.Target target, ScenarioSessionPlayer scenarioSessionPlayer,
                                                            Language language) {
            return new GameTargetSimpleResponseDTO(target.id().value(),
                    target.label().value(language),
                    scenarioSessionPlayer.isTargetDone(target.id()),
                    target.optional());
        }
    }

    @GetMapping({"/targets/{targetId}", "/targets/{targetId}/"})
    public GameTargetDetailsResponseDTO targetDetails(@RequestHeader("Authorization") String rawSessionToken,
                                                      @RequestHeader("Language") String languageStr,
                                                      @PathVariable("sessionId") String sessionIdStr,
                                                      @PathVariable("targetId") String targetIdStr) {

        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id(targetIdStr);
        try {
            authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));

            final ScenarioConfig scenario = cache.scenario(sessionId);
            ScenarioConfig.Target target = scenario.targetById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found", null));
            return GameTargetDetailsResponseDTO.toModel(target, language);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record GameTargetDetailsResponseDTO(String id, String label, String description, boolean done,
                                               boolean optional, List<String> hints, String answer) {

        public static GameTargetDetailsResponseDTO toModel(ScenarioConfig.Target target, Language language) {

            return new GameTargetDetailsResponseDTO(
                    target.id().value(),
                    target.label().value(language),
                    target.optDescription().map(desc -> desc.value(language)).orElse(""),
                    false,
                    target.optional(),
                    target.hints().stream().map(hint -> hint.value(language)).toList(),
                    target.optAnswer().map(ans -> ans.value(language)).orElse(""));
        }
    }


}
