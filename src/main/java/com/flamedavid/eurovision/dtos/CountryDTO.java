package com.flamedavid.eurovision.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flamedavid.eurovision.enums.CountryEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryDTO(CountryEnum name, String label, String isoCode, String participant, String assignedGuest) {
}
