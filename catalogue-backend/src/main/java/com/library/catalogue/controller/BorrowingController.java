package com.library.catalogue.controller;

import com.library.catalogue.dto.BorrowingRequestDto;
import com.library.catalogue.dto.BorrowingResponseDto;
import com.library.catalogue.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping
    public ResponseEntity<List<BorrowingResponseDto>> getUserBorrowings(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        UUID userId = UUID.fromString(authentication.getName());
        if (status != null) {
            return ResponseEntity.ok(borrowingService.getUserBorrowingsByStatus(userId, status));
        }
        return ResponseEntity.ok(borrowingService.getUserBorrowings(userId));
    }

    @PostMapping
    public ResponseEntity<BorrowingResponseDto> borrowBook(
            Authentication authentication,
            @Valid @RequestBody BorrowingRequestDto request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowingService.borrowBook(userId, request));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowingResponseDto> returnBook(@PathVariable UUID id) {
        return ResponseEntity.ok(borrowingService.returnBook(id));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<BorrowingResponseDto> renewBorrowing(@PathVariable UUID id) {
        return ResponseEntity.ok(borrowingService.renewBorrowing(id));
    }
}
