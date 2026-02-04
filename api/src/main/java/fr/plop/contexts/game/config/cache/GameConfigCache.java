package fr.plop.contexts.game.config.cache;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;

public interface GameConfigCache {
    void insert(GameInstance session);
    void remove(GameInstance.Id sessionId);
    ScenarioConfig scenario(GameInstance.Id sessionId);
    BoardConfig board(GameInstance.Id sessionId);
    MapConfig map(GameInstance.Id sessionId);
    TalkConfig talk(GameInstance.Id sessionId);
    ImageConfig image(GameInstance.Id sessionId);
    InventoryConfig inventory(GameInstance.Id id);
}
