package com.flamedavid.eurovision.services;

import com.flamedavid.eurovision.configurations.CountryConfigs;
import com.flamedavid.eurovision.dtos.CountryResultDTO;
import com.flamedavid.eurovision.dtos.UserCountryResultDTO;
import com.flamedavid.eurovision.dtos.UserVotingResultsDTO;
import com.flamedavid.eurovision.dtos.VoteCategoryDTO;
import com.flamedavid.eurovision.dtos.VoteRequestDTO;
import com.flamedavid.eurovision.dtos.VotingResultsDTO;
import com.flamedavid.eurovision.entities.SingleVote;
import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.entities.UserVote;
import com.flamedavid.eurovision.entities.VoteStatus;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.exceptions.BadRequestException;
import com.flamedavid.eurovision.exceptions.ForbiddenException;
import com.flamedavid.eurovision.exceptions.NotFoundException;
import com.flamedavid.eurovision.repositories.SingleVoteRepository;
import com.flamedavid.eurovision.repositories.UserRepository;
import com.flamedavid.eurovision.repositories.UserVoteRepository;
import com.flamedavid.eurovision.repositories.VoteStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteStatusRepository voteStatusRepository;
    private final UserVoteRepository userVoteRepository;
    private final SingleVoteRepository singleVoteRepository;
    private final UserRepository userRepository;
    private final CountryConfigs countryConfigs;

    public void revealVote(VoteCategory category, String username, int points) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        UserVote userVote = userVoteRepository.findByUserAndCategory(user, category)
            .orElseThrow(() -> new NotFoundException("Vote not found"));

        userVote.getVotes().stream()
            .filter(vote -> vote.getPoints() == points)
            .findAny()
            .ifPresentOrElse(vote -> {
                // Controlla se il voto è già rivelato
                if (vote.isRevealed()) {
                    throw new BadRequestException("Vote already revealed");
                }
                vote.setRevealed(true);
                singleVoteRepository.save(vote);
            }, () -> {
                throw new NotFoundException("Vote not found");
            });

    }

    public void updateVoteStatus(VoteCategory category, boolean open) {
        voteStatusRepository.findByOpenTrue().ifPresent(status -> {
            if (status.getCategory() != category) {
                throw new BadRequestException("There is already an open category");
            }
        });

        VoteStatus voteStatus = voteStatusRepository.findByCategory(category)
            .orElseThrow(() -> new NotFoundException("Vote status not found"));
        if (voteStatus.isOpen() == open) {
            throw new BadRequestException("Vote status is already " + (open ? "open" : "closed"));
        }
        voteStatus.setOpen(open);
        voteStatusRepository.save(voteStatus);
    }

    public void submitVote(User user, VoteCategory category, VoteRequestDTO dto) {
        // 1. Check admin
        if (user.isAdmin()) throw new ForbiddenException("The admin cannot vote");

        // 2. Check categoria aperta
        VoteStatus status = voteStatusRepository.findByCategory(category)
            .orElseThrow(() -> new NotFoundException("Category not found"));
        if (!status.isOpen()) throw new ForbiddenException("Closed votes");

        // 3. Check se ha già votato
        if (userVoteRepository.existsByUserAndCategory(user, category))
            throw new BadRequestException("Already voted");

        // 4. Regole per punti per categoria
        List<Integer> expectedPoints = switch (category) {
            case EUROVISION -> List.of(12, 10, 8, 7, 6, 5, 4, 3, 2, 1);
            case BEST_FOOD, BEST_GUEST_OUTFIT -> List.of(12, 10, 8, 7, 6);
            case BONO, BONA, BEST_SINGER_OUTFIT, WINNER -> List.of(12);
        };

        if (dto.votes().size() != expectedPoints.size()) {
            throw new BadRequestException("Wrong number of positions for the requested category");
        }

        var usedCountries = new HashSet<CountryEnum>();
        var usedPoints = new HashSet<Integer>();

        for (var vote : dto.votes()) {
            if (!expectedPoints.contains(vote.points()))
                throw new BadRequestException("Invalid points: " + vote.points());
            if (!usedPoints.add(vote.points()))
                throw new BadRequestException("Each position can be defined only one time");
            if (!usedCountries.add(vote.country()))
                throw new BadRequestException("Each country can be voted only once");

            if ((category == VoteCategory.BEST_FOOD || category == VoteCategory.BEST_GUEST_OUTFIT)) {
                // Autovoto vietato per alcune categorie
                if (vote.country().equals(user.getAssignedCountry())) {
                    throw new BadRequestException("You cannot vote your assigned country for this category.");
                }
            }

            // Controlla se la nazione è tra quelle ammesse
            switch(category) {
                case BEST_FOOD -> {
                    if (!countryConfigs.getBestFood().contains(vote.country())) {
                        throw new BadRequestException("The requested country cannot be voted for BEST_FOOD");
                    }
                }
                case BEST_GUEST_OUTFIT -> {
                    if (!countryConfigs.getBestGuestOutfit().contains(vote.country())) {
                        throw new BadRequestException("The requested country cannot be voted for BEST_GUEST_OUTFIT");
                    }
                }
                case BONO -> {
                    if (!countryConfigs.getBono().contains(vote.country())) {
                        throw new BadRequestException("The requested country cannot be voted for BONO");
                    }
                }
                case BONA -> {
                    if (!countryConfigs.getBona().contains(vote.country())) {
                        throw new BadRequestException("The requested country cannot be voted for BONA");
                    }
                }
                default -> {
                    if (!countryConfigs.getFinalists().contains(vote.country())) {
                        throw new BadRequestException("The requested country cannot be voted for EUROVISION");
                    }
                }
            }
        }

        // 5. Persistenza
        UserVote userVote = new UserVote();
        userVote.setUser(user);
        userVote.setCategory(category);

        List<SingleVote> voteEntities = dto.votes().stream().map(voteEntryDTO -> {
            SingleVote sv = new SingleVote();
            sv.setPoints(voteEntryDTO.points());
            sv.setCountry(voteEntryDTO.country());
            sv.setUserVote(userVote);
            sv.setRevealed(false);
            return sv;
        }).toList();

        userVote.setVotes(voteEntities);
        userVoteRepository.save(userVote);
    }

    public List<CountryEnum> getAvailableCountriesForCategory(VoteCategory category, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        List<CountryEnum> response;

        switch(category) {
            case BEST_FOOD -> response = countryConfigs.getBestFood().stream()
                .filter(countryEnum -> !countryEnum.equals(user.getAssignedCountry()))
                .toList();
            case BEST_GUEST_OUTFIT -> response = countryConfigs.getBestGuestOutfit().stream()
                .filter(countryEnum -> !countryEnum.equals(user.getAssignedCountry()))
                .toList();
            case BONO -> response = countryConfigs.getBono();
            case BONA -> response = countryConfigs.getBona();
            default -> response = countryConfigs.getFinalists();
        }
        return response;
    }

    public VoteCategoryDTO getOpenCategory() {
        return voteStatusRepository.findByOpenTrue()
            .map(VoteStatus::getCategory)
            .map(cat -> new VoteCategoryDTO(cat.name(), cat.getCategoryLabel()))
            .orElseThrow(() -> new NotFoundException("No open category found"));
    }

    public UserVotingResultsDTO getUserVotingResults(String username, VoteCategory category) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        UserVote userVote = userVoteRepository.findByUserAndCategory(user, category)
            .orElseThrow(() -> new NotFoundException("Vote not found"));

        List<SingleVote> votes = userVote.getVotes();
        List<UserCountryResultDTO> sortedResults = votes.stream()
            .map(vote -> new UserCountryResultDTO(vote.getCountry(), vote.getPoints(), vote.isRevealed()))
            .toList();
        return new UserVotingResultsDTO(username, category, sortedResults);
    }

    public VotingResultsDTO calculateResults(VoteCategory category, boolean showFirstVote, int limit) {
        int nonAdminUsersNum = userRepository.countByAdminFalse();
        List<UserVote> userVotes = userVoteRepository.findAllByCategory(category);
        boolean completed = userVotes.size() == nonAdminUsersNum;

        Map<CountryEnum, Integer> resultsMap = new EnumMap<>(CountryEnum.class);
        List<CountryEnum> countries = getAvailableCountriesForCategory(category);
        for (CountryEnum country : countries) {
            resultsMap.put(country, 0);
        }
        for (UserVote userVote : userVotes) {
            for (SingleVote singleVote : userVote.getVotes()) {
                if (singleVote.isRevealed()) {
                    resultsMap.put(singleVote.getCountry(), resultsMap.get(singleVote.getCountry()) + singleVote.getPoints());
                } else {
                    // Ci sono voti non rivelati quindi la classifica non è quella finale
                    completed = false;
                }
            }
        }
        List<CountryResultDTO> sortedResults = resultsMap.entrySet().stream()
            .map(entry -> new CountryResultDTO(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(CountryResultDTO::points).reversed())
            .limit(limit)
            .toList();
        if (!showFirstVote) {
            sortedResults = sortedResults.subList(1, sortedResults.size());
        }
        return new VotingResultsDTO(completed, category, sortedResults);
    }

    private List<CountryEnum> getAvailableCountriesForCategory(VoteCategory category) {
        List<CountryEnum> response;
        if (category == VoteCategory.BEST_FOOD) {
            response = countryConfigs.getBestFood();
        } else if (category == VoteCategory.BEST_GUEST_OUTFIT) {
            response = countryConfigs.getBestGuestOutfit();
        } else {
            response = Arrays.stream(CountryEnum.values()).toList();
        }
        return response;
    }

    public List<String> getUsersThatVoted(VoteCategory category) {
        return userVoteRepository.findAllByCategory(category)
            .stream()
            .map(userVote -> userVote.getUser().getUsername())
            .distinct()
            .toList();
    }
}
