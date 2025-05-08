package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.CountryEnum;

public record FinalScoreDTO(String username, CountryEnum assignedCountry, int totalScore, int bonoPoints, int bonaPoints,
                            int winnerPoints, int bestFoodPoints, int bestGuestOutfitPoints, int rankingAccuracy) {
}
