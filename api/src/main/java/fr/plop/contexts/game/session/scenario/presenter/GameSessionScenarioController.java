package fr.plop.contexts.game.session.scenario.presenter;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetEntity;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.domain.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sessions")
public class GameSessionScenarioController {

    private final ConnectUseCase connectUseCase;

    private final ScenarioGoalRepository goalRepository;

    public GameSessionScenarioController(ConnectUseCase connectUseCase, ScenarioGoalRepository goalRepository) {
        this.connectUseCase = connectUseCase;
        this.goalRepository = goalRepository;
    }

    @GetMapping({"/{sessionId}/goals", "/{sessionId}/goals/"})
    public List<GameGoalResponseDTO> goals(@RequestHeader("Authorization") String rawToken,
                                           @RequestHeader("Language") String languageStr,
                                           @PathVariable("sessionId") String sessionIdStr) {
        try {
            GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));

            Language language = Language.valueOf(languageStr.toUpperCase());
            List<ScenarioGoalEntity> goalEntities = goalRepository.fullByPlayerId(player.id().value());
            return goalEntities.stream()
                    .map(goalEntity -> GameGoalResponseDTO.fromEntity(goalEntity, language))
                    .toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }


    public record GameGoalResponseDTO(String id, String label, String state,
                                      List<GameTargetSimpleResponseDTO> targets) {
        public static GameGoalResponseDTO fromEntity(ScenarioGoalEntity goalEntity, Language language) {
            ScenarioStepEntity stepEntity = goalEntity.getStep();
            I18n stepLabel = stepEntity.getLabel().toModel();
            List<GameTargetSimpleResponseDTO> targets = stepEntity.getTargets().stream()
                    .map(targetEntity -> {
                        Optional<ScenarioGoalTargetEntity> optGoalTarget = goalEntity.getTargets().stream()
                                .filter(goalTarget -> goalTarget.getTarget().getId().equals(targetEntity.getId()))
                                .findFirst();
                        return GameTargetSimpleResponseDTO.fromEntity(targetEntity, optGoalTarget, language);
                    })
                    .toList();
            return new GameGoalResponseDTO(goalEntity.getId(), stepLabel.value(language), goalEntity.getState().name(), targets);
        }
    }


    public record GameTargetSimpleResponseDTO(String id, String label, boolean done, boolean optional) {
        public static GameTargetSimpleResponseDTO fromEntity(ScenarioTargetEntity targetEntity, Optional<ScenarioGoalTargetEntity> optGoalTarget, Language language) {
            I18n targetLabel = targetEntity.getLabel().toModel();
            boolean done = optGoalTarget.map(goalTarget -> goalTarget.getState() == ScenarioGoal.State.SUCCESS).orElse(false);
            return new GameTargetSimpleResponseDTO(targetEntity.getId(), targetLabel.value(language), done, targetEntity.isOptional());
        }
    }

}
