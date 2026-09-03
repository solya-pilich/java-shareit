package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingCreateDto bookingDto) {
        log.info("Создание бронирования пользователем {} для вещи {}", userId, bookingDto.getItemId());
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь c id " + userId + " не найден");
                });
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    log.warn("Вещь с id {} не найдена", bookingDto.getItemId());
                    return new NotFoundException("Вещь c id " + bookingDto.getItemId() + " не найдена");
                });
        if (Boolean.FALSE.equals(item.getAvailable())) {
            log.warn("Вещь {} недоступна для бронирования", item.getId());
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().equals(userId)) {
            log.warn("Владелец {} пытается забронировать свою вещь {}", userId, item.getId());
            throw new ForbiddenException("Владелец не может забронировать свою же вещь");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            log.warn("Некорректные даты бронирования: start={}, end={}", bookingDto.getStart(), bookingDto.getEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование создано с id {}, статус {}", saved.getId(), saved.getStatus());
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение/отклонение бронирования {} пользователем {}, approved={}", bookingId, userId, approved);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException("Бронирование c id " + bookingId + " не найдено");
                });

        boolean isOwner = userId.equals(booking.getItem().getOwner());
        if (!isOwner) {
            log.warn("Пользователь {} не является владельцем вещи для бронирования {}", userId, bookingId);
            throw new ForbiddenException("Изменить статус бронирования может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Бронирование {} уже обработано, текущий статус {}", bookingId, booking.getStatus());
            throw new BadRequestException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования {} изменён на {}", bookingId, updatedBooking.getStatus());
        return BookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.info("Запрос бронирования {} пользователем {}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException("Бронирование c id " + bookingId + " не найдено");
                });

        boolean isBooker = userId.equals(booking.getBooker().getId());
        boolean isOwner = userId.equals(booking.getItem().getOwner());

        if (!isBooker && !isOwner) {
            log.warn("Пользователь {} не имеет доступа к бронированию {}", userId, bookingId);
            throw new ForbiddenException("Получить сведения о бронировании может только автор бронирования или владелец вещи");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public Collection<BookingResponseDto> getBookingsByBooker(Long userId, BookingState state) {
        log.info("Запрос списка бронирований для пользователя {} с состоянием {}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь c id " + userId + " не найден");
                });

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByBooker(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findPastByBooker(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByBooker(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                log.warn("Неизвестный state: {}", state);
                throw new BadRequestException("Неизвестный state: " + state);
        }

        log.info("Найдено {} бронирований для пользователя {}", bookings.size(), userId);
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state) {
        log.info("Запрос списка бронирований владельца {} с состоянием {}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь c id " + userId + " не найден");
                });

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByOwnerId(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByOwner(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findPastByOwner(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByOwner(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.REJECTED);
                break;
            default:
                log.warn("Неизвестный state: {}", state);
                throw new BadRequestException("Неизвестный state: " + state);
        }

        log.info("Найдено {} бронирований для владельца {}", bookings.size(), userId);
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}
