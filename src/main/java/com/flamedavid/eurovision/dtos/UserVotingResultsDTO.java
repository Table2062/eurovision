package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.VoteCategory;

import java.util.List;

public record UserVotingResultsDTO(String username, VoteCategory category, List<UserCountryResultDTO> countryResults) {
}