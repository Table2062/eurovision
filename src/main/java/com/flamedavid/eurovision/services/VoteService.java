package com.flamedavid.eurovision.services;

import com.flamedavid.eurovision.configurations.CountryConfigs;
import com.flamedavid.eurovision.dtos.CategoryDTO;
import com.flamedavid.eurovision.dtos.CountryDTO;
import com.flamedavid.eurovision.dtos.CountryListResponseDTO;
import com.flamedavid.eurovision.dtos.CountryResultDTO;
import com.flamedavid.eurovision.dtos.MessageDTO;
import com.flamedavid.eurovision.dtos.UserCountryResultDTO;
import com.flamedavid.eurovision.dtos.UserVotingResultsDTO;
import com.flamedavid.eurovision.dtos.VoteCategoryDTO;
import com.flamedavid.eurovision.dtos.VoteRequestDTO;
import com.flamedavid.eurovision.dtos.VoteStatusDTO;
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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        if (dto.votes().size() != category.getVotePoints().size()) {
            throw new BadRequestException("Wrong number of positions for the requested category");
        }

        var usedCountries = new HashSet<CountryEnum>();
        var usedPoints = new HashSet<Integer>();

        for (var vote : dto.votes()) {
            if (!category.getVotePoints().contains(vote.points()))
                throw new BadRequestException("Invalid points: " + vote.points());
            if (!usedPoints.add(vote.points()))
                throw new BadRequestException("Each position can be defined only one time");
            if (!usedCountries.add(vote.country()))
                throw new BadRequestException("Each country can be voted only once");
            if ((category == VoteCategory.BEST_FOOD || category == VoteCategory.BEST_GUEST_OUTFIT) &&
                vote.country().equals(user.getAssignedCountry())) {
                throw new BadRequestException("You cannot vote your assigned country for this category.");
            }
            // Controlla se la nazione è tra quelle ammesse
            if (getAvailableCountriesForCategory(category).stream().noneMatch(countryDTO -> countryDTO.name().equals(vote.country()))) {
                throw new BadRequestException("The requested country cannot be voted for %s".formatted(category));
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

    public VoteCategoryDTO getOpenCategory(User user) {
        var voteCategory = voteStatusRepository.findByOpenTrue()
            .map(VoteStatus::getCategory)
            .orElseThrow(() -> new NotFoundException("No open category found!"));
        if (userVoteRepository.existsByUserAndCategory(user, voteCategory)) {
            throw new BadRequestException("You have already voted for the current category!");
        }
        return new VoteCategoryDTO(voteCategory.name(), voteCategory.getLabel(), voteCategory.getVotePoints());
    }

    public UserVotingResultsDTO getUserVotingResults(String username, VoteCategory category) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        UserVote userVote = userVoteRepository.findByUserAndCategory(user, category)
            .orElseThrow(() -> new NotFoundException("Vote not found"));

        var countryDTOMap = getCountryDTOStreamForCategory(category, null)
            .collect(Collectors.toMap(CountryDTO::name, dto -> dto));

        List<SingleVote> votes = userVote.getVotes();
        List<UserCountryResultDTO> sortedResults = votes.stream()
            .map(vote -> new UserCountryResultDTO(countryDTOMap.get(vote.getCountry()),
                    vote.getPoints(), vote.isRevealed()))
            .toList();
        return new UserVotingResultsDTO(username, category, sortedResults);
    }

    public MessageDTO deleteUserVotingResults(String username, VoteCategory category) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        UserVote userVote = userVoteRepository.findByUserAndCategory(user, category)
            .orElseThrow(() -> new NotFoundException("Vote not found"));
        if (singleVoteRepository.existsByUserVoteAndRevealedTrue(userVote)) {
            throw new BadRequestException("Cannot delete voting results with revealed votes");
        }
        userVoteRepository.delete(userVote);
        return new MessageDTO("Voting results deleted");
    }

    public VotingResultsDTO calculateResults(VoteCategory category, int limit) {
        int nonAdminUsersNum = userRepository.countByAdminFalse();
        List<UserVote> userVotes = userVoteRepository.findAllByCategory(category);
        boolean completed = userVotes.size() == nonAdminUsersNum;

        Map<CountryEnum, Integer> resultsMap = new HashMap<>();
        List<CountryDTO> countries = getAvailableCountriesForCategory(category);
        var countryDTOMap = getCountryDTOStreamForCategory(category, null)
            .collect(Collectors.toMap(CountryDTO::name, dto -> dto));
        for (CountryDTO country : countries) {
            resultsMap.put(country.name(), 0);
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
            .map(entry -> {
                var countryDTO = countryDTOMap.get(entry.getKey());
                return new CountryResultDTO(countryDTO, entry.getValue());
            })
            .sorted(Comparator.comparing(CountryResultDTO::points).reversed())
            .limit(limit)
            .toList();
        return new VotingResultsDTO(completed, category, sortedResults);
    }

    private List<CountryDTO> getAvailableCountriesForCategory(VoteCategory category) {
        Stream<CountryDTO> countryEnumsStream;
        switch(category) {
            case BEST_FOOD -> countryEnumsStream = countryConfigs.getBestFood().stream();
            case BEST_GUEST_OUTFIT -> countryEnumsStream = countryConfigs.getBestGuestOutfit().stream();
            case BONO -> countryEnumsStream = countryConfigs.getBono().stream();
            case BONA -> countryEnumsStream = countryConfigs.getBona().stream();
            default -> countryEnumsStream = countryConfigs.getFinalists().stream();
        }
        return countryEnumsStream.toList();
    }

    public Stream<CountryDTO> getCountryDTOStreamForCategory(VoteCategory category, User user) {
        var stream = switch (category) {
            case BEST_FOOD -> countryConfigs.getBestFood().stream()
                .filter(countryDTO -> Objects.isNull(user) || !countryDTO.name().equals(user.getAssignedCountry()));
            case BEST_GUEST_OUTFIT -> countryConfigs.getBestGuestOutfit().stream()
                .filter(countryDTO -> Objects.isNull(user) || !countryDTO.name().equals(user.getAssignedCountry()));
            case BONO -> countryConfigs.getBono().stream();
            case BONA -> countryConfigs.getBona().stream();
            default -> countryConfigs.getFinalists().stream();
        };
        return stream.map(dto -> CountryDTO.builder()
            .name(dto.name())
            .label(dto.name().getLabel())
            .isoCode(dto.name().getCountryCode())
            .participant(dto.participant())
            .assignedGuest(dto.assignedGuest())
            .build());
    }

    public CountryListResponseDTO getAvailableCountriesForCategory(VoteCategory category, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        Stream<CountryDTO> countryEnumsStream = getCountryDTOStreamForCategory(category, user);

        var countryUserMap = userRepository.findAllByAdminFalse().stream()
            .map(u -> Pair.of(u.getAssignedCountry(), u.getUsername()))
            .collect(Collectors.toMap(
                Pair::getFirst,
                Pair::getSecond,
                (existing, replacement) -> existing
            ));

        var countries = countryEnumsStream.map(dto -> {
                String assignedGuest = null;
                switch (category) {
                    case BEST_FOOD, BEST_GUEST_OUTFIT -> assignedGuest = countryUserMap.get(dto.name());
                }
                return new CountryDTO(dto.name(), dto.name().getLabel(),
                    dto.name().getCountryCode(), dto.participant(), assignedGuest);
            })
            .toList();

        return new CountryListResponseDTO(countries);
    }

    public VoteStatusDTO getUsersThatVoted(VoteCategory category) {
        List<String> voters = userVoteRepository.findAllByCategory(category)
            .stream()
            .map(userVote -> userVote.getUser().getUsername())
            .distinct()
            .toList();
        return voteStatusRepository.findByCategory(category)
            .map(voteStatus -> new VoteStatusDTO(category, voteStatus.isOpen(), voters))
            .orElse(new VoteStatusDTO(category, false, List.of()));
    }

    public List<CategoryDTO> getAllCategories() {
        return Arrays.stream(VoteCategory.values())
            .map(voteCategory ->
                new CategoryDTO(voteCategory.name(), voteCategory.getLabel(), getLimit(voteCategory)))
            .toList();
    }

    private int getLimit(VoteCategory category) {
        return switch (category) {
            case BEST_FOOD, BEST_GUEST_OUTFIT -> 5;
            default -> 10;
        };
    }
}
