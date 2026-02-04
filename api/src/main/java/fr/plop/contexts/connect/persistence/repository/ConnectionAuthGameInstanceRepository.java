package fr.plop.contexts.connect.persistence.repository;

import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.connect.persistence.entity.ConnectionAuthGameInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionAuthGameInstanceRepository extends JpaRepository<ConnectionAuthGameInstanceEntity, String> {

    String FROM = "FROM ConnectionAuthGameInstanceEntity auth_instance";

    String FETCHS = " LEFT JOIN FETCH auth_instance.originAuthUser auth_user" +
            " LEFT JOIN FETCH auth_instance.instance instance" +
            " LEFT JOIN FETCH auth_instance.gamePlayer game_player";

    @Query(FROM + FETCHS +
            " WHERE auth_instance.originAuthUser.connection.user.id = :userId" +
            " AND auth_instance.status = :status")
    List<ConnectionAuthGameInstanceEntity> fullByUserIdAndTypes(@Param("userId") String userId,
                                                                @Param("status") ConnectAuthGameInstance.Status status);

    @Query(FROM + FETCHS +
            " WHERE auth_instance.token = :token")
    Optional<ConnectionAuthGameInstanceEntity> fullByToken(@Param("token") String token);
}
