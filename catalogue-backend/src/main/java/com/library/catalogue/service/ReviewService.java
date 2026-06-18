package com.library.catalogue.service;

import com.library.catalogue.dto.ReviewRequestDto;
import com.library.catalogue.dto.ReviewResponseDto;
import com.library.catalogue.entity.BookEntity;
import com.library.catalogue.entity.ReviewEntity;
import com.library.catalogue.exception.ReviewNotFoundException;
import com.library.catalogue.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookService bookService;

    public List<ReviewResponseDto> getReviewsByBookId(UUID bookId) {
        return reviewRepository.findByBookId(bookId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional
    public ReviewResponseDto addReview(UUID bookId, UUID userId, ReviewRequestDto requestDto) {
        BookEntity book = bookService.getBookEntityById(bookId);

        ReviewEntity review = ReviewEntity.builder()
                .book(book)
                .userId(userId)
                .rating(requestDto.getRating())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .spoilerFlag(requestDto.getSpoilerFlag() != null ? requestDto.getSpoilerFlag() : false)
                .build();

        ReviewEntity saved = reviewRepository.save(review);

        updateBookAverageRating(bookId);

        log.info("Added review for book: {} by user: {}", book.getTitle(), userId);
        return mapToResponseDto(saved);
    }

    @Transactional
    public ReviewResponseDto updateReview(UUID reviewId, UUID userId, ReviewRequestDto requestDto) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.setRating(requestDto.getRating());
        review.setTitle(requestDto.getTitle());
        review.setContent(requestDto.getContent());
        if (requestDto.getSpoilerFlag() != null) {
            review.setSpoilerFlag(requestDto.getSpoilerFlag());
        }

        ReviewEntity updated = reviewRepository.save(review);

        updateBookAverageRating(review.getBook().getId());

        log.info("Updated review: {} by user: {}", reviewId, userId);
        return mapToResponseDto(updated);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        UUID bookId = review.getBook().getId();
        reviewRepository.delete(review);

        updateBookAverageRating(bookId);

        log.info("Deleted review: {}", reviewId);
    }

    private void updateBookAverageRating(UUID bookId) {
        BigDecimal avg = reviewRepository.calculateAverageRating(bookId);
        if (avg == null) avg = BigDecimal.ZERO;
        avg = avg.setScale(1, RoundingMode.HALF_UP);

        BookEntity book = bookService.getBookEntityById(bookId);
        book.setAverageRating(avg);
    }

    private ReviewResponseDto mapToResponseDto(ReviewEntity review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .bookId(review.getBook().getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .spoilerFlag(review.getSpoilerFlag())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
