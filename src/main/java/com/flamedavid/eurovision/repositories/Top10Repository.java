package com.flamedavid.eurovision.repositories;

import com.flamedavid.eurovision.entities.Top10;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface Top10Repository extends JpaRepository<Top10, UUID> {
    Optional<Top10> findByCode(String code);
    void deleteByCode(String code);
}
