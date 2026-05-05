package com.example.project3api.controller;

import com.example.project3api.dto.AddCardToListRequest;
import com.example.project3api.dto.CreateCardListRequest;
import com.example.project3api.model.CardList;
import com.example.project3api.model.CardListItem;
import com.example.project3api.service.CardListService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
public class CardListController {

    private final CardListService cardListService;

    public CardListController(CardListService cardListService) {
        this.cardListService = cardListService;
    }

    @GetMapping
    public List<CardList> getLists(@RequestParam String userId) {
        return cardListService.getListsForUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardList createList(@RequestBody CreateCardListRequest request) {
        return cardListService.createList(request);
    }

    @GetMapping("/{listId}/cards")
    public List<CardListItem> getListCards(
        @PathVariable Long listId,
        @RequestParam String userId
    ) {
        return cardListService.getListItems(listId, userId);
    }

    @PostMapping("/{listId}/cards")
    public CardListItem addCardToList(
        @PathVariable Long listId,
        @RequestBody AddCardToListRequest request
    ) {
        return cardListService.addCardToList(listId, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
