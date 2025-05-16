package com.flamedavid.eurovision.dtos;

public record FinalScoreDTO(String username, CountryDTO assignedCountry, int totalScore, int bonoPoints,
                            int bonaPoints, int winnerPoints, int bestSingerOutfit,
                            int bestFoodPoints, int bestGuestOutfitPoints, int rankingAccuracy) {
}
