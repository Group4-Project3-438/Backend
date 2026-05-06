package com.example.project3api.repository;

import com.example.project3api.model.CardList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardListRepository extends JpaRepository<CardList, Long> {
    List<CardList> findByUserIdOrderByCreatedAtDesc(String userId);
}
