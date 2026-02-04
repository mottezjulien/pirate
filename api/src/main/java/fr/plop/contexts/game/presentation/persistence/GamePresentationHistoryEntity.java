package fr.plop.contexts.game.presentation.persistence;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "LO_PRESENTATION_HISTORY")
public class GamePresentationHistoryEntity {

    @Id
    private String id;

}
