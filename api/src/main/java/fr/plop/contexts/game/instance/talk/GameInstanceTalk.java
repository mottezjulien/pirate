package fr.plop.contexts.game.instance.talk;

import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;

import java.util.List;
import java.util.Map;

public record GameInstanceTalk(Id id, Map<TalkCharacter.Id, List<TalkItem.Id>> read) {

    public Object map() {
        return null;
    }

    public record Id(String value) {

    }

    public boolean has(TalkItem.Id talkId) {
        return read.keySet().stream()
                .anyMatch(characterId -> read.get(characterId).contains(talkId));
    }

}
