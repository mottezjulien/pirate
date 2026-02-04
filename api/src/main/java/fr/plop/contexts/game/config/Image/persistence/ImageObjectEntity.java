package fr.plop.contexts.game.config.Image.persistence;


import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.generic.ImagePoint;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

import java.util.Optional;

@Entity
@Table(name = "LO_IMAGE_OBJECT")
public class ImageObjectEntity {

    public enum Type {
        POINT, IMAGE
    }

    @Id
    private String id;

    private String label;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private ImageGenericEntity image;

    @Enumerated(EnumType.STRING)
    private Type type;

    private double top;

    @Column(name = "_left")
    private double left;

    @Column(name = "point_color")
    private String pointColor;

    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;


    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setImage(ImageGenericEntity image) {
        this.image = image;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setPointColor(String pointColor) {
        this.pointColor = pointColor;
    }

    public void setImageType(Image.Type imageType) {
        this.imageType = imageType;
    }

    public void setImageValue(String imageValue) {
        this.imageValue = imageValue;
    }

    public void setNullableCondition(ConditionEntity nullableCondition) {
        this.nullableCondition = nullableCondition;
    }

    public ImageObject toModel() {
        ImagePoint center = new ImagePoint(top, left);
        Optional<Condition> optCondition = Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel);
        ImageObject.Atom atom = new ImageObject.Atom(new ImageObject.Id(id), label, center, optCondition);
        return switch (type) {
            case POINT -> new ImageObject.Point(atom, pointColor);
            case IMAGE -> new ImageObject._Image(atom, new Image(imageType, imageValue));
        };
    }

}
