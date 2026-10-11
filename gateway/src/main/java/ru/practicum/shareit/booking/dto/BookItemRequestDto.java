package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
    @NotNull(message = "itemId не может быть пустым")
    private Long itemId;
    @NotNull(message = "Дата начала обязательна")
    @FutureOrPresent
    private LocalDateTime start;
    @NotNull(message = "Дата окончания обязательна")
    @Future
    private LocalDateTime end;
}
