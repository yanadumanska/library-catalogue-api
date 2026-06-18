package com.library.catalogue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String biography;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String nationality;
    private String website;
}
