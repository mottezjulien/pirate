package fr.plop.contexts.connect.persistence;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionAuthRepository extends JpaRepository<ConnectionAuthEntity, String> {

    String FROM_FETCHS = "FROM ConnectionAuthEntity auth" +
            " LEFT JOIN FETCH auth.connection connect" +
            " LEFT JOIN FETCH connect.user user";

    @Query(FROM_FETCHS + " WHERE auth.token = :token")
    Optional<ConnectionAuthEntity> findByTokenFetchs(@Param("token") String token);

    @Query(FROM_FETCHS + " WHERE connect.id = :connectId ORDER BY auth.createdAt DESC")
    List<ConnectionAuthEntity> findByConnectIdFetchsOrderByCreatedAtDesc(@Param("connectId") String connectId, Limit limit);
}
