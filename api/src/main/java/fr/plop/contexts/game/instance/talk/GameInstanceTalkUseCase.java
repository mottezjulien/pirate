package fr.plop.contexts.game.instance.talk;

import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameInstanceTalkUseCase {

    public interface Port {
        Optional<GameInstanceTalk> findByPlayerId(GamePlayer.Id playerId);

        GameInstanceTalk createTalk(GamePlayer.Id playerId);

        void insert(GameInstanceTalk.Id instanceTalkId, TalkCharacter.Id characterId, TalkItem.Id talkId);
    }

    private final Port port;

    public GameInstanceTalkUseCase(Port port) {
        this.port = port;
    }


    public void read(GameInstanceContext context, TalkCharacter.Id characterId, TalkItem.Id talkId) {
        GameInstanceTalk talk = port.findByPlayerId(context.playerId())
                .orElseGet(() -> port.createTalk(context.playerId()));
        if (!talk.has(talkId)) {
            port.insert(talk.id(), characterId, talkId);
        }
    }

    public Map<TalkCharacter.Id, List<TalkItem.Id>> read(GamePlayer.Id playerId) {
        return port.findByPlayerId(playerId)
                .map(GameInstanceTalk::read)
                .orElse(Map.of());
    }

}