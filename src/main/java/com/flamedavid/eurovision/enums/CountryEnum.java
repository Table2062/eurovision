package com.flamedavid.eurovision.enums;

import com.flamedavid.eurovision.exceptions.NotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum CountryEnum {
    ALBANIA("Albania"),
    ARMENIA("Armenia"),
    AUSTRALIA("Australia"),
    AUSTRIA("Austria"),
    AZERBAIJAN("Azerbaijan"),
    BELGIUM("Belgium"),
    CROATIA("Croatia"),
    CYPRUS("Cyprus"),
    CZECHIA("Czechia"),
    DENMARK("Denmark"),
    ESTONIA("Estonia"),
    FINLAND("Finland"),
    FRANCE("France"),
    GEORGIA("Georgia"),
    GERMANY("Germany"),
    GREECE("Greece"),
    ICELAND("Iceland"),
    IRELAND("Ireland"),
    ISRAEL("Israel"),
    ITALY("Italy"),
    LATVIA("Latvia"),
    LITHUANIA("Lithuania"),
    LUXEMBOURG("Luxembourg"),
    MALTA("Malta"),
    MONTENEGRO("Montenegro"),
    NETHERLANDS("Netherlands"),
    NORWAY("Norway"),
    POLAND("Poland"),
    PORTUGAL("Portugal"),
    SAN_MARINO("San Marino"),
    SERBIA("Serbia"),
    SLOVENIA("Slovenia"),
    SPAIN("Spain"),
    SWEDEN("Sweden"),
    SWITZERLAND("Switzerland"),
    UKRAINE("Ukraine"),
    UNITED_KINGDOM("United Kingdom");

    private final String label;

    @Override
    public String toString() {
        return label;
    }

    public static CountryEnum fromString(String countryName) {
        for (CountryEnum country : CountryEnum.values()) {
            if (country.getLabel().equalsIgnoreCase(countryName)) {
                return country;
            } else if (country.name().equalsIgnoreCase(countryName)) {
                return country;
            }
        }
        throw new NotFoundException("No constant with country name " + countryName + " found");
    }

    public String getCountryCode() {
        for (String iso : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso);
            if (locale.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(label)) {
                return iso.toUpperCase();
            }
        }
        return name().substring(0,3).toUpperCase();
    }
}
