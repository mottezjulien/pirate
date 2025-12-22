package fr.plop.contexts.connect.domain;

import fr.plop.generic.tools.StringTools;

public record ConnectToken(String value) {
    public ConnectToken() {
        this(StringTools.generate());
    }
}
