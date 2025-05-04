package com.flamedavid.eurovision.controllers;

import com.flamedavid.eurovision.dtos.ChangePasswordDTO;
import com.flamedavid.eurovision.dtos.MessageDTO;
import com.flamedavid.eurovision.dtos.RevealVoteDTO;
import com.flamedavid.eurovision.dtos.UserSummaryDTO;
import com.flamedavid.eurovision.dtos.UserVotingResultsDTO;
import com.flamedavid.eurovision.dtos.VotingResultsDTO;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.services.UserService;
import com.flamedavid.eurovision.services.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService; // Servizio per la gestione dell'utente
    private final VoteService voteService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/users/{username}")
    public ResponseEntity<MessageDTO> deleteUser(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        return ResponseEntity.ok(new MessageDTO("User deleted!"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/users/{username}/password")
    public ResponseEntity<MessageDTO> changeUserPassword(@PathVariable String username,
                                                   @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.overwriteUserPassword(username, changePasswordDTO.password());
        return ResponseEntity.ok(new MessageDTO("Password updated!"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllNonAdminUsers() {
        List<UserSummaryDTO> users = userService.getAllNonAdminUsers();
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/close-voting/{category}")
    public ResponseEntity<MessageDTO> closeVoting(@PathVariable VoteCategory category) {
        voteService.updateVoteStatus(category, false);
        return ResponseEntity.ok(new MessageDTO("Voting closed!"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/open-voting/{category}")
    public ResponseEntity<MessageDTO> openVoting(@PathVariable VoteCategory category) {
        voteService.updateVoteStatus(category, true);
        return ResponseEntity.ok(new MessageDTO("Voting opened!"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/reveal-vote")
    public ResponseEntity<MessageDTO> revealVote(@RequestBody RevealVoteDTO revealVoteDTO) {
        voteService.revealVote(revealVoteDTO.category(), revealVoteDTO.username(), revealVoteDTO.points());
        return ResponseEntity.ok(new MessageDTO("Vote revealed!"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/voting-results/{category}")
    public ResponseEntity<VotingResultsDTO> getVotingResults(@PathVariable VoteCategory category) {
        return ResponseEntity.ok(voteService.calculateResults(category));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/voting-results/{category}/users/{username}")
    public ResponseEntity<UserVotingResultsDTO> getVotingResultsForUser(@PathVariable VoteCategory category,
                                                                        @PathVariable String username) {
        return ResponseEntity.ok(voteService.getUserVotingResults(username, category));
    }

    //get user that already voted a category
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{category}")
    public ResponseEntity<List<String>> getUsersThatVoted(@PathVariable VoteCategory category) {
        return ResponseEntity.ok(voteService.getUsersThatVoted(category));
    }
}
