package fr.plop.contexts.game.config.template.adapter;

import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import fr.plop.contexts.game.config.talk.persistence.*;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import fr.plop.subs.i18n.persistence.I18nRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TemplateInitDataTalkAdapter {

    private final I18nRepository i18nRepository;

    private final TalkConfigRepository talkConfigRepository;
    private final TalkItemRepository talkItemRepository;
    private final TalkItemBranchRepository talkItemBranchRepository;

    private final TalkItemMultipleOptionsRepository talkOptionsRepository;
    private final TalkOptionRepository talkOptionItemRepository;

    private final TalkCharacterRepository talkCharacterRepository;
    private final TalkCharacterReferenceRepository talkCharacterReferenceRepository;

    private final TemplateInitDataConditionAdapter conditionAdapter;

    public TemplateInitDataTalkAdapter(I18nRepository i18nRepository, TalkConfigRepository talkConfigRepository, TalkItemRepository talkItemRepository, TalkItemBranchRepository talkItemBranchRepository, TalkItemMultipleOptionsRepository talkOptionsRepository, TalkOptionRepository talkOptionItemRepository, TalkCharacterRepository talkCharacterRepository, TalkCharacterReferenceRepository talkCharacterReferenceRepository, TemplateInitDataConditionAdapter conditionAdapter) {
        this.i18nRepository = i18nRepository;
        this.talkConfigRepository = talkConfigRepository;
        this.talkItemRepository = talkItemRepository;
        this.talkItemBranchRepository = talkItemBranchRepository;
        this.talkOptionsRepository = talkOptionsRepository;
        this.talkOptionItemRepository = talkOptionItemRepository;
        this.talkCharacterRepository = talkCharacterRepository;
        this.talkCharacterReferenceRepository = talkCharacterReferenceRepository;
        this.conditionAdapter = conditionAdapter;
    }

    public void deleteAll() {
        talkItemBranchRepository.deleteAll();
        talkOptionsRepository.deleteAll();
        talkOptionItemRepository.deleteAll();
        talkItemRepository.deleteAll();
        talkConfigRepository.deleteAll();
        talkCharacterReferenceRepository.deleteAll();
        talkCharacterRepository.deleteAll();
    }

    public TalkConfigEntity createTalk(TalkConfig talk) {
        TalkConfigEntity config = new TalkConfigEntity();
        config.setId(talk.id().value());
        talkConfigRepository.save(config);
        List<TalkCharacter.Reference> savedCharacterReferences = createTalkCharacterReferences(talk.items());
        talk.items().forEach(item -> createTalkItem(item, config, savedCharacterReferences));
        return config;
    }

    private List<TalkCharacter.Reference> createTalkCharacterReferences(List<TalkItem> items) {
        Map<TalkCharacter, List<TalkCharacter.Reference>> allSaved = new HashMap<>();
        for (TalkItem item : items) {
            TalkCharacter itemCharacter = item.character();
            TalkCharacter savedCharacter = allSaved.keySet().stream()
                    .filter(itemCharacter::hasSameName)
                    .findFirst()
                    .orElseGet(() -> {
                        TalkCharacterEntity entity = TalkCharacterEntity.fromModel(itemCharacter);
                        talkCharacterRepository.save(entity);
                        allSaved.put(itemCharacter, new ArrayList<>());
                        return itemCharacter;
                    });
            List<TalkCharacter.Reference> savedReferences = allSaved.get(savedCharacter);
            if (savedReferences.stream()
                    .noneMatch(reference -> reference.hasSameValue(item.characterReference()))) {
                TalkCharacterReferenceEntity entity = TalkCharacterReferenceEntity.fromModel(savedCharacter.id(), item.characterReference());
                talkCharacterReferenceRepository.save(entity);
                savedReferences.add(item.characterReference());
                allSaved.put(itemCharacter, savedReferences);
            }
        }
        return allSaved.values().stream().flatMap(Collection::stream).toList();
    }

    private void createTalkItem(TalkItem item, TalkConfigEntity config, List<TalkCharacter.Reference> savedCharacterReferences) {
        TalkItemEntity entity = switch (item.next()) {
            case TalkItemNext.Empty ignored -> new TalkItemEntity();
            case TalkItemNext.Continue cont -> {
                TalkItemContinueEntity continueEntity = new TalkItemContinueEntity();
                continueEntity.setNextId(cont.nextId().value());
                yield continueEntity;
            }
            case TalkItemNext.Options opts -> {
                TalkItemMultipleOptionsEntity optionsEntity = new TalkItemMultipleOptionsEntity();
                List<TalkItemNext.Options.Option> options = opts.options().toList();
                for (int i = 0; i < options.size(); i++) {
                    optionsEntity.getOptions().add(createTalkOption(options.get(i), i));
                }
                yield optionsEntity;
            }
            case TalkItemNext.InputText inputText -> {
                TalkItemInputTextEntity inputTextEntity = new TalkItemInputTextEntity();
                inputTextEntity.setInputTextType(inputText.type().name());
                inputText.optSize().ifPresent(inputTextEntity::setInputTextSize);
                yield inputTextEntity;
            }
        };
        entity.setId(item.id().value());
        entity.setConfig(config);
        entity.setCharacterReference(findTalkCharacterReference(item.characterReference(), savedCharacterReferences));

        switch (item.out()) {
            case TalkItemOut.Fixed fixed -> {
                entity.setOutputType(TalkItemEntity.OutputType.FIXED);
                entity.setValue(createI18n(fixed.text()));
            }
            case TalkItemOut.Conditional conditional -> {
                entity.setOutputType(TalkItemEntity.OutputType.CONDITIONAL);
                entity.setValue(createI18n(conditional.defaultText()));
                for (TalkItemOut.Conditional.Branch branch : conditional.branches()) {
                    TalkItemBranchEntity branchEntity = new TalkItemBranchEntity();
                    branchEntity.setId(UUID.randomUUID().toString());
                    branchEntity.setTalkItem(entity);
                    branchEntity.setOrder(branch.order());
                    branchEntity.setValue(createI18n(branch.text()));
                    branchEntity.setCondition(conditionAdapter.create(branch.condition()));
                    entity.getBranches().add(branchEntity);
                }
            }
        }

        talkItemRepository.save(entity);
    }

    private TalkCharacterReferenceEntity findTalkCharacterReference(TalkCharacter.Reference characterReference, List<TalkCharacter.Reference> savedCharacterReferences) {
        TalkCharacter.Reference foundSaved = savedCharacterReferences.stream().filter(savedReference -> characterReference.value().equals(savedReference.value())
                && characterReference.character().name().equals(savedReference.character().name())).findFirst().orElseThrow();
        TalkCharacterReferenceEntity entity = new TalkCharacterReferenceEntity();
        entity.setId(foundSaved.id().value());
        return entity;
    }

    private TalkOptionEntity createTalkOption(TalkItemNext.Options.Option talkOption, int index) {
        TalkOptionEntity entity = new TalkOptionEntity();
        entity.setId(talkOption.id().value());
        entity.setValue(createI18n(talkOption.value()));
        entity.setOrder(index);
        talkOption.optNextId().ifPresent(nextId -> entity.setNullableNextId(nextId.value()));
        talkOption.optCondition()
                .ifPresent(condition -> entity.setNullableCondition(conditionAdapter.create(condition)));
        return talkOptionItemRepository.save(entity);
    }

    private I18nEntity createI18n(I18n model) {
        return i18nRepository.save(I18nEntity.fromModel(model));
    }

}
