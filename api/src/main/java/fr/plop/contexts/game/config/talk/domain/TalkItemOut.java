package fr.plop.contexts.game.config.talk.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.subs.i18n.domain.I18n;

import java.util.Comparator;
import java.util.List;

/**
 * Represents the text output of a TalkItem.
 * Can be either fixed (always the same) or conditional (depends on game situation).
 */
public sealed interface TalkItemOut permits TalkItemOut.Conditional, TalkItemOut.Fixed {

    /**
     * Resolves the value based on the current game situation.
     *
     * @param situation the current game session situation
     * @return the resolved I18n text
     */
    I18n resolve(GameSessionSituation situation);

    /**
     * Fixed value - always returns the same text.
     */
    record Fixed(I18n text) implements TalkItemOut {
        @Override
        public I18n resolve(GameSessionSituation situation) {
            return text;
        }
    }

    /**
     * Conditional value - evaluates branches in order and returns the first matching one.
     * Falls back to defaultText if no branch matches.
     */
    record Conditional(I18n defaultText, List<Branch> branches) implements TalkItemOut {

        /**
         * A branch with a condition and associated text.
         * Branches are evaluated in order (by their order field).
         */
        public record Branch(int order, Condition condition, I18n text) {}

        @Override
        public I18n resolve(GameSessionSituation situation) {
            return branches.stream()
                    .sorted(Comparator.comparingInt(Branch::order))
                    .filter(branch -> branch.condition().accept(situation).toBoolean())
                    .map(Branch::text)
                    .findFirst()
                    .orElse(defaultText);
        }
    }

    /**
     * Factory method to create a Fixed TalkOut from an I18n.
     */
    static TalkItemOut fixed(I18n text) {
        return new Fixed(text);
    }

    /**
     * Factory method to create a Conditional TalkOut.
     */
    static TalkItemOut conditional(I18n defaultText, List<Conditional.Branch> branches) {
        return new Conditional(defaultText, branches);
    }
}