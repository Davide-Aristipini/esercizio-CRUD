package com.colloquio.usermanagement.service;

import com.colloquio.usermanagement.dto.CsvImportError;
import com.colloquio.usermanagement.dto.CsvImportResponse;
import com.colloquio.usermanagement.dto.UserRequest;
import com.colloquio.usermanagement.dto.UserResponse;
import com.colloquio.usermanagement.entity.User;
import com.colloquio.usermanagement.exception.DuplicateEmailException;
import com.colloquio.usermanagement.exception.InvalidCsvFileException;
import com.colloquio.usermanagement.exception.ResourceNotFoundException;
import com.colloquio.usermanagement.mapper.UserMapper;
import com.colloquio.usermanagement.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private static final String HEADER_NOME = "nome";
    private static final String HEADER_COGNOME = "cognome";
    private static final String HEADER_EMAIL = "email";
    private static final String HEADER_INDIRIZZO = "indirizzo";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "id");

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Validator validator;

    public UserService(UserRepository userRepository, UserMapper userMapper, Validator validator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.validator = validator;
    }

    public UserResponse createUser(UserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Esiste gia' un utente con email " + normalizedEmail);
        }

        User user = userMapper.toEntity(request);
        return userMapper.toResponse(saveUser(user));
    }

    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll(DEFAULT_SORT)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<UserResponse> searchUsers(String nome, String cognome) {
        Specification<User> specification = Specification.where(null);

        if (StringUtils.hasText(nome)) {
            String normalizedNome = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), normalizedNome));
        }

        if (StringUtils.hasText(cognome)) {
            String normalizedCognome = "%" + cognome.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("cognome")), normalizedCognome));
        }

        return userRepository.findAll(specification, DEFAULT_SORT)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User existingUser = findUserById(id);
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateEmailException("Esiste gia' un utente con email " + normalizedEmail);
        }

        userMapper.updateEntity(existingUser, request);
        return userMapper.toResponse(saveUser(existingUser));
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    public CsvImportResponse importUsersFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvFileException("Il file CSV e' obbligatorio e non puo' essere vuoto");
        }

        List<CsvImportError> discardedRows = new ArrayList<>();
        int importedCount = 0;
        Set<String> processedEmails = new LinkedHashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            validateHeaders(parser);

            for (CSVRecord record : parser) {
                long rowNumber = record.getRecordNumber() + 1;
                UserRequest request = new UserRequest(
                        getValue(record, HEADER_NOME),
                        getValue(record, HEADER_COGNOME),
                        getValue(record, HEADER_EMAIL),
                        getValue(record, HEADER_INDIRIZZO)
                );

                List<String> validationErrors = validateCsvRow(request);
                String normalizedEmail = normalizeEmail(request.email());

                if (!validationErrors.isEmpty()) {
                    discardedRows.add(new CsvImportError(rowNumber, String.join("; ", validationErrors)));
                    continue;
                }

                if (processedEmails.contains(normalizedEmail) || userRepository.existsByEmail(normalizedEmail)) {
                    discardedRows.add(new CsvImportError(rowNumber, "Email duplicata: " + normalizedEmail));
                    continue;
                }

                User user = userMapper.toEntity(request);

                try {
                    saveUser(user);
                    processedEmails.add(normalizedEmail);
                    importedCount++;
                } catch (DuplicateEmailException exception) {
                    discardedRows.add(new CsvImportError(rowNumber, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            throw new InvalidCsvFileException("Errore durante la lettura del file CSV", exception);
        }

        return new CsvImportResponse(importedCount, discardedRows.size(), discardedRows);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente con id " + id + " non trovato"));
    }

    private User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException("Esiste gia' un utente con email " + user.getEmail());
        }
    }

    private List<String> validateCsvRow(UserRequest request) {
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
    }

    private void validateHeaders(CSVParser parser) {
        Set<String> headers = parser.getHeaderMap().keySet()
                .stream()
                .map(header -> header.toLowerCase(Locale.ROOT).trim())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<String> requiredHeaders = List.of(HEADER_NOME, HEADER_COGNOME, HEADER_EMAIL, HEADER_INDIRIZZO);
        boolean missingHeaders = requiredHeaders.stream().anyMatch(header -> !headers.contains(header));

        if (missingHeaders) {
            throw new InvalidCsvFileException(
                    "Header CSV non valido. Colonne richieste: nome,cognome,email,indirizzo");
        }
    }

    private String getValue(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return "";
        }
        return record.get(column);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
