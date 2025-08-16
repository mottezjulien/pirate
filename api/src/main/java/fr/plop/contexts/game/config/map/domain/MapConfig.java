package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.stream.Stream;

public record MapConfig(Id id, List<Item> items) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Item(Map map) {

    }

    public MapConfig() {
        this(List.of());
    }

    public MapConfig(List<Item> items) {
        this(new Id(), items);
    }

    public Stream<Map> byStepIds(List<ScenarioConfig.Step.Id> ids) {
        //TODO: ??? Filter ??
        return items.stream().map(item -> item.map);
    }

}
