package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_MAP")
public class MapEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @Column(name = "definition_type")
    @Enumerated(EnumType.STRING)
    private Map.Definition.Type definitionType;

    @Column(name = "definition_value")
    private String definitionValue;

    @Enumerated(EnumType.STRING)
    private Map.Priority priority;


    @OneToMany(mappedBy = "map")
    private Set<MapPositionEntity> positions = new HashSet<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public Map.Definition.Type getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(Map.Definition.Type definitionType) {
        this.definitionType = definitionType;
    }

    public String getDefinitionValue() {
        return definitionValue;
    }

    public void setDefinitionValue(String definitionValue) {
        this.definitionValue = definitionValue;
    }

    public Map.Priority getPriority() {
        return priority;
    }

    public void setPriority(Map.Priority priority) {
        this.priority = priority;
    }

    public Set<MapPositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(Set<MapPositionEntity> points) {
        this.positions = points;
    }

    public Map toModel() {
        Map.Definition definition = new Map.Definition(definitionType, definitionValue);
        return new Map(new Map.Id(id), label.toModel(), definition, priority, positions.stream()
                .map(entity -> entity.toModel()).toList());
    }

}
