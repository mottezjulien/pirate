package fr.plop.contexts.game.instance.time;


import fr.plop.contexts.game.instance.core.domain.model.GameInstance;

public interface GameInstanceTimerRemove {
    void remove(GameInstance.Id sessionId);

}
