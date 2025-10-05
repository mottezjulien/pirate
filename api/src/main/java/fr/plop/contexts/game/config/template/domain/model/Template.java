package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record Template(Atom atom, String label, String version, Duration maxDuration, ScenarioConfig scenario,
                       BoardConfig board, MapConfig map, TalkConfig talk) {

    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(30);

    public Template(Code code) {
        this(code, new ScenarioConfig());
    }

    public Template(Code code, ScenarioConfig scenario) {
        this(code,  "", scenario);
    }

    public Template(Code code, String label, ScenarioConfig scenario) {
        this(code,  label, scenario, new BoardConfig(), new MapConfig());
    }

    public Template(Code code, ScenarioConfig scenario, BoardConfig board, MapConfig map) {
        this(code, "", scenario, board, map, new TalkConfig());
    }

    public Template(Code code, String label, ScenarioConfig scenario, BoardConfig board, MapConfig map) {
        this(code, label, scenario, board, map, new TalkConfig());
    }

    public Template(Code code, String label, ScenarioConfig scenario, BoardConfig board, MapConfig map, TalkConfig talk) {
        this(new Atom(new Id(), code), label, "", DEFAULT_DURATION, scenario, board, map, talk);
    }



    /**
     * Valide que tous les TalkItem référencés par les possibilités (conséquences DisplayTalk et triggers TalkNext)
     * existent bien dans le TalkConfig du template.
     */
    public boolean isValid() {
        Set<String> availableTalkIds = talk.items().stream()
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
            // Triggers TalkNext
            PossibilityTrigger trigger = possibility.trigger();
            if (trigger instanceof PossibilityTrigger.TalkNext talkNext) {
                requiredTalkIds.add(talkNext.talkItemId().value());
            }
        }));

        return availableTalkIds.containsAll(requiredTalkIds);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Code(String value) {

    }

    public record Atom(Id id, Code code) {

    }

    public Id id() {
        return atom.id();
    }

    public Code code() {
        return atom.code();
    }

}
