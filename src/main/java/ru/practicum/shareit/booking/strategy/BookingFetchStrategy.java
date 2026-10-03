package ru.practicum.shareit.booking.strategy;

import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingFetchStrategy {
    List<Booking> fetch(Long userId, LocalDateTime now);

    BookingState getState();
}
