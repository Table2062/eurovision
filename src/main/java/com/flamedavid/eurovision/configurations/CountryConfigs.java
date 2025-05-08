package com.flamedavid.eurovision.configurations;

import com.flamedavid.eurovision.enums.CountryEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "countries")
@Data
public class CountryConfigs {
    private List<CountryEnum> finalists;
    private List<CountryEnum> bestFood;
    private List<CountryEnum> bestGuestOutfit;
    private List<CountryEnum> bono;
    private List<CountryEnum> bona;
}
