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
public class ReviewResponseDto {
    private UUID id;
    private UUID bookId;
    private UUID userId;
    private Integer rating;
    private String title;
    private String content;
    private Boolean spoilerFlag;
    private LocalDateTime createdAt;
}
