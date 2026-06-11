package com.library.catalogue.controller;

import com.library.catalogue.dto.ReviewRequestDto;
import com.library.catalogue.dto.ReviewResponseDto;
import com.library.catalogue.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getBookReviews(@PathVariable UUID bookId) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId));
    }

    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(
            @PathVariable UUID bookId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        // TODO: userId має братися з JWT токену (зробить Надя)
        UUID userId = UUID.randomUUID();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(bookId, userId, requestDto));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        // TODO: userId з JWT
        UUID userId = UUID.randomUUID();
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, requestDto));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
