package com.flamedavid.eurovision.repositories;

import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.entities.UserVote;
import com.flamedavid.eurovision.enums.VoteCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVoteRepository extends JpaRepository<UserVote, UUID> {
    boolean existsByUserAndCategory(User user, VoteCategory category);
    Optional<UserVote> findByUserAndCategory(User user, VoteCategory category);
    List<UserVote> findAllByCategory(VoteCategory category);
}
