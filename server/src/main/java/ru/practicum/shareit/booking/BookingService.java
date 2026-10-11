package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Collection;

interface BookingService {

    BookingResponseDto create(Long userId, BookingCreateDto bookingDto);

    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    Collection<BookingResponseDto> getBookingsByBooker(Long userId, BookingState state);

    Collection<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state);
}
