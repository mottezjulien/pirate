package fr.plop.contexts.game.config.inventory.persistence;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryMergedRule;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "LO_CONFIG_INVENTORY")
public class GameConfigInventoryEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "config")
    private final Set<GameConfigInventoryItemEntity> items = new HashSet<>();

    @Column(name = "merge_rules_json", columnDefinition = "TEXT")
    private String mergeRulesJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<GameConfigInventoryItemEntity> getItems() {
        return items;
    }

    public void setMergeRules(List<InventoryMergedRule> mergeRules) {
        if (mergeRules == null || mergeRules.isEmpty()) {
            this.mergeRulesJson = null;
            return;
        }
        try {
            List<MergeRuleJson> jsonRules = mergeRules.stream()
                    .map(rule -> new MergeRuleJson(
                            rule.accept().stream().map(GameConfigInventoryItem.Id::value).toList(),
                            rule.convertTo().value()))
                    .toList();
            this.mergeRulesJson = new ObjectMapper().writeValueAsString(jsonRules);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize merge rules", e);
        }
    }

    public List<InventoryMergedRule> getMergeRules() {
        if (mergeRulesJson == null || mergeRulesJson.isBlank()) {
            return List.of();
        }
        try {
            List<MergeRuleJson> jsonRules = new ObjectMapper().readValue(mergeRulesJson, new TypeReference<>() {});
            return jsonRules.stream()
                    .map(json -> new InventoryMergedRule(
                            json.accept().stream().map(GameConfigInventoryItem.Id::new).toList(),
                            new GameConfigInventoryItem.Id(json.result())))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize merge rules", e);
        }
    }

    public InventoryConfig toModel() {
        return new InventoryConfig(new InventoryConfig.Id(id), items.stream()
                .map(GameConfigInventoryItemEntity::toModel)
                .toList(), getMergeRules());
    }

    private record MergeRuleJson(List<String> accept, String result) {}
}
