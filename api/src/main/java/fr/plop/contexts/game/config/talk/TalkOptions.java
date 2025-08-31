package fr.plop.contexts.game.config.talk;

import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

import java.util.List;


public record TalkOptions(Id id, I18n label, List<Option> options) {

    public TalkOptions(I18n label, List<Option> options) {
        this(new Id(), label, options);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Option(Option.Id id, I18n value) {
        public Option(I18n i18n) {
            this(new Option.Id(), i18n);
        }

        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }
    }

}
