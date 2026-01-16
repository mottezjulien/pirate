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
        return new ObjectMapper().readValue(json, Map.class);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean almostEquals(String str, String strOther) {
        if (str == null || strOther == null) {
            return false;
        }

        String str1 = str.toLowerCase();
        String str2 = strOther.toLowerCase();

        if(str1.equals(str2)) {
            return true;
        }

        int len1 = str1.length();
        int len2 = str2.length();

        if (len1 == len2) {
            int diffCount = 0;
            for (int i = 0; i < len1; i++) {
                if (str1.charAt(i) != str2.charAt(i)) {
                    diffCount++;
                    if (diffCount > 1) {
                        return false;
                    }
                }
            }
            return diffCount == 1;
        }

        if (Math.abs(len1 - len2) == 1) {
            String longer = len1 > len2 ? str1 : str2;
            String shorter = len1 > len2 ? str2 : str1;

            for (int i = 0; i < longer.length(); i++) {
                String temp = longer.substring(0, i) + longer.substring(i + 1);
                if (temp.equals(shorter)) {
                    return true;
                }
            }
        }

        return false;
    }
}
