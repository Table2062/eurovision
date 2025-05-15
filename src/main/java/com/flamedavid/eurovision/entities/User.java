package com.flamedavid.eurovision.entities;

import com.flamedavid.eurovision.enums.CountryEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // criptata

    @Column
    private String email;

    @Column(nullable = false)
    private boolean admin = false;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private CountryEnum assignedCountry;

    @Column(name = "award_ranking_enabled")
    private boolean awardRankingEnabled = false;
}