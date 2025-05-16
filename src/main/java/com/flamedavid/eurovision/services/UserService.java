package com.flamedavid.eurovision.services;

import com.flamedavid.eurovision.dtos.LoginRequestDTO;
import com.flamedavid.eurovision.dtos.LoginResponseDTO;
import com.flamedavid.eurovision.dtos.RegisterUserDTO;
import com.flamedavid.eurovision.dtos.UserSummaryDTO;
import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.exceptions.BadRequestException;
import com.flamedavid.eurovision.exceptions.NotFoundException;
import com.flamedavid.eurovision.exceptions.UnauthorizedException;
import com.flamedavid.eurovision.repositories.UserRepository;
import com.flamedavid.eurovision.repositories.VoteStatusRepository;
import com.flamedavid.eurovision.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VoteStatusRepository voteStatusRepository;
    private final JwtUtil jwtUtil;

    @Value("${admin.username}")
    private String adminUsername;

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        String errorMessage = "Wrong login!";
        var user = getUserByUsername(loginRequestDTO.username())
            .orElseThrow(() -> new UnauthorizedException(errorMessage));
        if (!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new UnauthorizedException(errorMessage);
        }
        String token = jwtUtil.generateToken(user.getId(), user.isAdmin());
        return new LoginResponseDTO(token);
    }

    public void registerNewUser(RegisterUserDTO registerUserDTO) {
        var countryEnum = CountryEnum.fromString(registerUserDTO.getAssignedCountry());

        //Controlla che nessuna categoria sia aperta per le votazioni
        voteStatusRepository.findByOpenTrue()
            .ifPresent(voteStatus -> {
                throw new BadRequestException("Cannot register a new user while voting is open");
            });
        // Verifica che l'username non sia già in uso
        if (userRepository.existsByUsername(registerUserDTO.getUsername())) {
            throw new BadRequestException("User with this username already exists");
        }
        if (adminUsername.equals(registerUserDTO.getUsername())) {
            throw new BadRequestException("Cannot register with the admin username");
        }
        //Verifica che il paese non sia già assegnato
        if (userRepository.existsByAssignedCountry(countryEnum)) {
            throw new BadRequestException("Country already assigned to another user");
        }

        // Crea il nuovo utente
        User newUser = new User();
        newUser.setUsername(registerUserDTO.getUsername());
        newUser.setEmail(registerUserDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerUserDTO.getPassword())); // Cripta la password
        newUser.setAssignedCountry(countryEnum);

        // Salva il nuovo utente
        userRepository.save(newUser);
    }

    public void deleteUserByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User not found: " + username);
        }

        User user = userOpt.get();
        if (user.isAdmin()) {
            throw new BadRequestException("Cannot delete an admin user.");
        }

        userRepository.delete(user);
    }

    public void overwriteUserPassword(String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User not found: " + username);
        }

        User user = userOpt.get();

        user.setPassword(passwordEncoder.encode(newPassword)); // Cripta la nuova password
        userRepository.save(user);
    }

    public List<UserSummaryDTO> getAllNonAdminUsers(boolean canBeAwardedOnly) {
        List<User> users;
        if (canBeAwardedOnly) {
            users = userRepository.findAllByAdminFalseAndAwardRankingEnabledTrue();
        } else {
            users = userRepository.findAllByAdminFalse();
        }
        return users.stream()
            .map(user -> new UserSummaryDTO(
                user.getUsername(),
                user.getAssignedCountry(),
                user.isAwardRankingEnabled()
            )).toList();
    }

    public void setAwardRankingEnabled(String username, boolean enabled) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found: " + username));
        user.setAwardRankingEnabled(enabled);
        userRepository.save(user);
    }

    public boolean existsByCountry(CountryEnum countryEnum) {
        return userRepository.existsByAssignedCountry(countryEnum);
    }
}
