package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
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

    public Stream<MapItem> byStepIds(List<ScenarioConfig.Step.Id> ids) {
        return items.stream().filter(item -> item.isSteps(ids));
    }

}
