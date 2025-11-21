package fr.plop.contexts.game.config.Image.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageItemRepository extends JpaRepository<ImageItemEntity, String> {

}