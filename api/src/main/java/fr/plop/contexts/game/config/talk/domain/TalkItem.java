package fr.plop.contexts.game.config.talk.domain;

import fr.plop.subs.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.Language;

import java.util.List;
import java.util.Optional;


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

    TalkCharacter character();

    default String value(Language language) {
        return value().value(language);
    }

    record Simple(Id id, I18n value, TalkCharacter character) implements TalkItem {

    }

    record Continue(Id id, I18n value, TalkCharacter character, Id nextId) implements TalkItem  {

    }

    record Options(Id id, I18n value, TalkCharacter character, List<Option> options) implements TalkItem  {

        public Options(I18n value, List<Option> options) {
            this(new Id(), value, TalkCharacter.nobody(), options);
        }

        public Optional<Option> option(Option.Id optionId) {
            return options.stream().filter(option -> option.is(optionId)).findFirst();
        }

        public record Option(Option.Id id, I18n value, Optional<TalkItem.Id> optNextId) {
            public Option(I18n i18n) {
                this(new Option.Id(), i18n);
            }

            public Option(Id id, I18n value) {
                this(id, value, Optional.empty());
            }

            public Option(Id id, I18n value, TalkItem.Id nextId) {
                this(id, value, Optional.of(nextId));
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

            public record Id(String value) {
                public Id() {
                    this(StringTools.generate());
                }
            }
        }
    }

}

