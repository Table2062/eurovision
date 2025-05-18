package com.flamedavid.eurovision.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum VoteCategory {
    EUROVISION("Classifica Eurovision"),
    BEST_FOOD("Miglior cibo"),
    BEST_GUEST_OUTFIT("Miglior outfit - cosplay"),
    BONO("Il più bello"),
    BONA("La più bella"),
    BEST_SINGER_OUTFIT("Miglior outfit - cantante"),
    WINNER("Chi vince?");

    private final String label;
    private List<Integer> votePoints = List.of();

    VoteCategory(String label) {
        this.label = label;
        // 4. Regole per punti per categoria
        switch (this.name()) {
            case "EUROVISION" -> votePoints = List.of(12, 10, 8, 7, 6, 5, 4, 3, 2, 1);
            case "BEST_FOOD", "BEST_GUEST_OUTFIT" -> votePoints = List.of(12, 10, 8, 7, 6);
            case "BONO", "BONA", "BEST_SINGER_OUTFIT", "WINNER" -> votePoints = List.of(12);
        }
    }
}
