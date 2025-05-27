package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.VoteCategory;

import java.util.List;

public record VoteStatusDTO(VoteCategory category, boolean open, List<String> voters) {
}
