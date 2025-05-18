package fr.plop.contexts.game.config.map.domain;

import fr.plop.generic.tools.StringTools;

import java.util.List;

public record MapConfig(Id id, List<Item> items) {



    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Item(Map map) {

    }

    public MapConfig(List<Item> items) {
        this(new Id(), items);
    }

}
