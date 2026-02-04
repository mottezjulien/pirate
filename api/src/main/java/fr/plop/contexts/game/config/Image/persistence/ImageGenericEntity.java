package fr.plop.contexts.game.config.Image.persistence;


import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "LO_IMAGE_ITEM_GENERIC")
public class ImageGenericEntity {

    @Id
    private String id;

    private String label;

    @Enumerated(EnumType.STRING)
    private Image.Type type;

    @Column(name = "_value")
    private String value;

    @OneToMany(mappedBy = "image")
    private final Set<ImageObjectEntity> objects = new HashSet<>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(Image.Type type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ImageGeneric toModel() {
        List<ImageObject> objectModels = objects.stream().map(ImageObjectEntity::toModel).toList();
        return new ImageGeneric(new ImageGeneric.Id(id), label, new Image(type, value), objectModels);
    }

    public static ImageGenericEntity fromModelId(ImageGeneric.Id modelId) {
        ImageGenericEntity entity = new ImageGenericEntity();
        entity.id = modelId.value();
        return entity;
    }

}
