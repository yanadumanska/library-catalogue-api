package com.library.catalogue.dto;

import com.library.catalogue.enums.BookFormat;
import com.library.catalogue.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDto {
    private UUID id;
    private String isbn;
    private String title;
    private String subtitle;
    private String description;
    private BookFormat format;
    private BookStatus status;
    private Integer availableCopies;
    private BigDecimal averageRating;

    private List<AuthorResponseDto> authors;
    private List<CategoryResponseDto> categories;
}
