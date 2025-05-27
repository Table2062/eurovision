package com.flamedavid.eurovision.services;

import com.flamedavid.eurovision.dtos.CountryResultDTO;
import com.flamedavid.eurovision.dtos.FinalScoreDTO;
import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.exceptions.AppException;
import com.flamedavid.eurovision.repositories.UserRepository;
import com.flamedavid.eurovision.utils.EmailTemplateUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final RankingService rankingService;
    private final UserRepository userRepository;
    private final VoteService voteService;

    @Transactional
    public void sendFinalEmail() {
        var users = userRepository.findAllByAdminFalse();
        var generalScores = rankingService.calculateFinalScores(users.size(), false);
        var awardedScores = rankingService.calculateFinalScores(users.size(), true);
        var bestFoodVotingResults = voteService.calculateResults(VoteCategory.BEST_FOOD, 3).countryResults();
        var bestGuestOutfitVotingResults = voteService.calculateResults(VoteCategory.BEST_GUEST_OUTFIT, 3).countryResults();

        if (generalScores.size() < 3 || awardedScores.size() < 3) {
            throw new AppException("Final scores not valid.");
        }
        var top3 = List.of(generalScores.get(0), generalScores.get(1), generalScores.get(2));
        var top3Reward = List.of(awardedScores.get(0), awardedScores.get(1), awardedScores.get(2));

        users.stream()
            .filter(user -> StringUtils.isNotEmpty(user.getEmail()))
            .forEach(user -> CompletableFuture.runAsync(() -> {
                var toEmail = user.getEmail();
                var toUsername = user.getUsername();
                int toPoints = generalScores.stream()
                    .filter(score -> score.username().equals(user.getUsername()))
                    .findAny()
                    .map(FinalScoreDTO::totalScore)
                    .orElse(0);
                sendFinalEmail(toEmail, toUsername, String.valueOf(toPoints), top3, top3Reward,
                    bestFoodVotingResults, bestGuestOutfitVotingResults);
            }));
    }

    private void sendFinalEmail(String toEmail, String toUsername, String toPoints,
                                List<FinalScoreDTO> top3, List<FinalScoreDTO> top3Reward,
                                List<CountryResultDTO> bestFoodVotingResults,
                                List<CountryResultDTO> bestGuestOutfitVotingResults) {
        MimeMessage message = emailSender.createMimeMessage();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("USERNAME", toUsername);
        placeholders.put("POINTS", toPoints);
        placeholders.put("TOP1_USERNAME", top3.get(0).username());
        placeholders.put("TOP1_POINTS", String.valueOf(top3.get(0).totalScore()));
        placeholders.put("TOP2_USERNAME", top3.get(1).username());
        placeholders.put("TOP2_POINTS", String.valueOf(top3.get(1).totalScore()));
        placeholders.put("TOP3_USERNAME", top3.get(2).username());
        placeholders.put("TOP3_POINTS", String.valueOf(top3.get(2).totalScore()));
        placeholders.put("TOP1_REWARD_USERNAME", top3Reward.get(0).username());
        placeholders.put("TOP1_REWARD_POINTS", String.valueOf(top3Reward.get(0).totalScore()));
        placeholders.put("TOP2_REWARD_USERNAME", top3Reward.get(1).username());
        placeholders.put("TOP2_REWARD_POINTS", String.valueOf(top3Reward.get(1).totalScore()));
        placeholders.put("TOP3_REWARD_USERNAME", top3Reward.get(2).username());
        placeholders.put("TOP3_REWARD_POINTS", String.valueOf(top3Reward.get(2).totalScore()));

        placeholders.put("TOP1_FOOD_USERNAME", getUsername(bestFoodVotingResults.get(0).country().name()));
        placeholders.put("TOP1_FOOD_POINTS", String.valueOf(bestFoodVotingResults.get(0).points()));
        placeholders.put("TOP2_FOOD_USERNAME", getUsername(bestFoodVotingResults.get(1).country().name()));
        placeholders.put("TOP2_FOOD_POINTS", String.valueOf(bestFoodVotingResults.get(1).points()));
        placeholders.put("TOP3_FOOD_USERNAME", getUsername(bestFoodVotingResults.get(2).country().name()));
        placeholders.put("TOP3_FOOD_POINTS", String.valueOf(bestFoodVotingResults.get(2).points()));
        placeholders.put("TOP1_COSPLAY_USERNAME", getUsername(bestGuestOutfitVotingResults.get(0).country().name()));
        placeholders.put("TOP1_COSPLAY_POINTS", String.valueOf(bestGuestOutfitVotingResults.get(0).points()));
        placeholders.put("TOP2_COSPLAY_USERNAME", getUsername(bestGuestOutfitVotingResults.get(1).country().name()));
        placeholders.put("TOP2_COSPLAY_POINTS", String.valueOf(bestGuestOutfitVotingResults.get(1).points()));
        placeholders.put("TOP3_COSPLAY_USERNAME", getUsername(bestGuestOutfitVotingResults.get(2).country().name()));
        placeholders.put("TOP3_COSPLAY_POINTS", String.valueOf(bestGuestOutfitVotingResults.get(2).points()));
        String htmlContent = EmailTemplateUtil.loadTemplate("templates/final-email.html", placeholders);

        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom("noreply@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Votazioni Eurovision 2025 - Punteggi finali");
            helper.setText(htmlContent, true);
        } catch (MessagingException e) {
            throw new AppException(e);
        }

        emailSender.send(message);
    }

    private String getUsername(CountryEnum countryEnum) {
        return userRepository.findByAssignedCountry(countryEnum)
            .map(User::getUsername)
            .orElse("");
    }
}