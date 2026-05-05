package com.example.project3api.repository;

import com.example.project3api.model.FavoriteCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteCardRepository extends JpaRepository<FavoriteCard, Long> {
    List<FavoriteCard> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<FavoriteCard> findByUserIdAndCardId(String userId, String cardId);
}
