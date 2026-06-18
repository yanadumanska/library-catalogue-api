package com.library.catalogue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRequestDto {

    @NotNull(message = "Book ID is required")
    private UUID bookId;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer expectedDurationDays;
}
