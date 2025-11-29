package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.condition.persistence.ConditionRepository;
import org.springframework.stereotype.Component;

@Component
public class TemplateInitDataConditionAdapter {

    private final ConditionRepository conditionRepository;

    public TemplateInitDataConditionAdapter(ConditionRepository conditionRepository) {
        this.conditionRepository = conditionRepository;
    }

    public void deleteAll() {
        conditionRepository.deleteAll();
    }

    public ConditionEntity create(Condition model) {
        return createEntityRecursively(ConditionEntity.fromModel(model));
    }

    private ConditionEntity createEntityRecursively(ConditionEntity entity) {
        entity.getSubs().forEach(this::createEntityRecursively);
        return conditionRepository.save(entity);
    }

}
