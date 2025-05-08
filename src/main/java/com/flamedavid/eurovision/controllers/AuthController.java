package com.flamedavid.eurovision.controllers;

import com.flamedavid.eurovision.dtos.CountryDTO;
import com.flamedavid.eurovision.dtos.CountryListResponseDTO;
import com.flamedavid.eurovision.dtos.LoginRequestDTO;
import com.flamedavid.eurovision.dtos.LoginResponseDTO;
import com.flamedavid.eurovision.dtos.RegisterUserDTO;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(userService.login(loginRequestDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserDTO> registerUser(@RequestBody RegisterUserDTO registerUserDTO) {
        userService.registerNewUser(registerUserDTO);
        return ResponseEntity.ok(registerUserDTO);
    }

    @GetMapping("/all-countries")
    public ResponseEntity<CountryListResponseDTO> getAllCountries() {
        var countries = Arrays.stream(CountryEnum.values())
            .map(countryEnum -> new CountryDTO(countryEnum.name(), countryEnum.getCountryName()))
            .toList();
        return ResponseEntity.ok(new CountryListResponseDTO(countries));
    }

}
