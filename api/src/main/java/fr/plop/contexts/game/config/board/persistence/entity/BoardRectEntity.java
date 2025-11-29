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
    private double topRightLatitude;

    @Column(name = "top_right_longitude")
    private double topRightLongitude;

    @Column(name = "bottom_left_latitude")
    private double bottomLeftLatitude;

    @Column(name = "bottom_left_longitude")
    private double bottomLeftLongitude;

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

    public void setTopRightLatitude(double topRightLatitude) {
        this.topRightLatitude = topRightLatitude;
    }

    public void setTopRightLongitude(double topRightLongitude) {
        this.topRightLongitude = topRightLongitude;
    }

    public void setBottomLeftLatitude(double bottomLeftLatitude) {
        this.bottomLeftLatitude = bottomLeftLatitude;
    }

    public void setBottomLeftLongitude(double bottomLeftLongitude) {
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
