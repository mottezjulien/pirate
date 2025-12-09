package fr.plop.contexts.game.session.situation.domain.port;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;

public interface GameSessionSituationGetPort {

    GameSessionSituation get(GameSessionContext context);

}
