package com.flamedavid.eurovision.controllers;

import com.flamedavid.eurovision.dtos.MessageDTO;
import com.flamedavid.eurovision.dtos.VoteCategoryDTO;
import com.flamedavid.eurovision.dtos.VoteRequestDTO;
import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.enums.CountryEnum;
import com.flamedavid.eurovision.enums.VoteCategory;
import com.flamedavid.eurovision.services.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/submit/{category}")
    public ResponseEntity<MessageDTO> submitVote(
        @AuthenticationPrincipal User user,
        @PathVariable VoteCategory category,
        @RequestBody VoteRequestDTO request
    ) {
        voteService.submitVote(user, category, request);
        return ResponseEntity.ok(new MessageDTO("Votes received!"));
    }

    @GetMapping("/available-countries/{category}")
    public ResponseEntity<List<CountryEnum>> getAvailableCountriesForCategory(
        @AuthenticationPrincipal User user,
        @PathVariable VoteCategory category
    ) {
        List<CountryEnum> countries = voteService.getAvailableCountriesForCategory(category, user.getUsername());
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/open-category")
    public ResponseEntity<VoteCategoryDTO> getOpenCategory() {
        return ResponseEntity.ok(voteService.getOpenCategory());
    }
}
