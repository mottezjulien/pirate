package fr.plop.contexts.game.config.board.persistence.entity;

import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_BOARD_RECT")
public class BoardRectEntity {

    @Id
    //@UuidGenerator
    private String id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private BoardSpaceEntity space;

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

    public BoardSpaceEntity getSpace() {
        return space;
    }

    public void setSpace(BoardSpaceEntity space) {
        this.space = space;
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

    public Rect toModel() {
        return new Rect(new Point(bottomLeftLatitude, bottomLeftLongitude), new Point(topRightLatitude, topRightLongitude));
    }
}
