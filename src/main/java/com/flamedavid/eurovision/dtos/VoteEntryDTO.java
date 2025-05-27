package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.CountryEnum;

public record VoteEntryDTO(
    CountryEnum country,
    int points
) {}
