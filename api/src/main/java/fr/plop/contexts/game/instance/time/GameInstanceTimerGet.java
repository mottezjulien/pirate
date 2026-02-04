package fr.plop.contexts.game.instance.time;


import fr.plop.contexts.game.instance.core.domain.model.GameInstance;

public interface GameInstanceTimerGet {
    GameInstanceTimeUnit current(GameInstance.Id sessionId);

}
