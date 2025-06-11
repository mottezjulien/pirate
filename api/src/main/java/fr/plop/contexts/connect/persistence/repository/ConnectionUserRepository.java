package fr.plop.contexts.connect.persistence.repository;

import fr.plop.contexts.connect.persistence.entity.ConnectionUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionUserRepository extends JpaRepository<ConnectionUserEntity, String> {

}
