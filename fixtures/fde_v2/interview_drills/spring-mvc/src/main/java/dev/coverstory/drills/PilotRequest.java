package dev.coverstory.drills;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PilotRequest(@NotBlank String workflow, @Min(1) int limit) {}
