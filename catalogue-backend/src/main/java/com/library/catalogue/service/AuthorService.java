package com.library.catalogue.service;

import com.library.catalogue.dto.AuthorRequestDto;
import com.library.catalogue.dto.AuthorResponseDto;
import com.library.catalogue.entity.AuthorEntity;
import com.library.catalogue.exception.AuthorNotFoundException;
import com.library.catalogue.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Cacheable(value = "authors", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<AuthorResponseDto> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    @Cacheable(value = "authors", key = "'search-' + #search + '-' + #pageable.pageNumber")
    public Page<AuthorResponseDto> searchAuthors(String search, Pageable pageable) {
        return authorRepository.searchByName(search, pageable)
                .map(this::mapToResponseDto);
    }

    @Cacheable(value = "author", key = "#id")
    public AuthorResponseDto getAuthorById(UUID id) {
        return authorRepository.findById(id)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    public AuthorEntity getAuthorEntityById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @Transactional
    @CacheEvict(value = {"authors", "author"}, allEntries = true)
    public AuthorResponseDto createAuthor(AuthorRequestDto requestDto) {
        AuthorEntity author = AuthorEntity.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .biography(requestDto.getBiography())
                .birthDate(requestDto.getBirthDate())
                .deathDate(requestDto.getDeathDate())
                .nationality(requestDto.getNationality())
                .website(requestDto.getWebsite())
                .build();

        AuthorEntity saved = authorRepository.save(author);
        log.info("Created author: {} {}", saved.getFirstName(), saved.getLastName());
        return mapToResponseDto(saved);
    }

    @Transactional
    @CacheEvict(value = {"authors", "author"}, allEntries = true)
    public AuthorResponseDto updateAuthor(UUID id, AuthorRequestDto requestDto) {
        AuthorEntity existing = getAuthorEntityById(id);

        existing.setFirstName(requestDto.getFirstName());
        existing.setLastName(requestDto.getLastName());
        existing.setBiography(requestDto.getBiography());
        existing.setBirthDate(requestDto.getBirthDate());
        existing.setDeathDate(requestDto.getDeathDate());
        existing.setNationality(requestDto.getNationality());
        existing.setWebsite(requestDto.getWebsite());

        AuthorEntity updated = authorRepository.save(existing);
        log.info("Updated author: {} {}", updated.getFirstName(), updated.getLastName());
        return mapToResponseDto(updated);
    }

    @Transactional
    @CacheEvict(value = {"authors", "author"}, allEntries = true)
    public void deleteAuthor(UUID id) {
        AuthorEntity author = getAuthorEntityById(id);
        authorRepository.delete(author);
        log.info("Deleted author: {} {}", author.getFirstName(), author.getLastName());
    }

    private AuthorResponseDto mapToResponseDto(AuthorEntity author) {
        return AuthorResponseDto.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .biography(author.getBiography())
                .birthDate(author.getBirthDate())
                .deathDate(author.getDeathDate())
                .nationality(author.getNationality())
                .website(author.getWebsite())
                .build();
    }
}
