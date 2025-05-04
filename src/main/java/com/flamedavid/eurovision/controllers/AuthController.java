package com.flamedavid.eurovision.controllers;

import com.flamedavid.eurovision.dtos.LoginRequestDTO;
import com.flamedavid.eurovision.dtos.LoginResponseDTO;
import com.flamedavid.eurovision.dtos.RegisterUserDTO;
import com.flamedavid.eurovision.exceptions.UnauthorizedException;
import com.flamedavid.eurovision.security.JwtUtil;
import com.flamedavid.eurovision.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO req) {
        String errorMessage = "Wrong login!";
        var user = userService.getUserByUsername(req.username())
            .orElseThrow(() -> new UnauthorizedException(errorMessage));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new UnauthorizedException(errorMessage);
        }
        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserDTO> registerUser(@RequestBody RegisterUserDTO registerUserDTO) {
        userService.registerNewUser(registerUserDTO);
        return ResponseEntity.ok(registerUserDTO);
    }

}
