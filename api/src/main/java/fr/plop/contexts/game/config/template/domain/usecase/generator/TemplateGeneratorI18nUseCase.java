package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TemplateGeneratorI18nUseCase {

    Language currentLang;


    public Optional<I18n> apply(List<Tree> children) {
        final Map<Language, String> values = new HashMap<>();
        currentLang = Language.byDefault();
        for (Tree child : children) {
            if(child.params().size() == 1) {
                Language.safeValueOf(child.header())
                        .ifPresent(language -> {
                            currentLang = language;
                            insertValue(values, child.params().getFirst());
                        });
            } else {
                insertValue(values, child.headerKeepCase());
            }
        }
        if(values.isEmpty())
            return Optional.empty();
        return Optional.of(new I18n(values));
    }

    private void insertValue(Map<Language, String> values, String value) {
        values.compute(currentLang, (language, current) -> {
            if(current == null) {
                return value;
            }
            return current + "\n" + value;
        });
    }

}
