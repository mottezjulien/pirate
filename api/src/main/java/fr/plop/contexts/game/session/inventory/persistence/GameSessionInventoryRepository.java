package fr.plop.contexts.game.session.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameSessionInventoryRepository extends JpaRepository<GameSessionInventoryEntity, String> {

    Optional<GameSessionInventoryEntity> findByPlayerId(String playerId);

}
