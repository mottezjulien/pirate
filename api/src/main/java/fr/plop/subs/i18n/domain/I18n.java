package fr.plop.subs.i18n.domain;

import fr.plop.generic.tools.StringTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public record I18n(I18n.Id id, String description, Map<Language, String> values) {
    private static final Logger log = LoggerFactory.getLogger(I18n.class);

    public I18n() {
        this(Map.of());
    }

    public I18n(Map<Language, String> values) {
        this( "", values);
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
        log.debug("I18n.value language: {} (hash: {})", language, System.identityHashCode(language));
        log.debug("I18n.value available languages: {}", values.keySet());
        for (Language l : values.keySet()) {
            log.debug("key: {} (hash: {})", l, System.identityHashCode(l));
        }
        return values.get(language);
    }
}
