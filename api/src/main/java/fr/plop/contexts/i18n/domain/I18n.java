package fr.plop.contexts.i18n.domain;

import fr.plop.generic.tools.StringTools;

import java.util.Map;

public record I18n(I18n.Id id, String description, Map<Language, String> values) {

    public I18n(Map<Language, String> values) {
        this(new Id(), "", values);
    }

    public I18n(String description, Map<Language, String> values) {
        this(new Id(), description, values);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public String jsonValues() {
        return StringTools.toJson(values);
    }

    public String value(Language language) {
        return values.get(language);
    }
}
