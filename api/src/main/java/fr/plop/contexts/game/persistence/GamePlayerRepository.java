package fr.plop.contexts.game.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, String> {

}
