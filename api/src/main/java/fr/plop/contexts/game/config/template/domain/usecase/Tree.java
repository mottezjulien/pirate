package fr.plop.contexts.game.config.template.domain.usecase;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Tree(String header, List<String> params, List<Tree> children) {

    public Tree sub() {
        if(!params.isEmpty()) {
            return new Tree(params.getFirst(), params.subList(1, params.size()), children());
        }
        return this;
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


}
