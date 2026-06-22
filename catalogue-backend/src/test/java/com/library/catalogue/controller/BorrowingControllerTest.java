package com.library.catalogue.controller;

import com.library.catalogue.dto.BorrowingRequestDto;
import com.library.catalogue.dto.BorrowingResponseDto;
import com.library.catalogue.service.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingControllerTest {

    @Mock
    private BorrowingService borrowingService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BorrowingController borrowingController;

    private UUID userId;
    private UUID borrowingId;
    private UUID bookId;
    private BorrowingResponseDto borrowingDto;
    private BorrowingRequestDto borrowingRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        borrowingId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        borrowingDto = BorrowingResponseDto.builder()
                .id(borrowingId)
                .bookId(bookId)
                .bookTitle("Clean Code")
                .userId(userId)
                .userName("Test User")
                .borrowDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .status("ACTIVE")
                .renewalCount(0)
                .fineAmount(0.0)
                .build();

        borrowingRequest = BorrowingRequestDto.builder()
                .bookId(bookId)
                .expectedDurationDays(14)
                .build();
    }

    private void mockAuthentication() {
        when(authentication.getName()).thenReturn(userId.toString());
    }

    @Test
    void getUserBorrowings_ShouldReturnList() {
        mockAuthentication();
        when(borrowingService.getUserBorrowings(userId)).thenReturn(List.of(borrowingDto));

        ResponseEntity<List<BorrowingResponseDto>> response = borrowingController.getUserBorrowings(authentication, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getUserBorrowings_WithStatus_ShouldFilterByStatus() {
        mockAuthentication();
        when(borrowingService.getUserBorrowingsByStatus(userId, "ACTIVE")).thenReturn(List.of(borrowingDto));

        ResponseEntity<List<BorrowingResponseDto>> response = borrowingController.getUserBorrowings(authentication, "ACTIVE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(borrowingService).getUserBorrowingsByStatus(userId, "ACTIVE");
    }

    @Test
    void borrowBook_ShouldReturnCreated() {
        mockAuthentication();
        when(borrowingService.borrowBook(eq(userId), any())).thenReturn(borrowingDto);

        ResponseEntity<BorrowingResponseDto> response = borrowingController.borrowBook(authentication, borrowingRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ACTIVE", response.getBody().getStatus());
    }

    @Test
    void returnBook_ShouldReturnOk() {
        BorrowingResponseDto returnedDto = BorrowingResponseDto.builder()
                .id(borrowingId)
                .status("RETURNED")
                .returnDate(LocalDateTime.now())
                .build();
        when(borrowingService.returnBook(borrowingId)).thenReturn(returnedDto);

        ResponseEntity<BorrowingResponseDto> response = borrowingController.returnBook(borrowingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("RETURNED", response.getBody().getStatus());
    }

    @Test
    void renewBorrowing_ShouldReturnOk() {
        BorrowingResponseDto renewedDto = BorrowingResponseDto.builder()
                .id(borrowingId)
                .status("ACTIVE")
                .renewalCount(1)
                .dueDate(LocalDateTime.now().plusDays(28))
                .build();
        when(borrowingService.renewBorrowing(borrowingId)).thenReturn(renewedDto);

        ResponseEntity<BorrowingResponseDto> response = borrowingController.renewBorrowing(borrowingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getRenewalCount());
    }
}
