package fr.plop.contexts.game.config.board.persistence.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;
import fr.plop.generic.tools.StringTools;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_BOARD_RECT")
public class BoardRectEntity {

    @Id
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

    public static BoardRectEntity fromModel(BoardSpace.Id spaceId, Rect model) {
        BoardRectEntity entity = new BoardRectEntity();
        entity.setId(StringTools.generate());
        BoardSpaceEntity spaceEntity = new BoardSpaceEntity();
        spaceEntity.setId(spaceId.value());
        entity.setSpace(spaceEntity);
        entity.setTopRightLatitude(model.topRight().lat());
        entity.setTopRightLongitude(model.topRight().lng());
        entity.setBottomLeftLatitude(model.bottomLeft().lat());
        entity.setBottomLeftLongitude(model.bottomLeft().lng());
        return entity;
    }

}
