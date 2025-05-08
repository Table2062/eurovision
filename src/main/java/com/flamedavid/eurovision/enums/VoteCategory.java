package com.flamedavid.eurovision.enums;

import lombok.Getter;

@Getter
public enum VoteCategory {
    EUROVISION("Classifica Eurovision"),
    BEST_FOOD("Miglior cibo"),
    BEST_GUEST_OUTFIT("Miglior Outfit - Cosplay"),
    BONO("Il più bello"),
    BONA("La più bella"),
    BEST_SINGER_OUTFIT("Miglior Outfit - Cantante"),
    WINNER("Chi vince?");

    private final String categoryLabel;

    VoteCategory(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }
}
