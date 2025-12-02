package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MapItem_selectPosition_Test {

    private final BoardSpace.Id spaceId1 = new BoardSpace.Id("spaceId1");
    private final BoardSpace.Id spaceId2 = new BoardSpace.Id("spaceId2");
    private final BoardSpace.Id spaceId3 = new BoardSpace.Id("spaceId3");
    private final MapItem.Position position1 = new MapItem.Position(spaceId1, Mockito.mock(ImagePoint.class), Priority.MEDIUM);
    private final MapItem.Position position2 = new MapItem.Position(spaceId2, Mockito.mock(ImagePoint.class), Priority.HIGHEST);
    private final MapItem.Position position3 = new MapItem.Position(spaceId3, Mockito.mock(ImagePoint.class), Priority.HIGH);

    @Test
    public void emptyByEmptyPositions() {
        MapItem mapItem = new MapItem(imageGeneric(), Optional.of(pointer()), List.of());
        assertThat(mapItem.selectPosition(List.of(spaceId1))).isEmpty();
    }

    @Test
    public void uniqueSpace() {
        MapItem mapItem = new MapItem(imageGeneric(), Optional.of(pointer()), List.of(position1, position2, position3));
        assertThat(mapItem.selectPosition(List.of(spaceId1))).hasValue(position1);
    }

    @Test
    public void multipleSpace_selectByPriority() {
        MapItem mapItem = new MapItem(imageGeneric(), Optional.of(pointer()), List.of(position1, position2, position3));
        assertThat(mapItem.selectPosition(List.of(spaceId1, spaceId2, spaceId3))).hasValue(position2);
    }

    @Test
    public void emptyIfNotMatch() {
        MapItem mapItem = new MapItem(imageGeneric(), Optional.of(pointer()), List.of(position1, position2));
        assertThat(mapItem.selectPosition(List.of(spaceId3))).isEmpty();
    }


    private ImageGeneric imageGeneric() {
        return new ImageGeneric("", mock(Image.class), List.of());
    }

    private Image pointer() {
        return new Image(Image.Type.WEB, "any");
    }

}