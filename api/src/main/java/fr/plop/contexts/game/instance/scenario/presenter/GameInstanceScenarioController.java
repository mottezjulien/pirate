package fr.plop.contexts.game.instance.scenario.presenter;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.scenario.domain.GameInstanceScenarioGoalPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioInstancePlayer;
import fr.plop.subs.i18n.domain.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/instances/{instanceId}")
public class GameInstanceScenarioController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameInstanceScenarioGoalPort scenarioGoalPort;
    private final GameConfigCache cache;

    public GameInstanceScenarioController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameInstanceScenarioGoalPort scenarioGoalPort, GameConfigCache cache) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.scenarioGoalPort = scenarioGoalPort;
        this.cache = cache;
    }


    @GetMapping({"/goals", "/goals/"})
    public Stream<GameGoalResponseDTO> goals(@RequestHeader("Authorization") String rawInstanceToken,
                                             @RequestHeader("Language") String languageStr,
                                             @PathVariable("instanceId") String instanceIdStr) {

        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        try {

            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(instanceId, new ConnectToken(rawInstanceToken));

            final ScenarioInstancePlayer scenarioInstancePlayer = scenarioGoalPort.findByPlayerId(context.playerId());
            final ScenarioConfig scenario = cache.scenario(instanceId);

            return scenario.orderedSteps()
                    .filter(step -> scenarioInstancePlayer.isStepPresent(step.id()))
                    .map(step -> GameGoalResponseDTO.fromModel(step, scenarioInstancePlayer, language));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }


    public record GameGoalResponseDTO(String id, String label, String state,
                                      List<GameTargetSimpleResponseDTO> targets) {
        public static GameGoalResponseDTO fromModel(ScenarioConfig.Step step, ScenarioInstancePlayer scenarioInstancePlayer, Language language) {
            List<GameTargetSimpleResponseDTO> targets = step.targets().stream()
                    .map(target -> GameTargetSimpleResponseDTO.fromModel(target, scenarioInstancePlayer, language)).toList();
            return new GameGoalResponseDTO(step.id().value(), step.label().value(language),
                    scenarioInstancePlayer.optStepState(step.id()).orElseThrow().name(), targets);
        }
    }

    public record GameTargetSimpleResponseDTO(String id, String label, boolean done, boolean optional) {
        public static GameTargetSimpleResponseDTO fromModel(ScenarioConfig.Target target, ScenarioInstancePlayer scenarioInstancePlayer,
                                                            Language language) {
            return new GameTargetSimpleResponseDTO(target.id().value(),
                    target.label().value(language),
                    scenarioInstancePlayer.isTargetDone(target.id()),
                    target.optional());
        }
    }

    @GetMapping({"/targets/{targetId}", "/targets/{targetId}/"})
    public GameTargetDetailsResponseDTO targetDetails(@RequestHeader("Authorization") String rawInstanceToken,
                                                      @RequestHeader("Language") String languageStr,
                                                      @PathVariable("instanceId") String instanceIdStr,
                                                      @PathVariable("targetId") String targetIdStr) {

        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final ScenarioConfig.Target.Id targetId = new ScenarioConfig.Target.Id(targetIdStr);
        try {
            authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));

            final ScenarioConfig scenario = cache.scenario(instanceId);
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
