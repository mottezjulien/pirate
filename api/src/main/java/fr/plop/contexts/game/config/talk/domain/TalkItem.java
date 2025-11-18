package fr.plop.contexts.game.config.talk.domain;

import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;

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

    I18n value();

    TalkCharacter.Reference characterReference();

    default TalkCharacter character() {
        return characterReference().character();
    }

    default String value(Language language) {
        return value().value(language);
    }

    record Simple(Id id, I18n value, TalkCharacter.Reference characterReference) implements TalkItem {

    }

    record Continue(Id id, I18n value, TalkCharacter.Reference characterReference, Id nextId) implements TalkItem  {
        public TalkItem withNextId(Id newNextId) {
            return new Continue(id, value, characterReference, newNextId);
        }
    }

    record Options(Id id, I18n value, TalkCharacter.Reference characterReference, List<Option> _options) implements TalkItem  {

        public Optional<Option> option(Option.Id optionId) {
            return options().filter(option -> option.is(optionId)).findFirst();
        }

        public Stream<Option> options() {
            return _options.stream().sorted(Comparator.comparing(Option::order));
        }

        public TalkItem withOptions(List<Option> newOptions) {
            return new Options(id, value, characterReference, newOptions);
        }

        public record Option(Option.Id id, Integer order, I18n value, Optional<TalkItem.Id> optNextId) {

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
                return new Option(id, order, value, Optional.of(nextId));
            }

            public record Id(String value) {
                public Id() {
                    this(StringTools.generate());
                }
            }
        }
    }

}

