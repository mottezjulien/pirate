package fr.plop.contexts.game.config.map.persistence;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_MAP_ITEM_POSITION")
public class MapItemPositionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private MapItemEntity item;

    @Column(name = "space_id")
    private String spaceId;
    private double top;
    @Column(name = "_left")
    private double left;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    public void setId(String id) {
        this.id = id;
    }

    public void setItem(MapItemEntity item) {
        this.item = item;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public MapItem.Position toModel() {
        return new MapItem.Position(new MapItem.Position.Id(id), new BoardSpace.Id(spaceId),
                new ImagePoint(top, left), priority);
    }

}
