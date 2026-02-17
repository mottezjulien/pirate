package fr.plop.contexts.game.instance.time.persistence;

import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.contexts.game.instance.time.GameInstanceTimerGet;
import fr.plop.contexts.game.instance.time.GameInstanceTimerRemove;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


@Repository
public class GameInstanceTimerMemoryRepository implements GameInstanceTimerGet, GameInstanceTimerRemove {

    private final Map<GameInstance.Id, GameInstanceTimeUnit> byInstance = new HashMap<>();

    @Override
    public GameInstanceTimeUnit current(GameInstance.Id instanceId) {
        return byInstance.get(instanceId);
    }

    public void insert(GameInstance.Id instanceId) {
        byInstance.put(instanceId, new GameInstanceTimeUnit());
    }

    public void remove(GameInstance.Id instanceId) {
        byInstance.remove(instanceId);
    }

    public void incAll() {
        byInstance.replaceAll((key, timeUnit) -> timeUnit.inc());
    }

    public void forEach(BiConsumer<GameInstance.Id, GameInstanceTimeUnit> consumer) {
        byInstance.forEach(consumer);
    }

}
