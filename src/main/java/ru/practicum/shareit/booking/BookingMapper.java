package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static Booking toBooking(BookingCreateDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        Item item = booking.getItem();
        ItemShortDto itemShortDto = item != null ? new ItemShortDto(item.getId(), item.getName()) : null;

        User booker = booking.getBooker();
        UserShortDto userShortDto = booker != null ? new UserShortDto(booker.getId(), booker.getName()) : null;

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                userShortDto,
                itemShortDto
        );
    }
}

