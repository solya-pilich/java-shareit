package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestDataFactory.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {

    private final BookingService bookingService;
    private final TestDataFactory factory;

    @Test
    void create_shouldSaveBooking() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto dto = new BookingCreateDto(item.getId(), start, end);

        BookingResponseDto created = bookingService.create(booker.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrow_whenItemNotFound() {
        User booker = factory.createUser("Booker", "booker@mail.com");

        BookingCreateDto dto = new BookingCreateDto(
                999L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrow_whenItemNotAvailable() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", false, owner.getId());

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrow_whenOwnerBooksOwnItem() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), dto))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void create_shouldThrow_whenStartAfterEnd() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        BookingCreateDto dto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrow_whenStartEqualsEnd() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime same = LocalDateTime.now().plusDays(1);
        BookingCreateDto dto = new BookingCreateDto(item.getId(), same, same);

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_shouldSetApproved() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Booking booking = factory.createBooking(item, booker,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        BookingResponseDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_shouldSetRejected() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Booking booking = factory.createBooking(item, booker,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        BookingResponseDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_shouldThrow_whenBookingNotFound() {
        User owner = factory.createUser("Owner", "owner@mail.com");

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), 999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approve_shouldThrow_whenNotOwner() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        User other = factory.createUser("Other", "other@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Booking booking = factory.createBooking(item, booker,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertThatThrownBy(() -> bookingService.approve(other.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_shouldThrow_whenAlreadyProcessed() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Booking booking = factory.createBooking(item, booker,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), true))
                .isInstanceOf(ValidationException.class);
    }



    @Test
    void getBooking_shouldReturnForBooker() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Booking booking = factory.createBooking(item, booker,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        BookingResponseDto dto = bookingService.getBooking(booker.getId(), booking.getId());

        assertThat(dto.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBooking_shouldReturnForOwner() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Booking booking = factory.createBooking(item, booker,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        BookingResponseDto dto = bookingService.getBooking(owner.getId(), booking.getId());

        assertThat(dto.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBooking_shouldThrow_whenBookingNotFound() {
        User user = factory.createUser("User", "user@mail.com");

        assertThatThrownBy(() -> bookingService.getBooking(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }




    @Test
    void getBookingsByBooker_shouldReturnAll() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByBooker_shouldReturnCurrent() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.CURRENT);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnPast() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(5), now.minusDays(3), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.PAST);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnFuture() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(5), now.minusDays(3), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.FUTURE);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnWaiting() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByBooker_shouldReturnRejected() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.REJECTED);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBookingsByBooker_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingsByBooker(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void getBookingsByOwner_shouldReturnAll() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker1 = factory.createUser("Booker1", "b1@mail.com");
        User booker2 = factory.createUser("Booker2", "b2@mail.com");
        Item item1 = factory.createItem("Дрель", "Мощная", true, owner.getId());
        Item item2 = factory.createItem("Пила", "Острая", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item1, booker1, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);
        factory.createBooking(item2, booker2, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.ALL);

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByOwner_shouldReturnCurrent() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.CURRENT);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnPast() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(5), now.minusDays(3), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.PAST);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnFuture() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(5), now.minusDays(3), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.FUTURE);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnWaiting() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByOwner_shouldReturnRejected() {
        User owner = factory.createUser("Owner", "owner@mail.com");
        User booker = factory.createUser("Booker", "booker@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.REJECTED);
        factory.createBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBookingsByOwner_shouldReturnEmpty_whenNoItems() {
        User owner = factory.createUser("Owner", "owner@mail.com");

        Collection<BookingResponseDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.ALL);

        assertThat(result).isEmpty();
    }

    @Test
    void getBookingsByOwner_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingsByOwner(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }
}
