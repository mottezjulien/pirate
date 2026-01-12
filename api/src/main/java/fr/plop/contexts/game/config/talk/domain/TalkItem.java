package fr.plop.contexts.game.config.talk.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public sealed interface TalkItem permits TalkItem.Simple, TalkItem.Continue, TalkItem.Options {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    Id id();

    default boolean is(Id otherId) {
        return id().equals(otherId);
    }

    TalkValue value();

    TalkCharacter.Reference characterReference();

    default TalkCharacter character() {
        return characterReference().character();
    }

    /**
     * Resolves this TalkItem into a TalkItemResolved based on the current game situation.
     * Evaluates all conditions and returns a simplified object ready for display.
     *
     * @param situation the current game session situation
     * @return a resolved TalkItem ready for display
     */
    TalkItemResolved resolve(GameSessionSituation situation);

    /**
     * Simple TalkItem - the conversation ends here.
     */
    record Simple(
            Id id,
            TalkValue value,
            TalkCharacter.Reference characterReference
    ) implements TalkItem {

        @Override
        public TalkItemResolved resolve(GameSessionSituation situation) {
            I18n resolvedValue = value.resolve(situation);
            return new TalkItemResolved.Simple(id, resolvedValue, characterReference);
        }
    }

    /**
     * Continue TalkItem - automatically leads to the next item.
     */
    record Continue(
            Id id,
            TalkValue value,
            TalkCharacter.Reference characterReference,
            Id nextId
    ) implements TalkItem {

        @Override
        public TalkItemResolved resolve(GameSessionSituation situation) {
            I18n resolvedValue = value.resolve(situation);
            return new TalkItemResolved.Continue(id, resolvedValue, characterReference, nextId);
        }

        public TalkItem withNextId(Id newNextId) {
            return new Continue(id, value, characterReference, newNextId);
        }
    }

    /**
     * Options TalkItem - user must select from available options.
     * Options can have conditions that determine their visibility.
     */
    record Options(
            Id id,
            TalkValue value,
            TalkCharacter.Reference characterReference,
            List<Option> _options
    ) implements TalkItem {

        public Optional<Option> option(Option.Id optionId) {
            return options().filter(option -> option.is(optionId)).findFirst();
        }

        public Stream<Option> options() {
            return _options.stream().sorted(Comparator.comparing(Option::order));
        }

        public TalkItem withOptions(List<Option> newOptions) {
            return new Options(id, value, characterReference, newOptions);
        }

        @Override
        public TalkItemResolved resolve(GameSessionSituation situation) {
            I18n resolvedValue = value.resolve(situation);

            // Filter options based on their conditions
            List<TalkItemResolved.Options.Option> resolvedOptions = options()
                    .filter(option -> option.accept(situation))
                    .map(Option::toResolved)
                    .toList();

            return new TalkItemResolved.Options(id, resolvedValue, characterReference, resolvedOptions);
        }

        public boolean contains(Option.Id optId) {
            return _options.stream().anyMatch(_opt -> _opt.is(optId));
        }

        /**
         * Option with optional condition for visibility.
         */
        public record Option(
                Option.Id id,
                Integer order,
                I18n value,
                Optional<TalkItem.Id> optNextId,
                Optional<Condition> optCondition
        ) {
            public Option(Id id, int order, I18n value) {
                this(id, order, value, Optional.empty(), Optional.empty());
            }

            public boolean is(Id otherId) {
                return id.equals(otherId);
            }

            public boolean hasNext() {
                return optNextId.isPresent();
            }

            public TalkItem.Id nextId() {
                return optNextId.orElseThrow();
            }

            public Option withNextId(TalkItem.Id nextId) {
                return new Option(id, order, value, Optional.of(nextId), optCondition);
            }

            /**
             * Checks if this option should be displayed based on its condition and the current situation.
             */
            public boolean accept(GameSessionSituation situation) {
                return optCondition.map(condition -> condition.accept(situation).toBoolean())
                        .orElse(true);
            }

            /**
             * Converts this Option to a resolved Option (without the condition).
             */
            public TalkItemResolved.Options.Option toResolved() {
                return new TalkItemResolved.Options.Option(
                        new TalkItemResolved.Options.Option.Id(id.value()),
                        order,
                        value,
                        optNextId
                );
            }

            public record Id(String value) {
                public Id() {
                    this(StringTools.generate());
                }
            }
        }
    }
}