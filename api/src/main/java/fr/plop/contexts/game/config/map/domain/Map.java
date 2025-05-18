package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.position.Rect;
import fr.plop.generic.tools.StringTools;

public record Map(Id id, I18n label, String definition, Rect rect) {
    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }
}
