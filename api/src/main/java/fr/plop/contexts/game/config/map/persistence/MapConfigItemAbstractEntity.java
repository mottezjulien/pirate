package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.map.domain.MapConfig;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_MAP_CONFIG_ITEM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type")
public abstract class MapConfigItemAbstractEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private MapConfigEntity config;

    @ManyToOne
    @JoinColumn(name = "map_id")
    private MapEntity map;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MapConfigEntity getConfig() {
        return config;
    }

    public void setConfig(MapConfigEntity config) {
        this.config = config;
    }

    public MapEntity getMap() {
        return map;
    }

    public void setMap(MapEntity map) {
        this.map = map;
    }

    public MapConfig.Item toModel() {
        return new MapConfig.Item(map.toModel());
    }
}
