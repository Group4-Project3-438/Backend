package com.example.project3api.controller;

import com.example.project3api.dto.CreateFavoriteRequest;
import com.example.project3api.model.FavoriteCard;
import com.example.project3api.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoriteCard> getFavorites(@RequestParam String userId) {
        return favoriteService.getByUserId(userId);
    }

    @PostMapping
    public FavoriteCard addFavorite(@RequestBody CreateFavoriteRequest request) {
        return favoriteService.addFavorite(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
