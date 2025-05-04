package com.flamedavid.eurovision.dtos;

import com.flamedavid.eurovision.enums.CountryEnum;

public record CountryResultDTO(CountryEnum countryEnum, int points) {
}
