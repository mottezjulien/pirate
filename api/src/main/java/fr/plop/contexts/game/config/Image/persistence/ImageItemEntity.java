package fr.plop.contexts.game.config.Image.persistence;


import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_IMAGE_ITEM")
public class ImageItemEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private ImageConfigEntity config;

    @Enumerated(EnumType.STRING)
    private Image.Type type;

    @Column(name = "_value")
    private String value;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setConfig(ImageConfigEntity config) {
        this.config = config;
    }

    public void setType(Image.Type type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ImageItem toModel() {
        return new ImageItem(new ImageItem.Id(id), new Image(type, value));
    }

    public static ImageItemEntity fromModel(ImageConfig.Id configId, ImageItem model) {
        ImageItemEntity entity = new ImageItemEntity();
        entity.setId(model.id().value());
        ImageConfigEntity configEntity = new ImageConfigEntity();
        configEntity.setId(configId.value());
        entity.setConfig(configEntity);
        entity.setType(model.value().type());
        entity.setValue(model.value().value());
        return entity;
    }


}
