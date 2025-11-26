package fr.plop.contexts.game.session.situation.domain.port;

import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;

public interface GameSessionSituationGetPort {

    GameSessionSituation get(GameContext context);
    GameSessionSituation get(GameSession.Id sessionId, GamePlayer player);

}
