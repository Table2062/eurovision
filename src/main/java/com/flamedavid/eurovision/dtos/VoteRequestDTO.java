package com.flamedavid.eurovision.dtos;

import java.util.List;

public record VoteRequestDTO(
    List<VoteEntryDTO> votes
) {}
