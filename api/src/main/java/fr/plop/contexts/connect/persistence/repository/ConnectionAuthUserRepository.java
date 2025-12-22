package fr.plop.contexts.connect.persistence.repository;

import fr.plop.contexts.connect.persistence.entity.ConnectionAuthUserEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionAuthUserRepository extends JpaRepository<ConnectionAuthUserEntity, String> {


    String FROM = "FROM ConnectionAuthUserEntity auth_user";
    String FETCHS = " LEFT JOIN FETCH auth_user.connection connect" +
            " LEFT JOIN FETCH connect.user user";

    @Query(FROM + FETCHS + " WHERE auth_user.token = :token")
    Optional<ConnectionAuthUserEntity> fullByToken(@Param("token") String token);

    @Query(FROM + FETCHS + " WHERE connect.id = :connectId" +
            " ORDER BY auth_user.createdAt DESC")
    List<ConnectionAuthUserEntity> fullByConnectIdOrderByCreatedAtDesc(@Param("connectId") String connectId, Limit limit);

}
