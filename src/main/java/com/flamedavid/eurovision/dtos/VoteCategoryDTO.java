package com.flamedavid.eurovision.dtos;

import java.util.List;

public record VoteCategoryDTO(String name, String label, List<Integer> votePoints) {
}
