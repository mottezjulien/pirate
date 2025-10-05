package fr.plop.contexts.game.config.talk.domain;

import fr.plop.subs.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.Language;

import java.util.List;
import java.util.Optional;


public sealed interface TalkItem permits TalkItem.Simple, TalkItem.MultipleOptions {



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

    record MultipleOptions(Id id, I18n value, TalkCharacter character, List<Option> options) implements TalkItem  {

        public MultipleOptions(I18n value, List<Option> options) {
            this(new Id(), value, TalkCharacter.nobody(), options);
        }

        public Optional<Option> option(Option.Id optionId) {
            return options.stream().filter(option -> option.is(optionId)).findFirst();
        }

        public record Option(Option.Id id, I18n value) {
            public Option(I18n i18n) {
                this(new Option.Id(), i18n);
            }

            public boolean is(Id otherId) {
                return id.equals(otherId);
            }

            public record Id(String value) {
                public Id() {
                    this(StringTools.generate());
                }
            }
        }

    }

}

