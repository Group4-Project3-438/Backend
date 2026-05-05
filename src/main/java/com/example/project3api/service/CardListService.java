package com.example.project3api.service;

import com.example.project3api.dto.AddCardToListRequest;
import com.example.project3api.dto.CreateCardListRequest;
import com.example.project3api.model.CardList;
import com.example.project3api.model.CardListItem;
import com.example.project3api.repository.CardListItemRepository;
import com.example.project3api.repository.CardListRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CardListService {

    private final CardListRepository cardListRepository;
    private final CardListItemRepository cardListItemRepository;

    public CardListService(CardListRepository cardListRepository, CardListItemRepository cardListItemRepository) {
        this.cardListRepository = cardListRepository;
        this.cardListItemRepository = cardListItemRepository;
    }

    public List<CardList> getListsForUser(String userId) {
        requireUserId(userId);
        return cardListRepository.findByUserIdOrderByCreatedAtDesc(userId.trim());
    }

    public CardList createList(CreateCardListRequest request) {
        String userId = requireUserId(request.getUserId());
        String name = requireListName(request.getName());

        CardList cardList = new CardList();
        cardList.setUserId(userId);
        cardList.setName(name);
        return cardListRepository.save(cardList);
    }

    public List<CardListItem> getListItems(Long listId, String userId) {
        CardList list = requireOwnedList(listId, userId);
        return cardListItemRepository.findByListIdOrderByCreatedAtDesc(list.getId());
    }

    public CardListItem addCardToList(Long listId, AddCardToListRequest request) {
        CardList list = requireOwnedList(listId, request.getUserId());
        String cardId = requireCardId(request.getCardId());

        return cardListItemRepository
            .findByListIdAndCardId(list.getId(), cardId)
            .orElseGet(() -> {
                CardListItem listItem = new CardListItem();
                listItem.setListId(list.getId());
                listItem.setCardId(cardId);
                return cardListItemRepository.save(listItem);
            });
    }

    private CardList requireOwnedList(Long listId, String userId) {
        if (listId == null) {
            throw new IllegalArgumentException("listId is required");
        }
        String normalizedUserId = requireUserId(userId);
        CardList list = cardListRepository.findById(listId)
            .orElseThrow(() -> new IllegalArgumentException("List not found"));

        if (!normalizedUserId.equals(list.getUserId())) {
            throw new IllegalArgumentException("List does not belong to user");
        }
        return list;
    }

    private String requireUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }

    private String requireListName(String listName) {
        if (!StringUtils.hasText(listName)) {
            throw new IllegalArgumentException("name is required");
        }
        return listName.trim();
    }

    private String requireCardId(String cardId) {
        if (!StringUtils.hasText(cardId)) {
            throw new IllegalArgumentException("cardId is required");
        }
        return cardId.trim();
    }
}
