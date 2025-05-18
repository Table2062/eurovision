package com.flamedavid.eurovision.repositories;

import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.enums.CountryEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByAdminFalse(); // Trova tutti gli utenti non admin
    List<User> findAllByAdminFalseAndAwardRankingEnabledTrue(); // Trova tutti gli utenti non admin con award ranking abilitato
    int countByAdminFalse();
    Optional<User> findByUsername(String username);  // Cerca per username
    boolean existsByUsername(String username);       // Verifica se esiste un
    boolean existsByAssignedCountry(CountryEnum countryEnum);
    Optional<User> findByAssignedCountry(CountryEnum countryEnum);
}
