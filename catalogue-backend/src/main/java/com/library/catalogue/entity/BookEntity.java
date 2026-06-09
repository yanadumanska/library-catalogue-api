package com.library.catalogue.entity;

import com.library.catalogue.enums.BookFormat;
import com.library.catalogue.enums.BookStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate publicationDate;

    private String publisher;

    private Integer pageCount;

    private String language;

    @Enumerated(EnumType.STRING)
    private BookFormat format;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private String shelfLocation;

    private Integer totalCopies;

    private Integer availableCopies;

    private BigDecimal averageRating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
