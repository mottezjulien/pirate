package fr.plop.contexts.game.config.talk.domain;

import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;

public record TalkConfig(Id id, List<TalkItem> items) {

    public Optional<TalkItem> byId(TalkItem.Id taklId) {
        return items.stream().filter(item -> item.is(taklId)).findFirst();
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public TalkConfig() {
        this(List.of());
    }

    public TalkConfig(List<TalkItem> items) {
        this(new Id(), items);
    }

}