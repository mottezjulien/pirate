package fr.plop.contexts.game.config.template.domain.usecase.generator;

import java.util.*;

public class GlobalCache {

    private final Map<String, Object> references = new HashMap<>();

    public <Element> Element reference(String referenceName, Class<Element> type, Element orElse) {
        if(references.containsKey(referenceName)) {
            Object ref = references.get(referenceName);
            if (type.isInstance(ref)) {
                return type.cast(ref);
            }
        }
        references.put(referenceName, orElse);
        return orElse;
    }

}