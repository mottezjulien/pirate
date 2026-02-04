package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.Image.persistence.ImageItemEntity;
import fr.plop.contexts.game.config.consequence.Consequence;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("IMAGE")
public final class ConsequenceImageEntity
        extends ConsequenceAbstractEntity {

    @ManyToOne
    @JoinColumn(name = "image_id")
    private ImageItemEntity image;

    public void setImage(ImageItemEntity image) {
        this.image = image;
    }

    public Consequence toModel() {
        ImageItem.Id talkId = new ImageItem.Id(image.getId());
        return new Consequence.DisplayImage(new Consequence.Id(id), talkId);
    }

}
