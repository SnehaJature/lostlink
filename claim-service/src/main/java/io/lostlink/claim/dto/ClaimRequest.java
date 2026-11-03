package io.lostlink.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClaimRequest(@NotNull UUID matchId,
                           @NotBlank @Size(max = 2000) String description,
                           @NotBlank @Size(max = 5000) String proofDetails) {}
