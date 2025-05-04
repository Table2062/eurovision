package com.flamedavid.eurovision.repositories;

import com.flamedavid.eurovision.entities.VoteStatus;
import com.flamedavid.eurovision.enums.VoteCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteStatusRepository extends JpaRepository<VoteStatus, VoteCategory> {
    Optional<VoteStatus> findByCategory(VoteCategory category);
    boolean existsByCategory(VoteCategory category);
    Optional<VoteStatus> findByOpenTrue(); // Trova se c'è una categoria con votazione aperta
}
