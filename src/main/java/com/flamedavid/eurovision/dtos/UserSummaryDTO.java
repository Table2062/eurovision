package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.CountryEnum;

public record UserSummaryDTO(String username, CountryEnum country, boolean awardRankingEnabled) {
}
