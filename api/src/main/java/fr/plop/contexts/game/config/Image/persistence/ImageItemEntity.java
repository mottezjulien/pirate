package fr.plop.contexts.game.config.Image.persistence;


import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_IMAGE_ITEM")
public class ImageItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private ImageConfigEntity config;

    @ManyToOne
    @JoinColumn(name = "image_generic_id")
    private ImageGenericEntity imageGeneric;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setConfig(ImageConfigEntity config) {
        this.config = config;
    }


    public ImageItem toModel() {
        return new ImageItem(new ImageItem.Id(id), imageGeneric.toModel());
    }

    public static ImageItemEntity fromModel(ImageConfig.Id configId, ImageItem model) {
        ImageItemEntity entity = new ImageItemEntity();
        entity.setId(model.id().value());
        entity.setConfig(ImageConfigEntity.fromModelId(configId));
        entity.imageGeneric = ImageGenericEntity.fromModelId(model.generic().id());
        return entity;
    }


}
