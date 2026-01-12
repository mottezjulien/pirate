package fr.plop.contexts.game.config.talk.domain;

import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public sealed interface TalkItemResolved permits
        TalkItemResolved.Simple,
        TalkItemResolved.Continue,
        TalkItemResolved.Options {

    TalkItem.Id id();

    I18n value();

    TalkCharacter.Reference characterReference();

    default TalkCharacter character() {
        return characterReference().character();
    }

    default String value(Language language) {
        return value().value(language);
    }

    /**
     * Simple resolved item - the conversation ends here.
     */
    record Simple(
            TalkItem.Id id,
            I18n value,
            TalkCharacter.Reference characterReference
    ) implements TalkItemResolved {}

    /**
     * Continue resolved item - automatically leads to the next item.
     */
    record Continue(
            TalkItem.Id id,
            I18n value,
            TalkCharacter.Reference characterReference,
            TalkItem.Id nextId
    ) implements TalkItemResolved {}

    /**
     * Options resolved item - user must select from available options.
     * Options have already been filtered based on conditions.
     */
    record Options(
            TalkItem.Id id,
            I18n value,
            TalkCharacter.Reference characterReference,
            List<Option> _options
    ) implements TalkItemResolved {

        public Optional<Option> option(Option.Id optionId) {
            return options().filter(option -> option.is(optionId)).findFirst();
        }

        public Stream<Option> options() {
            return _options.stream().sorted(Comparator.comparing(Option::order));
        }

        public boolean contains(Option.Id optId) {
            return _options.stream().anyMatch(opt -> opt.is(optId));
        }

        /**
         * Resolved option - ready for display.
         */
        public record Option(
                Id id,
                int order,
                I18n value,
                Optional<TalkItem.Id> optNextId
        ) {
            public boolean is(Id otherId) {
                return id.equals(otherId);
            }

            public boolean hasNext() {
                return optNextId.isPresent();
            }

            public TalkItem.Id nextId() {
                return optNextId.orElseThrow();
            }

            public record Id(String value) {}
        }
    }
}