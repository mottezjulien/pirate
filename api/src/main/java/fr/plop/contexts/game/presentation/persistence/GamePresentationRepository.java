package fr.plop.contexts.game.presentation.persistence;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface GamePresentationRepository extends JpaRepository<GamePresentationEntity, String> {

    String FROM = "FROM GamePresentationEntity presentation";
    String FETCH_GAME = " LEFT JOIN FETCH presentation.game game";
    String FETCH_LABELS = " LEFT JOIN FETCH presentation.label label" +
            " LEFT JOIN FETCH presentation.description desc";
    String FETCH_AUTHOR = " LEFT JOIN FETCH presentation.author author";
    String FETCH_ACHIEVEMENT = " LEFT JOIN FETCH presentation.achievements achievement" +
            " LEFT JOIN FETCH achievement.image achievement_image" +
            " LEFT JOIN FETCH achievement.users achievement_user";
    String FETCH_REVIEW = " LEFT JOIN FETCH presentation.reviews review" +
            " LEFT JOIN FETCH review.user review_user";
    String FETCH_ALL = FETCH_GAME + FETCH_LABELS + FETCH_AUTHOR + FETCH_ACHIEVEMENT + FETCH_REVIEW;

    @Query(FROM + FETCH_ALL + " WHERE presentation.id = :id")
    Optional<GamePresentationEntity> fullById(@Param("id") String id);
    @Query(FROM + FETCH_GAME + FETCH_LABELS
            + " WHERE LOWER(REPLACE(presentation.game.project.code, ' ', '')) LIKE LOWER(CONCAT(:code, '%'))")
    List<GamePresentationEntity> fetchLabelsTemplateLikeLowerCode(@Param("code") String code);

    @Query(FROM + FETCH_GAME + FETCH_LABELS
            + " WHERE presentation.departureBottomLeftLng <= :topRightLng"
            + " AND presentation.departureTopRightLng >= :bottomLeftLng"
            + " AND presentation.departureBottomLeftLat <= :topRightLat"
            + " AND presentation.departureTopRightLat >= :bottomLeftLat")
    List<GamePresentationEntity> searchByLocation(@Param("bottomLeftLat") BigDecimal bottomLeftLat,
                                                 @Param("bottomLeftLng") BigDecimal bottomLeftLng,
                                                 @Param("topRightLat") BigDecimal topRightLat,
                                                 @Param("topRightLng") BigDecimal topRightLng);

    List<GamePresentationEntity> findByGameId(String gameId);

}
