package fr.plop.contexts.game.config.Image.persistence;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_IMAGE_CONFIG")
public class ImageConfigEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "config")
    private Set<ImageItemEntity> items = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<ImageItemEntity> getItems() {
        return items;
    }

    public void setItems(Set<ImageItemEntity> items) {
        this.items = items;
    }

    public ImageConfig toModel() {
        return new ImageConfig(new ImageConfig.Id(id), items.stream()
                .map(ImageItemEntity::toModel)
                .toList());
    }
}