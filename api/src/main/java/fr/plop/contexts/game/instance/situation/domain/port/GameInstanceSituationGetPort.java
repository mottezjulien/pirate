package fr.plop.contexts.game.instance.situation.domain.port;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;

public interface GameInstanceSituationGetPort {

    GameInstanceSituation get(GameInstanceContext context);

}
