package ru.practicum.shareit.booking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookItemRequestDtoJsonTest {

    private final JacksonTester<BookItemRequestDto> json;
    private final Validator validator;

    @Test
    void serialize_shouldWriteAllFields() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 2, 10, 0);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        JsonContent<BookItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-01-02T10:00:00");
    }

    @Test
    void deserialize_shouldReadAllFields() throws Exception {
        String jsonString = "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}";

        BookItemRequestDto dto = json.parseObject(jsonString);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 1, 2, 10, 0));
    }

    @Test
    void validate_shouldPass_whenAllFieldsValid() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFail_whenItemIdIsNull() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto dto = new BookItemRequestDto(null, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("itemId");
    }

    @Test
    void validate_shouldFail_whenStartIsNull() {
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto dto = new BookItemRequestDto(1L, null, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("start");
    }

    @Test
    void validate_shouldFail_whenEndIsNull() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, null);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("end");
    }

    @Test
    void validate_shouldFail_whenStartInPast() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("start");
    }

    @Test
    void validate_shouldFail_whenEndInPast() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}
