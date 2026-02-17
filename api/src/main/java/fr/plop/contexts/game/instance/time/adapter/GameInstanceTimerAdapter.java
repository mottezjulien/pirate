package fr.plop.contexts.game.instance.time.adapter;

import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.contexts.game.instance.time.GameInstanceTimer;
import fr.plop.contexts.game.instance.time.persistence.GameInstanceTimerMemoryRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class GameInstanceTimerAdapter implements GameInstanceTimer {

    private static final String ONE_MINUTE_IN_MILLISECONDS = "60000";
    private final GameInstanceTimerMemoryRepository repository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameEventOrchestrator eventOrchestrator;

    public GameInstanceTimerAdapter(GameInstanceTimerMemoryRepository repository, GamePlayerRepository gamePlayerRepository, GameEventOrchestrator eventOrchestrator) {
        this.repository = repository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.eventOrchestrator = eventOrchestrator;
    }


    @Scheduled(fixedDelayString = "${game.session.timer.duration:" + ONE_MINUTE_IN_MILLISECONDS + "}")
    public void run() {
        execute();
        repository.incAll();
    }

    private void execute() {
        repository.forEach(this::tick);
    }

    private void tick(GameInstance.Id instanceId, GameInstanceTimeUnit timeUnit) {
        final GameEvent.TimeClick event = new GameEvent.TimeClick(timeUnit);
        for (GamePlayer.Id playerId : findActivePlayerIdsByInstanceId(instanceId)) {
            eventOrchestrator.fire( new GameInstanceContext(instanceId, playerId), event);
        }
    }

    private List<GamePlayer.Id> findActivePlayerIdsByInstanceId(GameInstance.Id instanceId) {
        return gamePlayerRepository.activeIdsByInstanceId(instanceId.value())
                .stream().map(GamePlayer.Id::new).toList();
    }

    @Override
    public void start(GameInstance.Id instanceId) {
        repository.insert(instanceId);
    }

}
