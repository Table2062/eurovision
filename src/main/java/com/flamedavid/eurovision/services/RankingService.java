package com.flamedavid.eurovision.services;

import com.flamedavid.eurovision.dtos.CountryDTO;
import com.flamedavid.eurovision.dtos.CountryResultDTO;
import com.flamedavid.eurovision.dtos.FinalScoreDTO;
import com.flamedavid.eurovision.dtos.MessageDTO;
import com.flamedavid.eurovision.entities.SingleVote;
import com.flamedavid.eurovision.entities.Top10;
import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.exceptions.BadRequestException;
import com.flamedavid.eurovision.repositories.Top10Repository;
import com.flamedavid.eurovision.repositories.UserRepository;
import com.flamedavid.eurovision.repositories.UserVoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {
    private static String FINAL_TOP10_CODE = "FINAL";

    private final UserVoteRepository userVoteRepository;
    private final UserRepository userRepository;
    private final Top10Repository top10Repository;
    private final VoteService voteService;

    public List<String> getFinalTop10() {
        return top10Repository.findByCode(FINAL_TOP10_CODE)
            .map(Top10::getPositions)
            .map(positions -> Arrays.stream(positions.split(",")).toList())
            .orElse(List.of());
    }

    public MessageDTO saveFinalTop10(List<CountryEnum> finalTop10) {
        if (finalTop10.size() != 10) {
            return new MessageDTO("The final top 10 must contain exactly 10 countries.");
        }
        var top10 = new Top10();
        String positions = finalTop10.stream()
            .map(CountryEnum::name)
            .collect(Collectors.joining(","));
        top10.setCode(FINAL_TOP10_CODE);
        top10.setPositions(positions);
        top10Repository.save(top10);
        return new MessageDTO("Final top 10 saved successfully.");
    }

    @Transactional
    public MessageDTO deleteFinalTop10() {
        top10Repository.deleteByCode(FINAL_TOP10_CODE);
        return new MessageDTO("Final top 10 deleted successfully.");
    }

    public List<FinalScoreDTO> calculateFinalScores(Integer limit, boolean canBeAwardedOnly) {
        var finalTop10 = top10Repository.findByCode(FINAL_TOP10_CODE)
            .map(Top10::getPositions)
            .map(positions -> Arrays.stream(positions.split(","))
                .map(CountryEnum::fromString)
                .toList())
            .orElseThrow(() -> new BadRequestException("You should persist the final top 10 first!"));

        List<User> users = canBeAwardedOnly ? userRepository.findAllByAdminFalseAndAwardRankingEnabledTrue()
            : userRepository.findAllByAdminFalse();

        if (Objects.isNull(limit) || limit <= 0) {
            limit = users.size();
        }
        var bonoCountry = voteService.calculateResults(VoteCategory.BONO, 1)
            .countryResults().get(0).country().name();
        var bonaCountry = voteService.calculateResults(VoteCategory.BONA,  1)
            .countryResults().get(0).country().name();
        var winnerCountry = finalTop10.get(0);
        var bestSingerOutfit = voteService.calculateResults(VoteCategory.BEST_SINGER_OUTFIT,  1)
            .countryResults().get(0).country().name();
        var bestFoodResults = voteService.calculateResults(VoteCategory.BEST_FOOD,  5)
            .countryResults().stream().map(CountryResultDTO::country).map(CountryDTO::name).toList();
        var bestGuestOutfitResults = voteService.calculateResults(VoteCategory.BEST_GUEST_OUTFIT,  5)
            .countryResults().stream().map(CountryResultDTO::country).map(CountryDTO::name).toList();

        return users.stream()
            .map(user -> computeFinalScore(user, bonoCountry, bonaCountry, winnerCountry,
                bestSingerOutfit, bestFoodResults, bestGuestOutfitResults, finalTop10))
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
                                           List<CountryEnum> finalTop10) {
        String username = user.getUsername();
        CountryEnum assignedCountry = user.getAssignedCountry();

        int bonoPoints = getSingleVotePoints(user, VoteCategory.BONO, bonoCountry, 3);
        int bonaPoints = getSingleVotePoints(user, VoteCategory.BONA, bonaCountry, 3);
        int winnerPoints = getSingleVotePoints(user, VoteCategory.WINNER, winnerCountry, 6);
        int bestSingerOutfitPoints = getSingleVotePoints(user, VoteCategory.BEST_SINGER_OUTFIT, bestSingerOutfit, 3);

        int bestFoodPoints = getAssignedCountryPoints(user, VoteCategory.BEST_FOOD, bestFoodResults);
        int bestGuestOutfitPoints = getAssignedCountryPoints(user, VoteCategory.BEST_GUEST_OUTFIT,
            bestGuestOutfitResults);
        int rankingAccuracy = calculateEurovisionAccuracyScore(user, finalTop10);

        // Somma totale
        int totalScore = bonoPoints + bonaPoints + winnerPoints + bestSingerOutfitPoints +
            bestFoodPoints + bestGuestOutfitPoints + rankingAccuracy;

        var countryDTO = new CountryDTO(assignedCountry, assignedCountry.getLabel(), assignedCountry.getCountryCode(), null, user.getUsername());
        return new FinalScoreDTO(username, countryDTO, totalScore,
            bonoPoints, bonaPoints, winnerPoints, bestSingerOutfitPoints,
            bestFoodPoints, bestGuestOutfitPoints, rankingAccuracy);
    }

    private int calculateEurovisionAccuracyScore(User user, List<CountryEnum> finalTop10) {
        return userVoteRepository.findByUserAndCategory(user, VoteCategory.EUROVISION)
            .map(userVote -> {
                var sortedUserRankingCountries = userVote.getVotes().stream()
                    .sorted(Comparator.comparingInt(SingleVote::getPoints).reversed())
                    .map(SingleVote::getCountry)
                    .toList();
                return calculateEurovisionAccuracyScore(sortedUserRankingCountries, finalTop10);
            })
            .orElse(0);
    }

    private int calculateEurovisionAccuracyScore(List<CountryEnum> userRanking, List<CountryEnum> finalTop10) {
        int score = 0;
        for (int i = 0; i < userRanking.size(); i++) {
            CountryEnum userCountry = userRanking.get(i);
            int officialIndex = finalTop10.indexOf(userCountry);

            if (officialIndex == -1) continue; // la nazione non è nella top 10 ufficiale

            int distance = Math.abs(i - officialIndex);

            if (distance == 0) {
                score += 4;
            } else if (distance == 1) {
                score += 2;
            } else if (distance == 2) {
                score += 1;
            }
        }
        return score;
    }

    private int getSingleVotePoints(User user, VoteCategory category, CountryEnum actualWinner, int points) {
        return userVoteRepository.findByUserAndCategory(user, category)
            .map(userVote -> {
                if (userVote.getVotes().get(0).getCountry().equals(actualWinner)) {
                    return points;
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
                    case 0 -> 5;
                    case 1 -> 4;
                    case 2 -> 3;
                    case 3 -> 2;
                    case 4 -> 1;
                    default -> 0;
                };
            })
            .orElse(0);
    }

}
