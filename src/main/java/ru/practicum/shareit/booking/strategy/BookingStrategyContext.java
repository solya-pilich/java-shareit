package ru.practicum.shareit.booking.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BookingStrategyContext {

    private final Map<BookingState, BookingFetchStrategy> strategyMap = new HashMap<>();

    public BookingStrategyContext(List<BookingFetchStrategy> strategies) {
        for (BookingFetchStrategy strategy : strategies) {
            strategyMap.put(strategy.getState(), strategy);
        }
        log.info("Загружено {} стратегий для бронирований", strategyMap.size());
    }

    public List<Booking> execute(BookingState state, Long userId, LocalDateTime now) {
        BookingFetchStrategy strategy = strategyMap.get(state);
        if (strategy == null) {
            log.warn("Неизвестный state: {}", state);
            throw new ValidationException("Неизвестный state: " + state);
        }
        return strategy.fetch(userId, now);
    }
}
