package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.VoteCategory;

import java.util.List;

public record VotingResultsDTO(boolean completed, VoteCategory category, List<CountryResultDTO> countryResults) {
}
