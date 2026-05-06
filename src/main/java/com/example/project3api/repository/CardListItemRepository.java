package com.example.project3api.repository;

import com.example.project3api.model.CardListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardListItemRepository extends JpaRepository<CardListItem, Long> {
    List<CardListItem> findByListIdOrderByCreatedAtDesc(Long listId);
    Optional<CardListItem> findByListIdAndCardId(Long listId, String cardId);
}
