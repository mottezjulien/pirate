package fr.plop.contexts.game.instance.core.adapter;

import fr.plop.contexts.connect.persistence.repository.ConnectionAuthGameInstanceRepository;
import fr.plop.contexts.game.instance.board.persistence.BoardPositionRepository;
import fr.plop.contexts.game.instance.core.domain.port.GameInstanceClearPort;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceRepository;
import fr.plop.contexts.game.instance.inventory.persistence.GameInstanceInventoryItemRepository;
import fr.plop.contexts.game.instance.inventory.persistence.GameInstanceInventoryRepository;
import fr.plop.contexts.game.instance.scenario.persistence.ScenarioGoalStepRepository;
import fr.plop.contexts.game.instance.scenario.persistence.ScenarioGoalTargetRepository;
import fr.plop.contexts.game.instance.talk.persistence.GameInstanceTalkItemRepository;
import fr.plop.contexts.game.instance.talk.persistence.GameInstanceTalkRepository;
import org.springframework.stereotype.Component;

@Component
public class GameInstanceClearAdapter implements GameInstanceClearPort {

    private final ConnectionAuthGameInstanceRepository authGameInstanceRepository;
    private final GameInstanceRepository gameInstanceRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final BoardPositionRepository boardPositionRepository;
    private final ScenarioGoalStepRepository scenarioGoalRepository;
    private final ScenarioGoalTargetRepository scenarioGoalTargetRepository;
    private final GamePlayerActionRepository gamePlayerActionRepository;
    private final GameInstanceInventoryItemRepository inventoryItemRepository;
    private final GameInstanceInventoryRepository inventoryRepository;
    private final GameInstanceTalkRepository talkRepository;
    private final GameInstanceTalkItemRepository talkItemRepository;

    public GameInstanceClearAdapter(ConnectionAuthGameInstanceRepository authGameInstanceRepository, GameInstanceRepository gameInstanceRepository, GamePlayerRepository gamePlayerRepository, BoardPositionRepository boardPositionRepository, ScenarioGoalStepRepository scenarioGoalRepository, ScenarioGoalTargetRepository scenarioGoalTargetRepository, GamePlayerActionRepository gamePlayerActionRepository, GameInstanceInventoryItemRepository inventoryItemRepository, GameInstanceInventoryRepository inventoryRepository, GameInstanceTalkRepository talkRepository, GameInstanceTalkItemRepository talkItemRepository) {
        this.authGameInstanceRepository = authGameInstanceRepository;
        this.gameInstanceRepository = gameInstanceRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.boardPositionRepository = boardPositionRepository;
        this.scenarioGoalRepository = scenarioGoalRepository;
        this.scenarioGoalTargetRepository = scenarioGoalTargetRepository;
        this.gamePlayerActionRepository = gamePlayerActionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.talkRepository = talkRepository;
        this.talkItemRepository = talkItemRepository;
    }

    @Override
    public void clearAll() {

        authGameInstanceRepository.deleteAllInBatch();

        scenarioGoalTargetRepository.deleteAllInBatch();
        scenarioGoalRepository.deleteAllInBatch();

        gamePlayerRepository.findAll().forEach(entity -> {
            entity.setLastPosition(null);
            gamePlayerRepository.save(entity);
        });
        boardPositionRepository.deleteAllInBatch();

        // Supprimer les inventaires avant les players (contrainte FK)
        inventoryItemRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();

        talkItemRepository.deleteAllInBatch();
        talkRepository.deleteAllInBatch();

        gamePlayerActionRepository.deleteAllInBatch();
        gamePlayerRepository.deleteAllInBatch();

        gameInstanceRepository.deleteAllInBatch();
    }
}