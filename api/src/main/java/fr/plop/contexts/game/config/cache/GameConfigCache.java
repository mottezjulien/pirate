package fr.plop.contexts.game.config.cache;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;

public interface GameConfigCache {
    void insert(GameInstance instance);
    void remove(GameInstance.Id instanceId);
    ScenarioConfig scenario(GameInstance.Id instanceId);
    BoardConfig board(GameInstance.Id instanceId);
    MapConfig map(GameInstance.Id instanceId);
    TalkConfig talk(GameInstance.Id instanceId);
    ImageConfig image(GameInstance.Id instanceId);
    InventoryConfig inventory(GameInstance.Id id);
}
