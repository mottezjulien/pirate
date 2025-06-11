package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import org.springframework.stereotype.Component;

@Component
public class GameEventActionMessage {
    public void alert(GamePlayer.Id id, PossibilityConsequence.Alert consequence) {
        //TODO messageBroadCast.alert(id, consequence.message());
    }
}
