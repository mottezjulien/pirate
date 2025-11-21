package fr.plop.contexts.game.config.Image.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageConfigRepository extends JpaRepository<ImageConfigEntity, String> {
    String FETCH_ALL = " LEFT JOIN FETCH image.items image_item";

}