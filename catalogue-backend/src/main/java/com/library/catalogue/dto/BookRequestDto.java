package com.library.catalogue.dto;

import com.library.catalogue.enums.BookFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDto {

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(97[8-9])\\d{10}$|^\\d{10}$|^\\d{13}$",
            message = "Invalid ISBN format")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title too long")
    private String title;

    private String subtitle;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;

    private String publisher;

    @Min(value = 1, message = "Page count must be at least 1")
    @Max(value = 10000, message = "Page count too high")
    private Integer pageCount;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be ISO 639-1 (e.g., 'en', 'uk')")
    private String language;

    @NotNull(message = "Format is required")
    private BookFormat format;

    private String shelfLocation;

    @Min(value = 0, message = "Total copies cannot be negative")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    private List<UUID> authorIds;
    private List<UUID> categoryIds;
}
