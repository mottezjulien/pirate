package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.stream.Stream;

public record MapConfig(Id id, List<MapItem> items) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public MapConfig() {
        this(List.of());
    }

    public MapConfig(List<MapItem> items) {
        this(new Id(), items);
    }

    public Stream<MapItem> select(GameInstanceSituation situation) {
        return items.stream()
                .filter(item -> item.optCondition().isEmpty() || item.optCondition().get().accept(situation).toBoolean())
                .map(item -> item.select(situation));
    }

}
