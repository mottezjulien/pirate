package fr.plop.contexts.game.config.board.persistence.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rectangle;
import fr.plop.generic.tools.StringTools;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "LO_BOARD_RECTANGLE")
public class BoardRectEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private BoardSpaceEntity space;

    @Column(name = "top_right_latitude", precision = 18, scale = 12)
    private BigDecimal topRightLatitude;

    @Column(name = "top_right_longitude", precision = 18, scale = 12)
    private BigDecimal topRightLongitude;

    @Column(name = "bottom_left_latitude", precision = 18, scale = 12)
    private BigDecimal bottomLeftLatitude;

    @Column(name = "bottom_left_longitude", precision = 18, scale = 12)
    private BigDecimal bottomLeftLongitude;

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

    public void setTopRightLatitude(BigDecimal topRightLatitude) {
        this.topRightLatitude = topRightLatitude;
    }

    public void setTopRightLongitude(BigDecimal topRightLongitude) {
        this.topRightLongitude = topRightLongitude;
    }

    public void setBottomLeftLatitude(BigDecimal bottomLeftLatitude) {
        this.bottomLeftLatitude = bottomLeftLatitude;
    }

    public void setBottomLeftLongitude(BigDecimal bottomLeftLongitude) {
        this.bottomLeftLongitude = bottomLeftLongitude;
    }

    public Rectangle toModel() {
        return new Rectangle(new Point(bottomLeftLatitude, bottomLeftLongitude), new Point(topRightLatitude, topRightLongitude));
    }

    public static BoardRectEntity fromModel(BoardSpace.Id spaceId, Rectangle model) {
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
