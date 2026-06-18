package com.library.catalogue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingResponseDto {
    private UUID id;
    private UUID bookId;
    private String bookTitle;
    private UUID userId;
    private String userName;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
    private Integer renewalCount;
    private Double fineAmount;
}
