package com.flamedavid.eurovision.initializers;

import com.flamedavid.eurovision.entities.VoteStatus;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.repositories.VoteStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class VoteStatusInitializer implements CommandLineRunner {

    private final VoteStatusRepository voteStatusRepository;

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(VoteCategory.values())
                .forEach(category -> {
                    if (!voteStatusRepository.existsByCategory(category)) {
                        var voteStatus = new VoteStatus();
                        voteStatus.setCategory(category);
                        voteStatus.setOpen(false);
                        voteStatus.setVotedUsers(Set.of());
                        voteStatusRepository.save(voteStatus);
                    }
                });
    }
}
