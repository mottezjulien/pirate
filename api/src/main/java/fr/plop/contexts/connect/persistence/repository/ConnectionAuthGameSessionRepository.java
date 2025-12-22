package fr.plop.contexts.connect.persistence.repository;

import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthGameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionAuthGameSessionRepository extends JpaRepository<ConnectionAuthGameSessionEntity, String> {

    String FROM = "FROM ConnectionAuthGameSessionEntity auth_game_session";

    String FETCHS = " LEFT JOIN FETCH auth_game_session.originAuthUser auth_user" +
            " LEFT JOIN FETCH auth_game_session.gameSession game_session" +
            " LEFT JOIN FETCH auth_game_session.gamePlayer game_player";

    @Query(FROM + FETCHS +
            " WHERE auth_game_session.originAuthUser.connection.user.id = :userId" +
            " AND auth_game_session.type IN (:types)")
    List<ConnectionAuthGameSessionEntity> fullByUserIdAndTypes(@Param("userId") String userId,
                                                               @Param("types") List<ConnectAuthGameSession.Type> types);

    @Query(FROM + FETCHS +
            " WHERE auth_game_session.token = :token")
    Optional<ConnectionAuthGameSessionEntity> fullByToken(@Param("token") String token);
}
