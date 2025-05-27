package com.flamedavid.eurovision.configurations;

import com.flamedavid.eurovision.dtos.CountryDTO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "countries")
@Data
public class CountryConfigs {
    private List<CountryDTO> finalists;
    private List<CountryDTO> bestFood;
    private List<CountryDTO> bestGuestOutfit;
    private List<CountryDTO> bono;
    private List<CountryDTO> bona;
}
