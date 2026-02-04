package fr.plop.contexts.game.config.talk.domain;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents what happens after a TalkItem is displayed.
 * Can be Empty (end), Continue (auto-next), Options (user choice), or InputText (user input).
 */
public sealed interface TalkItemNext permits TalkItemNext.Empty, TalkItemNext.Continue, TalkItemNext.Options, TalkItemNext.InputText {

    /**
     * Empty - the conversation ends here (no next item).
     */
    record Empty() implements TalkItemNext {}

    /**
     * Continue - automatically leads to the next item.
     */
    record Continue(TalkItem.Id nextId) implements TalkItemNext {}

    /**
     * Options - user must select from available options.
     * Options can have conditions that determine their visibility.
     */
    record Options(List<Option> _options) implements TalkItemNext {

        public Optional<Option> option(Option.Id optionId) {
            return options().filter(option -> option.is(optionId)).findFirst();
        }

        public Stream<Option> options() {
            return _options.stream().sorted(Comparator.comparing(Option::order));
        }

        public boolean contains(Option.Id optId) {
            return _options.stream().anyMatch(_opt -> _opt.is(optId));
        }

        /**
         * Option with optional condition for visibility.
         */
        public record Option(Option.Id id, Integer order, I18n value,
                             Optional<TalkItem.Id> optNextId, Optional<Condition> optCondition) {
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
            public boolean accept(GameInstanceSituation situation) {
                return optCondition.map(condition -> condition.accept(situation).toBoolean())
                        .orElse(true);
            }
            public record Id(String value) {
                public Id() {
                    this(StringTools.generate());
                }
            }
        }
    }

    record InputText(Type type, Optional<Integer> optSize) implements TalkItemNext {
        public enum Type{
            ALPHANUMERIC, NUMERIC
        }

    }
}