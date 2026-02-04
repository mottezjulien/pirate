package fr.plop.contexts.game.presentation.persistence;

import fr.plop.contexts.game.presentation.domain.PresentationReview;
import fr.plop.contexts.user.persistence.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_PRESENTATION_REVIEW")
public class GamePresentationReviewEntity {

    @Id
    private String id;

    @Column(name = "_value")
    private int value;

    private String details;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private PresentationReview.State state;

    @ManyToOne
    @JoinColumn(name = "presentation_id")
    private GamePresentationEntity presentation;

    public PresentationReview toModel() {
        return new PresentationReview(new PresentationReview.Id(id), user.toModelId(), new PresentationReview.Value(value), details);
    }

}
