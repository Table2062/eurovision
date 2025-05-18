package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.VoteCategory;

public record RevealVoteDTO(String username, VoteCategory category, int points) {
}
