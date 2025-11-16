package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/sessions/admin")
public class GameAdminSessionController {

    private final GameSessionRepository gameSessionRepository;
    public GameAdminSessionController(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    @GetMapping({"", "/"})
    public List<GameAdminSessionResponseDTO> sessions() {
        return gameSessionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GameSessionEntity::getStartAt))
                .map(GameAdminSessionResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping({"/{sessionId}", "/{sessionId}/"})
    public GameSessionDetailsResponseDTO oneSession(@PathVariable("sessionId") String sessionIdStr) {
        return gameSessionRepository.findByIdFetchPlayerAndUser(sessionIdStr)
                .map(GameSessionDetailsResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record GameAdminSessionResponseDTO(String id, String label, String state, String startAt, String overAt) {
        public static GameAdminSessionResponseDTO fromEntity(GameSessionEntity entity) {
            String startAt = null;
            if (entity.getStartAt() != null) {
                startAt = entity.getStartAt().toString();
            }
            String overAt = null;
            if (entity.getOverAt() != null) {
                overAt = entity.getOverAt().toString();
            }
            return new GameAdminSessionResponseDTO(entity.getId(),
                    entity.getLabel(),
                    entity.getState().name(), startAt, overAt);
        }
    }

    public record GameSessionDetailsResponseDTO(String id, String label, String state, String startAt, String overAt,
                                                List<Player> players) {
        public record Player(String id, String state, String userId) {
            public static Player fromEntity(GamePlayerEntity entity) {
                return new Player(entity.getId(), entity.getState().name(), entity.getUser().getId());
            }
        }

        public static GameSessionDetailsResponseDTO fromEntity(GameSessionEntity entity) {
            String startAt = entity.getStartAt().toString();
            String overAt = null;
            if (entity.getOverAt() != null) {
                overAt = entity.getOverAt().toString();
            }
            List<Player> players = entity.getPlayers().stream()
                    .map(Player::fromEntity).toList();
            return new GameSessionDetailsResponseDTO(entity.getId(),
                    entity.getLabel(),
                    entity.getState().name(), startAt, overAt, players);
        }
    }

    /*
    @PostMapping({"/{sessionId}/players/{playerId}/message"})
    public void sendMessage(@PathVariable("sessionId") String sessionIdStr,
                            @PathVariable("playerId") String playerIdStr,
                            @RequestBody SendMessageRequest request) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        GamePlayer.Id playerId = new GamePlayer.Id(playerIdStr);
        pushPort.push(new PushEvent.Message(sessionId, playerId, request.value()));
    }

    public record SendMessageRequest(String value) {

    }*/

}


