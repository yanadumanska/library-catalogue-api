package com.library.catalogue.service;

import com.library.catalogue.dto.BorrowingRequestDto;
import com.library.catalogue.dto.BorrowingResponseDto;
import com.library.catalogue.entity.BookEntity;
import com.library.catalogue.entity.BorrowingEntity;
import com.library.catalogue.entity.UserEntity;
import com.library.catalogue.enums.BookStatus;
import com.library.catalogue.exception.BookNotFoundException;
import com.library.catalogue.repository.BookRepository;
import com.library.catalogue.repository.BorrowingRepository;
import com.library.catalogue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public List<BorrowingResponseDto> getUserBorrowings(UUID userId) {
        return borrowingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<BorrowingResponseDto> getUserBorrowingsByStatus(UUID userId, String status) {
        return borrowingRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public BorrowingResponseDto borrowBook(UUID userId, BorrowingRequestDto request) {
        BookEntity book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book is not available: " + book.getTitle());
        }
        boolean alreadyBorrowed = borrowingRepository
                .findByBookIdAndUserIdAndStatus(request.getBookId(), userId, "ACTIVE")
                .isPresent();

        if (alreadyBorrowed) {
            throw new RuntimeException("Ви вже взяли цю книгу");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long activeBorrows = borrowingRepository.countByUserIdAndStatus(userId, "ACTIVE");
        if (user.getMaxBorrowLimit() != null && activeBorrows >= user.getMaxBorrowLimit()) {
            throw new RuntimeException("Borrow limit reached. Max: " + user.getMaxBorrowLimit());
        }

        int days = request.getExpectedDurationDays() != null ? request.getExpectedDurationDays() : 14;

        BorrowingEntity borrowing = BorrowingEntity.builder()
                .book(book)
                .user(user)
                .borrowDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(days))
                .status("ACTIVE")
                .renewalCount(0)
                .fineAmount(0.0)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (book.getAvailableCopies() == 0) {
            book.setStatus(BookStatus.BORROWED);
        }
        bookRepository.save(book);

        BorrowingEntity saved = borrowingRepository.save(borrowing);
        log.info("Book borrowed: {} by user {}", book.getTitle(), userId);
        return mapToDto(saved);
    }

    @Transactional
    public BorrowingResponseDto returnBook(UUID borrowingId) {
        BorrowingEntity borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        if (!"ACTIVE".equals(borrowing.getStatus())) {
            throw new RuntimeException("Borrowing is not active");
        }

        borrowing.setStatus("RETURNED");
        borrowing.setReturnDate(LocalDateTime.now());

        if (borrowing.getReturnDate().isAfter(borrowing.getDueDate())) {
            long daysLate = borrowing.getReturnDate().toLocalDate()
                    .toEpochDay() - borrowing.getDueDate().toLocalDate().toEpochDay();
            borrowing.setFineAmount(daysLate * 5.0); // 5 грн за день
        }

        BookEntity book = borrowing.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        BorrowingEntity saved = borrowingRepository.save(borrowing);
        log.info("Book returned: {} by user {}", book.getTitle(), borrowing.getUser().getId());
        return mapToDto(saved);
    }

    @Transactional
    public BorrowingResponseDto renewBorrowing(UUID borrowingId) {
        BorrowingEntity borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        if (!"ACTIVE".equals(borrowing.getStatus())) {
            throw new RuntimeException("Only active borrowings can be renewed");
        }

        if (borrowing.getRenewalCount() != null && borrowing.getRenewalCount() >= 2) {
            throw new RuntimeException("Maximum renewals reached (2)");
        }

        borrowing.setDueDate(borrowing.getDueDate().plusDays(14));
        borrowing.setRenewalCount(borrowing.getRenewalCount() + 1);

        BorrowingEntity saved = borrowingRepository.save(borrowing);
        log.info("Borrowing renewed: {}", borrowingId);
        return mapToDto(saved);
    }

    private BorrowingResponseDto mapToDto(BorrowingEntity b) {
        return BorrowingResponseDto.builder()
                .id(b.getId())
                .bookId(b.getBook().getId())
                .bookTitle(b.getBook().getTitle())
                .userId(b.getUser().getId())
                .userName(b.getUser().getFullName())
                .borrowDate(b.getBorrowDate())
                .dueDate(b.getDueDate())
                .returnDate(b.getReturnDate())
                .status(b.getStatus())
                .renewalCount(b.getRenewalCount())
                .fineAmount(b.getFineAmount())
                .build();
    }
}
