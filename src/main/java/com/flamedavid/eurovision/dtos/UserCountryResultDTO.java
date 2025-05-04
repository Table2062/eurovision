package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.CountryEnum;

public record UserCountryResultDTO(CountryEnum countryEnum, int points, boolean revealed) {
}
