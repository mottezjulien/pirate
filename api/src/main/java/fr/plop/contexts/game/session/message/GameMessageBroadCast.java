package fr.plop.contexts.game.session.message;


import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.i18n.domain.I18n;

public interface GameMessageBroadCast {

    void alert(GamePlayer.Id id, I18n message);

    //void forceUpdate(GamePlayer.Id id);

}
