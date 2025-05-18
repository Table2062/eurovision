package com.flamedavid.eurovision.repositories;

import com.flamedavid.eurovision.entities.SingleVote;
import com.flamedavid.eurovision.entities.UserVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SingleVoteRepository extends JpaRepository<SingleVote, UUID> {
    List<SingleVote> findByUserVote(UserVote userVote);
    boolean existsByUserVoteAndRevealedTrue(UserVote userVote);
}
