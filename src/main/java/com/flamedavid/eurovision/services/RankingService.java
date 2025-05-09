package com.flamedavid.eurovision.services;

import com.flamedavid.eurovision.dtos.CountryResultDTO;
import com.flamedavid.eurovision.dtos.FinalScoreDTO;
import com.flamedavid.eurovision.dtos.VoteEntryDTO;
import com.flamedavid.eurovision.entities.SingleVote;
import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.repositories.UserRepository;
import com.flamedavid.eurovision.repositories.UserVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserVoteRepository userVoteRepository;
    private final UserRepository userRepository;
    private final VoteService voteService;

    public List<FinalScoreDTO> calculateFinalScores(List<VoteEntryDTO> officialRankingTop10, int limit) {
        List<User> users = userRepository.findAllByAdminFalse();
        var bonoCountry = voteService.calculateResults(VoteCategory.BONO, true, 1)
            .countryResults().get(0).countryEnum();
        var bonaCountry = voteService.calculateResults(VoteCategory.BONA, true, 1)
            .countryResults().get(0).countryEnum();
        var winnerCountry = voteService.calculateResults(VoteCategory.WINNER, true, 1)
            .countryResults().get(0).countryEnum();
        var bestSingerOutfit = voteService.calculateResults(VoteCategory.BEST_SINGER_OUTFIT, true, 1)
            .countryResults().get(0).countryEnum();
        var bestFoodResults = voteService.calculateResults(VoteCategory.BEST_FOOD, true, 5)
            .countryResults().stream().map(CountryResultDTO::countryEnum).toList();
        var bestGuestOutfitResults = voteService.calculateResults(VoteCategory.BEST_GUEST_OUTFIT, true, 5)
            .countryResults().stream().map(CountryResultDTO::countryEnum).toList();

        return users.stream()
            .map(user -> computeFinalScore(user, bonoCountry, bonaCountry, winnerCountry,
                bestSingerOutfit, bestFoodResults, bestGuestOutfitResults, officialRankingTop10))
            .sorted(Comparator.comparingInt(FinalScoreDTO::totalScore).reversed())
            .limit(limit)
            .toList();
    }

    public FinalScoreDTO computeFinalScore(User user,
                                           CountryEnum bonoCountry,
                                           CountryEnum bonaCountry,
                                           CountryEnum winnerCountry,
                                           CountryEnum bestSingerOutfit,
                                           List<CountryEnum> bestFoodResults,
                                           List<CountryEnum> bestGuestOutfitResults,
                                           List<VoteEntryDTO> officialRankingTop10) {
        String username = user.getUsername();
        CountryEnum assignedCountry = user.getAssignedCountry();

        int bonoPoints = getSingleVotePoints(user, VoteCategory.BONO, bonoCountry);
        int bonaPoints = getSingleVotePoints(user, VoteCategory.BONA, bonaCountry);
        int winnerPoints = getSingleVotePoints(user, VoteCategory.WINNER, winnerCountry);
        int bestSingerOutfitPoints = getSingleVotePoints(user, VoteCategory.BEST_SINGER_OUTFIT, bestSingerOutfit);

        int bestFoodPoints = getAssignedCountryPoints(user, VoteCategory.BEST_FOOD, bestFoodResults);
        int bestGuestOutfitPoints = getAssignedCountryPoints(user, VoteCategory.BEST_GUEST_OUTFIT,
            bestGuestOutfitResults);
        int rankingAccuracy = calculateEurovisionAccuracyScore(user, officialRankingTop10);

        // Somma totale
        int totalScore = bonoPoints + bonaPoints + winnerPoints + bestSingerOutfitPoints +
            bestFoodPoints + bestGuestOutfitPoints + rankingAccuracy;

        return new FinalScoreDTO(username, assignedCountry, totalScore,
            bonoPoints, bonaPoints, winnerPoints, bestFoodPoints, bestGuestOutfitPoints, rankingAccuracy);
    }

    private int calculateEurovisionAccuracyScore(User user, List<VoteEntryDTO> officialRanking) {
        return userVoteRepository.findByUserAndCategory(user, VoteCategory.EUROVISION)
            .map(userVote -> {
                var sortedUserRankingCountries = userVote.getVotes().stream()
                    .sorted(Comparator.comparingInt(SingleVote::getPoints).reversed())
                    .map(SingleVote::getCountry)
                    .toList();
                var sortedOfficialRankingCountries = officialRanking.stream().sorted(Comparator.comparingInt(VoteEntryDTO::points).reversed())
                    .map(VoteEntryDTO::country)
                    .toList();
                return calculateEurovisionAccuracyScore(sortedUserRankingCountries, sortedOfficialRankingCountries);
            })
            .orElse(0);
    }

    private int calculateEurovisionAccuracyScore(List<CountryEnum> userRanking, List<CountryEnum> officialRanking) {
        int score = 0;
        for (int i = 0; i < userRanking.size(); i++) {
            CountryEnum userCountry = userRanking.get(i);
            int officialIndex = officialRanking.indexOf(userCountry);

            if (officialIndex == -1) continue; // la nazione non è nella top 10 ufficiale

            int distance = Math.abs(i - officialIndex);

            if (distance == 0) {
                score += 3;
            } else if (distance == 1) {
                score += 2;
            } else if (distance == 2) {
                score += 1;
            }
        }
        return score;
    }

    private int getSingleVotePoints(User user, VoteCategory category, CountryEnum actualWinner) {
        return userVoteRepository.findByUserAndCategory(user, category)
            .map(userVote -> {
                if (userVote.getVotes().get(0).getCountry().equals(actualWinner)) {
                    return 12;
                }
                return 0;
            })
            .orElse(0);
    }

    private int getAssignedCountryPoints(User user, VoteCategory category, List<CountryEnum> finalRanking) {
        return userVoteRepository.findByUserAndCategory(user, category)
            .map(userVote -> {
                int position = finalRanking.indexOf(user.getAssignedCountry());
                return switch (position) {
                    case 0 -> 12;
                    case 1 -> 10;
                    case 2 -> 8;
                    case 3 -> 7;
                    case 4 -> 6;
                    default -> 0;
                };
            })
            .orElse(0);
    }

}
