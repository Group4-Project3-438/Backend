package com.example.project3api.service;

import com.example.project3api.dto.CreateFavoriteRequest;
import com.example.project3api.model.FavoriteCard;
import com.example.project3api.repository.FavoriteCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteCardRepository favoriteCardRepository;

    public FavoriteService(FavoriteCardRepository favoriteCardRepository) {
        this.favoriteCardRepository = favoriteCardRepository;
    }

    public List<FavoriteCard> getByUserId(String userId) {
        requireUserId(userId);
        return favoriteCardRepository.findByUserIdOrderByCreatedAtDesc(userId.trim());
    }

    public FavoriteCard addFavorite(CreateFavoriteRequest request) {
        String userId = requireUserId(request.getUserId());
        String cardId = requireCardId(request.getCardId());

        return favoriteCardRepository
            .findByUserIdAndCardId(userId, cardId)
            .orElseGet(() -> {
                FavoriteCard favoriteCard = new FavoriteCard();
                favoriteCard.setUserId(userId);
                favoriteCard.setCardId(cardId);
                return favoriteCardRepository.save(favoriteCard);
            });
    }

    private String requireUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }

    private String requireCardId(String cardId) {
        if (!StringUtils.hasText(cardId)) {
            throw new IllegalArgumentException("cardId is required");
        }
        return cardId.trim();
    }
}
