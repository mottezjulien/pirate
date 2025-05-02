package fr.plop.contexts.game.config.scenario.presenter;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.usecase.GameConnectUseCase;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/scenarios")
public class ScenarioController {

    private final ConnectUseCase connectUseCase;
    private final GameConnectUseCase gameConnectUseCase;
    private final ScenarioGoalRepository goalRepository;

    public ScenarioController(ConnectUseCase connectUseCase, GameConnectUseCase gameConnectUseCase, ScenarioGoalRepository goalRepository) {
        this.connectUseCase = connectUseCase;
        this.gameConnectUseCase = gameConnectUseCase;
        this.goalRepository = goalRepository;
    }


    @GetMapping({"/", ""})
    public List<ScenarioStepResponseDTO> goals(@RequestHeader("Authorization") String rawToken, @RequestHeader("Language") String languageStr) {
        Language language = Language.valueOfSafe(languageStr);
        try {
            ConnectUser user = connectUseCase.findUserIdByRawToken(new ConnectToken(rawToken));
            GamePlayer.Atom player = gameConnectUseCase.findByUserId(user.id());
            List<ScenarioGoalEntity> goals = goalRepository.fullByPlayerId(player.id().value());
            return goals.stream()
                    .map(entity -> ScenarioStepResponseDTO.fromEntity(entity.getStep(), language))
                    .toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.type().name(), e);
        }
    }

    public record ScenarioStepResponseDTO(String id, String label, List<ScenarioTargetResponseDTO> targets) {
        public static ScenarioStepResponseDTO fromEntity(ScenarioStepEntity entity, Language language) {
            return new ScenarioStepResponseDTO(
                    entity.getId(),
                    entity.getLabel().toModel().value(language),
                    entity.getTargets().stream().map(target -> ScenarioTargetResponseDTO.fromEntity(target, language)).toList()
            );
        }
    }

    public record ScenarioTargetResponseDTO(String id, String label) {
        public static ScenarioTargetResponseDTO fromEntity(ScenarioTargetEntity entity, Language language) {
            return new ScenarioTargetResponseDTO(entity.getId(), entity.getLabel().toModel().value(language));
        }
    }

}
