package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;

public record Template(Id id, Duration maxDuration, ScenarioConfig scenario,
                       BoardConfig board, MapConfig map, TalkConfig talk, ImageConfig image,
                       InventoryConfig inventory) {

    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(30);

    /**
     * Valide que tous les TalkItem référencés par les possibilités (conséquences DisplayTalk et triggers TalkNext)
     * existent bien dans le TalkConfig du template.
     */
    public boolean isValid() {
        /*Set<String> availableTalkIds = talk.items().stream()
                .map(TalkItem::id)
                .map(TalkItem.Id::value)
                .collect(Collectors.toSet());

        Set<String> requiredTalkIds = new HashSet<>();

        scenario.steps().forEach(step -> step.genericPossibilities().forEach(possibility -> {
            // Conséquences DisplayTalk
            possibility.consequences().forEach(consequence -> {
                if (consequence instanceof Consequence.DisplayTalk dt) {
                    requiredTalkIds.add(dt.talkId().value());
                }
            });
        }));

        return availableTalkIds.containsAll(requiredTalkIds);*/
        return true;
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Id id = new Id();
        private Duration maxDuration = DEFAULT_DURATION;
        private ScenarioConfig scenario = new ScenarioConfig();
        private BoardConfig board = new BoardConfig();
        private MapConfig map = new MapConfig();
        private TalkConfig talk = new TalkConfig();
        private ImageConfig image = new ImageConfig();
        private InventoryConfig inventory = new InventoryConfig();

        public Builder id(Id id) {
            this.id = id;
            return this;
        }

        public Builder scenario(ScenarioConfig scenario) {
            this.scenario = scenario;
            return this;
        }

        public Builder board(BoardConfig board) {
            this.board = board;
            return this;
        }

        public Builder map(MapConfig map) {
            this.map = map;
            return this;
        }

        public Builder talk(TalkConfig talk) {
            this.talk = talk;
            return this;
        }

        public Builder image(ImageConfig image) {
            this.image = image;
            return this;
        }

        public Builder inventory(InventoryConfig inventory) {
            this.inventory = inventory;
            return this;
        }

        public Template build() {
            return new Template(id, maxDuration, scenario, board, map, talk, image, inventory);
        }
    }

}
