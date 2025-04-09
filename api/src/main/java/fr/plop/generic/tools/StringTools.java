package fr.plop.generic.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

public class StringTools {

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public static Map<String, String> fromJson(String json) throws JsonProcessingException {
        return  new ObjectMapper().readValue(json, Map.class);
    }

}
