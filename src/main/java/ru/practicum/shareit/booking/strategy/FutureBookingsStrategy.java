package ru.practicum.shareit.booking.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FutureBookingsStrategy implements BookingFetchStrategy {

    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> fetch(Long userId, LocalDateTime now) {
        return bookingRepository.findFutureByBooker(userId, now);
    }

    @Override
    public BookingState getState() {
        return BookingState.FUTURE;
    }
}
