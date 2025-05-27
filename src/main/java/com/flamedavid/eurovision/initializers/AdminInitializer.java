package com.flamedavid.eurovision.initializers;

import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.default-password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se esiste già un admin nel database
        if (!userRepository.existsByUsername(adminUsername)) {
            // Crea un nuovo admin
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail("nomail@nomail.it");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setAdmin(true);

            // Salva l'admin nel database
            userRepository.save(admin);
            System.out.println("Admin account created successfully!");
        } else {
            System.out.println("Admin account already exists.");
        }
    }

}
