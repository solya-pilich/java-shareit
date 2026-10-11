package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404() {
        NotFoundException e = new NotFoundException("Пользователь не найден");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Пользователь не найден");
    }

    @Test
    void handleDuplicate_shouldReturn409() {
        DuplicateDataException e = new DuplicateDataException("Email уже занят");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Email уже занят");
    }

    @Test
    void handleForbidden_shouldReturn403() {
        ForbiddenException e = new ForbiddenException("Доступ запрещён");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Доступ запрещён");
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        ValidationException e = new ValidationException("Некорректные данные");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Некорректные данные");
    }

    @Test
    void handleGeneric_shouldReturn500() {
        Exception e = new RuntimeException("Что-то пошло не так");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Что-то пошло не так");
    }
}
