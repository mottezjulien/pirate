package fr.plop.contexts.game.config.talk.domain;

import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.List;

/**
 * Represents a single item in a dialog/conversation.
 * Contains the text to display, the character speaking, and what happens next.
 */
public record TalkItem(Id id, TalkItemOut out, TalkCharacter.Reference characterReference, TalkItemNext next) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public boolean is(Id otherId) {
        return id().equals(otherId);
    }

    public TalkCharacter character() {
        return characterReference().character();
    }

    /**
     * Resolves this TalkItem into a TalkItemResolved based on the current game situation.
     * Evaluates all conditions and returns a simplified object ready for display.
     *
     * @param situation the current game session situation
     * @return a resolved TalkItem ready for display
     */
    public I18n resolve(GameSessionSituation situation) {
        return out.resolve(situation);
    }

    // ========== Convenience methods for working with TalkItemNext types ==========

    /**
     * Returns true if this item ends the conversation (Empty next).
     */
    public boolean isSimple() {
        return next instanceof TalkItemNext.Empty;
    }

    /**
     * Returns true if this item automatically continues to another item.
     */
    public boolean isContinue() {
        return next instanceof TalkItemNext.Continue;
    }

    /**
     * Returns true if this item presents options to the user.
     */
    public boolean isOptions() {
        return next instanceof TalkItemNext.Options;
    }

    /**
     * Gets the next item ID if this is a Continue type.
     * @throws IllegalStateException if not a Continue type
     */
    public Id nextId() {
        if (next instanceof TalkItemNext.Continue(Id nextId)) {
            return nextId;
        }
        throw new IllegalStateException("TalkItem is not a Continue type");
    }

    /**
     * Gets the options if this is an Options type.
     * @throws IllegalStateException if not an Options type
     */
    public TalkItemNext.Options options() {
        if (next instanceof TalkItemNext.Options opts) {
            return opts;
        }
        throw new IllegalStateException("TalkItem is not an Options type");
    }

    /**
     * Creates a new TalkItem with an updated nextId (for Continue type).
     */
    public TalkItem withNextId(Id newNextId) {
        return new TalkItem(id, out, characterReference, new TalkItemNext.Continue(newNextId));
    }

    /**
     * Creates a new TalkItem with updated options (for Options type).
     */
    public TalkItem withOptions(List<TalkItemNext.Options.Option> newOptions) {
        return new TalkItem(id, out, characterReference, new TalkItemNext.Options(newOptions));
    }

    // ========== Static factory methods for easier construction ==========

    /**
     * Creates a Simple TalkItem (conversation ends here).
     */
    public static TalkItem simple(Id id, TalkItemOut out, TalkCharacter.Reference characterReference) {
        return new TalkItem(id, out, characterReference, new TalkItemNext.Empty());
    }

    /**
     * Creates a Continue TalkItem (automatically leads to next item).
     */
    public static TalkItem continueItem(Id id, TalkItemOut out, TalkCharacter.Reference characterReference, Id nextId) {
        return new TalkItem(id, out, characterReference, new TalkItemNext.Continue(nextId));
    }

    /**
     * Creates an Options TalkItem (user must select from options).
     */
    public static TalkItem options(Id id, TalkItemOut out, TalkCharacter.Reference characterReference, List<TalkItemNext.Options.Option> options) {
        return new TalkItem(id, out, characterReference, new TalkItemNext.Options(options));
    }

}
