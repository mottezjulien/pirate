package fr.plop.contexts.i18n.domain;
import fr.plop.generic.tools.StringTools;

import java.util.Map;

public record I18n(String description, Map<Language, String> values) {

    public String jsonValues() {
        return StringTools.toJson(values);
    }

    public String value(Language language) {
        return values.get(language);
    }
}
