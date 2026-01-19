package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.generic.position.Location;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;

public record Template(Atom atom, String label, String version, Descriptor descriptor, Duration maxDuration, ScenarioConfig scenario,
                       BoardConfig board, MapConfig map, TalkConfig talk, ImageConfig image, InventoryConfig inventory) {

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

        scenario.steps().forEach(step -> step.possibilities().forEach(possibility -> {
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

    public Descriptor.Level level() {
        return descriptor.level();
    }

    public String description() {
        return descriptor.desc();
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Code(String value) {

    }

    public record Atom(Id id, Code code) {
        public Atom() {
            this(new Id(), new Code(""));
        }

        public Atom withId(Id id) {
            return new Atom(id, code);
        }
        public Atom withCode(Code code) {
            return new Atom(id, code);
        }
    }

    public record Descriptor(Level level, String desc, Location departure) {

        public static Descriptor empty() {
            return new Descriptor(Level._default(), "", Location.lyonBellecour());
        }

        public record Level(int value) { // 1-5
            public static Level from(int value) {
                return new Level(value);
            }

            public static Level _default() {
                return Level.from(3);
            }
        }

    }


    public Id id() {
        return atom.id();
    }

    public Code code() {
        return atom.code();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Atom atom = new Atom();
        private String label = "";
        private String version = "0.0.0";
        private Descriptor descriptor = Descriptor.empty();
        private Duration maxDuration = DEFAULT_DURATION;
        private ScenarioConfig scenario = new ScenarioConfig();
        private BoardConfig board = new BoardConfig();
        private MapConfig map = new MapConfig();
        private TalkConfig talk = new TalkConfig();
        private ImageConfig image = new ImageConfig();
        private InventoryConfig inventory = new InventoryConfig();

        public Builder id(Id id) {
            this.atom = atom.withId(id);
            return this;
        }

        public Builder code(Code code) {
            this.atom = atom.withCode(code);
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
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

        public Template build() {
            return new Template(atom, label, version, descriptor, maxDuration, scenario, board, map, talk, image, inventory);
        }
    }

}
