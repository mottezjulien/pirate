package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TemplateGeneratorRootParser {
    private TemplateGeneratorRootParser() {
    }

    public static TemplateGeneratorRoot apply(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, TemplateGeneratorRoot.class);
    }
}