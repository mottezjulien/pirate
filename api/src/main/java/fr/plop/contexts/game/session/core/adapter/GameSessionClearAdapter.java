package fr.plop.contexts.game.session.core.adapter;

import fr.plop.contexts.connect.persistence.repository.ConnectionAuthGameSessionRepository;
import fr.plop.contexts.game.session.board.persistence.BoardPositionRepository;
import fr.plop.contexts.game.session.core.domain.port.GameSessionClearPort;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetRepository;
import org.springframework.stereotype.Component;

@Component
public class GameSessionClearAdapter implements GameSessionClearPort {

    private final ConnectionAuthGameSessionRepository authGameSessionRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final BoardPositionRepository boardPositionRepository;
    private final ScenarioGoalStepRepository scenarioGoalRepository;
    private final ScenarioGoalTargetRepository scenarioGoalTargetRepository;
    private final GamePlayerActionRepository gamePlayerActionRepository;

    public GameSessionClearAdapter(ConnectionAuthGameSessionRepository authGameSessionRepository, GameSessionRepository gameSessionRepository, GamePlayerRepository gamePlayerRepository, BoardPositionRepository boardPositionRepository, ScenarioGoalStepRepository scenarioGoalRepository, ScenarioGoalTargetRepository scenarioGoalTargetRepository, GamePlayerActionRepository gamePlayerActionRepository) {
        this.authGameSessionRepository = authGameSessionRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.boardPositionRepository = boardPositionRepository;
        this.scenarioGoalRepository = scenarioGoalRepository;
        this.scenarioGoalTargetRepository = scenarioGoalTargetRepository;
        this.gamePlayerActionRepository = gamePlayerActionRepository;
    }

    @Override
    public void clearAll() {

        authGameSessionRepository.deleteAll();

        scenarioGoalTargetRepository.deleteAll();
        scenarioGoalRepository.deleteAll();

        gamePlayerRepository.findAll().forEach(entity -> {
            entity.setLastPosition(null);
            gamePlayerRepository.save(entity);
        });
        boardPositionRepository.deleteAll();

        gamePlayerActionRepository.deleteAll();
        gamePlayerRepository.deleteAll();

        gameSessionRepository.deleteAll();
    }
}