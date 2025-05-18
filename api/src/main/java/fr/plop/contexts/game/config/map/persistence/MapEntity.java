package fr.plop.contexts.game.config.map.persistence;


import fr.plop.contexts.game.config.board.persistence.entity.BoardSpaceEntity;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_MAP")
public class MapEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    private String definition;

    @Column(name = "top_right_latitude")
    private float topRightLatitude;

    @Column(name = "top_right_longitude")
    private float topRightLongitude;

    @Column(name = "bottom_left_latitude")
    private float bottomLeftLatitude;

    @Column(name = "bottom_left_longitude")
    private float bottomLeftLongitude;


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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public float getTopRightLatitude() {
        return topRightLatitude;
    }

    public void setTopRightLatitude(float topRightLatitude) {
        this.topRightLatitude = topRightLatitude;
    }

    public float getTopRightLongitude() {
        return topRightLongitude;
    }

    public void setTopRightLongitude(float topRightLongitude) {
        this.topRightLongitude = topRightLongitude;
    }

    public float getBottomLeftLatitude() {
        return bottomLeftLatitude;
    }

    public void setBottomLeftLatitude(float bottomLeftLatitude) {
        this.bottomLeftLatitude = bottomLeftLatitude;
    }

    public float getBottomLeftLongitude() {
        return bottomLeftLongitude;
    }

    public void setBottomLeftLongitude(float bottomLeftLongitude) {
        this.bottomLeftLongitude = bottomLeftLongitude;
    }

    public Map toModel() {
        return new Map(new Map.Id(id), label.toModel(), definition, new Rect(new Point(bottomLeftLatitude, bottomLeftLongitude), new Point(topRightLatitude, topRightLongitude)));
    }
}
