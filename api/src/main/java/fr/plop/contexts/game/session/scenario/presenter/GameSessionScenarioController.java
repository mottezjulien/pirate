package fr.plop.contexts.game.session.scenario.presenter;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.scenario.domain.usecase.ScenarioSessionPlayerGetUseCase;
import fr.plop.subs.i18n.domain.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions/{sessionId}")
public class GameSessionScenarioController {

    private final ConnectUseCase connectUseCase;

    private final ScenarioSessionPlayerGetUseCase getUseCase;

    private final GameConfigCache cache;

    public GameSessionScenarioController(ConnectUseCase connectUseCase, ScenarioSessionPlayerGetUseCase getUseCase, GameConfigCache cache) {
        this.connectUseCase = connectUseCase;
        this.getUseCase = getUseCase;
        this.cache = cache;
    }


    @GetMapping({"/goals", "/goals/"})
    public List<GameGoalResponseDTO> goals(@RequestHeader("Authorization") String rawToken,
                                           @RequestHeader("Language") String languageStr,
                                           @PathVariable("sessionId") String sessionIdStr) {

        Language language = Language.valueOf(languageStr.toUpperCase());
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));
            ScenarioSessionPlayer scenarioSessionPlayer = getUseCase.findByPlayerId(new GameContext(sessionId, player.id()));
            ScenarioConfig scenario = cache.scenario(sessionId);
            return scenarioSessionPlayer.bySteps()
                    .entrySet().stream()
                    .flatMap(entry -> scenario.steps().stream()
                            .filter(step -> step.id().equals(entry.getKey()))
                            .findFirst()
                            .map(step -> GameGoalResponseDTO.fromModel(step, entry.getValue(),
                                    scenarioSessionPlayer.byTargets(), language))
                            .stream()).toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record GameGoalResponseDTO(String id, String label, String state, List<GameTargetSimpleResponseDTO> targets) {
        public static GameGoalResponseDTO fromModel(ScenarioConfig.Step step,
                                                    ScenarioSessionState state,
                                                    Map<ScenarioConfig.Target.Id, ScenarioSessionState> byTargets,
                                                    Language language) {
            List<GameTargetSimpleResponseDTO> targetDTOs = step.targets().stream()
                    .map(target -> GameTargetSimpleResponseDTO.fromModel(target, byTargets, language)).toList();
            return new GameGoalResponseDTO(step.id().value(), step.label().value(language), state.name(), targetDTOs);
        }
    }

    public record GameTargetSimpleResponseDTO(String id, String label, boolean done, boolean optional) {
        public static GameTargetSimpleResponseDTO fromModel(ScenarioConfig.Target target, Map<ScenarioConfig.Target.Id,
                                                                    ScenarioSessionState> byTargets,
                                                            Language language) {
            ScenarioSessionState nullableState = byTargets.get(target.id());
            return new GameTargetSimpleResponseDTO(target.id().value(),
                    target.label().value(language),nullableState == ScenarioSessionState.SUCCESS,
                    target.optional());
        }
    }

}
