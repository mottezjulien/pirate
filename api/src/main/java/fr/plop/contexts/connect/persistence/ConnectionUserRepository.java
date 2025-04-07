package fr.plop.contexts.connect.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionUserRepository extends JpaRepository<ConnectionUserEntity, String> {

}
