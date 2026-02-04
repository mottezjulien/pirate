package fr.plop.contexts.game.presentation.domain;

import fr.plop.generic.position.Location;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.List;

public record Presentation(Id id, I18n label, I18n description,
                           Level level, Visibility visibility, Location departure,
                           List<GameType> gameTypes, Integer participantMin, Integer participantMax,
                           List<ParticipantType> participantTypes, List<Achievement> achievements, List<PresentationReview> reviews) {



    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum Visibility {
        SHOWN, HIDDEN
    }

    public record Level(int value) { // 1-5
        public static Level from(int value) {
            return new Level(value);
        }

        public static Level _default() {
            return Level.from(3);
        }
    }

    public enum GameType {
        TREASURE_HUNT, INVESTIGATION, MAN_HUNT, INFILTRATION
    }

    public enum ParticipantType {
        SOLO, FAMILY, FRIENDS, TEAM_BUILDING
    }

    public record Achievement(Id id) {
        public record Id(String value) {

        }
    }

    public Float rating() {
        if(reviews.isEmpty()) {
            return null;
        }
        Integer reduce = reviews.stream().map(PresentationReview::valueInt).reduce(0, Integer::sum);
        return (float) (reduce / reviews.size());
    }


}
