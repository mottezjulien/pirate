package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.map.domain.MapConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LO_MAP_CONFIG")
public class MapConfigEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "config")
    private final Set<MapItemEntity> items = new HashSet<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<MapItemEntity> getItems() {
        return items;
    }

    public MapConfig toModel() {
        return new MapConfig(new MapConfig.Id(id),
                items.stream().map(MapItemEntity::toModel).toList());
    }

}
