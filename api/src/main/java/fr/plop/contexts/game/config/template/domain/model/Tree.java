package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.generic.tools.StringTools;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

public record Tree(String originalHeader, String _header, String reference, List<String> params, List<Tree> children) {

    public Tree(String header, List<String> params, List<Tree> children) {
        this(header, clearHeader(header), findReference(header), params, children);
        if(StringTools.isEmpty(header)) {
            throw new IllegalArgumentException("Header cannot be empty");
        }
    }

    private static String clearHeader(String rawHeader) {
        // Chercher le pattern "(ref XXXX)" ou "(REF XXXX)"
        int refStart = rawHeader.indexOf("(");
        if (refStart == -1) {
            return rawHeader.trim();
        }
        return rawHeader.substring(0, refStart).trim();
    }

    /**
     * Extrait une référence du header au format "(ref REFERENCE_NAME)" ou "(REF REFERENCE_NAME)".
     * Exemple: "Step(ref REF_STEP_A)" -> "REF_STEP_A"
     * Exemple: "Option (REF CHOIX_A)" -> "CHOIX_A"
     */
    private static String findReference(String rawHeader) {
        // Chercher le pattern "(ref XXXX)" ou "(REF XXXX)"
        int refStart = rawHeader.toUpperCase().indexOf("(REF ");
        if (refStart == -1) {
            return null;
        }
        int refNameStart = refStart + 5; // longueur de "(ref " ou "(REF "
        int refEnd = rawHeader.indexOf(")", refNameStart);
        if (refEnd == -1) {
            return null;
        }
        return rawHeader.substring(refNameStart, refEnd).trim();
    }

    public Tree sub() {
        if(!params.isEmpty()) {
            return new Tree(params.getFirst(), params.subList(1, params.size()), children());
        }
        return this;
    }

    public String header() {
        return _header.toUpperCase();
    }

    public String headerKeepCase() {
        return _header;
    }

    public boolean hasUniqueParam() {
        return params.size() == 1;
    }

    public String uniqueParam() {
        return params.getFirst();
    }

    public boolean hasParamKey(String paramKey) {
        return paramsKeys().contains(paramKey);
    }

    public String paramValue(String paramKey) {
        return paramValues().get(paramsKeys().indexOf(paramKey));
    }

    private List<String> paramsKeys() {
        if(params.size() % 2 == 0 && !params.isEmpty()) {
            List<String> result = IntStream
                    .range(0, params.size())
                    .filter(i -> i % 2 == 0)
                    .mapToObj(params::get).toList();
            if(result.size() == new HashSet<>(result).size()) {
                return result;
            }
        }
        return List.of();
    }

    private List<String> paramValues() {
        if(params.size() % 2 == 0 && !params.isEmpty()) {
            return IntStream
                    .range(0, params.size())
                    .filter(i -> i % 2 == 1)
                    .mapToObj(params::get).toList();
        }
        return List.of();
    }


    public int paramSize() {
        return params.size();
    }

    public String param(int index) {
        return params.get(index);
    }

    public boolean hasParams() {
        return !params.isEmpty();
    }
}
