package com.flamedavid.eurovision.enums;

import lombok.Getter;

@Getter
public enum VoteCategory {
    EUROVISION("Eurovision"),
    BEST_FOOD("Best Food"),
    BEST_GUEST_OUTFIT("Best Guest Outfit"),
    BONO("Il più bono"),
    BONA("La più bona"),
    BEST_SINGER_OUTFIT("Best Singer Outfit");

    private final String categoryName;

    VoteCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}
