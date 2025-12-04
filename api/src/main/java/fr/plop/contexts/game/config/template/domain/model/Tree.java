package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.generic.tools.StringTools;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Tree(String originalHeader, String _header, String reference, List<String> params, List<Tree> children) {


    public Tree(String header, List<String> params) {
        this(header, params, List.of());
    }

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

    boolean hasUniqueParam() {
        return params.size() == 1;
    }

    private String uniqueParam() {
        return params.getFirst();
    }

    public Stream<String> keys() {
        if(!params.isEmpty()) {
            return paramsUpperKeys().stream();
        }
        return children.stream()
                .filter(Tree::hasUniqueParam)
                .map(child -> child.params.getFirst());
    }

    public String findByKeyWithUnique(String key) {
        if(params.size() == 1) {
            return params.getFirst();
        }
        return findByKeyOrThrow(key);
    }

    public String findByKeyOrThrow(String key) {
        return findByKey(key)
                .orElseThrow(() -> new RuntimeException("key " + key + " not found in tree " + originalHeader));
    }
    public String findByKeyOrValue(String key, String orElse) {
        return findByKey(key).orElse(orElse);
    }

    public String findByKeyOrParamIndexOrValue(String key, int index, String orElse) {
        return findByKey(key)
                .orElseGet(() -> {
                    if(params.size()-1 >= index) {
                        return params.get(index);
                    }
                    return orElse;
        });
    }

    public String findByKeyOrParamIndexOrThrow(String key, int index) {
        Optional<String> optValue = findByKey(key);
        if(optValue.isPresent()) {
            return optValue.get();
        }
        if(params.size()-1 >= index) {
            return params.get(index);
        }
        throw new RuntimeException("key " + key + " not found in tree " + originalHeader);
    }


    public Optional<String> findByKey(String key) {
        String upperKey = key.toUpperCase();
        if(hasParamKey(upperKey)) {
            return Optional.of(paramValue(upperKey));
        }
        Optional<Tree> optChildByKey = findChildKey(upperKey);
        if(optChildByKey.isPresent() && optChildByKey.get().hasUniqueParam()){
            return Optional.of(optChildByKey.get().uniqueParam());
        }
        return Optional.empty();
    }


    boolean hasParamKey(String upperKey) {
        return paramsUpperKeys().contains(upperKey);
    }

    String paramValue(String upperKey) {
        return paramValues().get(paramsUpperKeys().indexOf(upperKey));
    }

    private List<String> paramsUpperKeys() {
        if(params.size() % 2 == 0 && !params.isEmpty()) {
            List<String> result = IntStream
                    .range(0, params.size())
                    .filter(i -> i % 2 == 0)
                    .mapToObj(index -> params.get(index).toUpperCase()).toList();
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

    public Optional<Tree> findChildKey(String key) {
        return childrenByKey(key.toUpperCase()).findFirst();
    }

    public Stream<Tree> childrenByKey(String upperKey) {
        return children.stream()
                .filter(child -> child.isHeader(upperKey));
    }

    public boolean isHeader(String key) {
        return key.toUpperCase().equals(header());
    }

}
