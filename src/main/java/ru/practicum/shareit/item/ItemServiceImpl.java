package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto add(Long userId, ItemDto itemDto) {
        log.info("Добавление вещи для пользователя {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item item = ItemMapper.toItem(itemDto, user);
        Item saved = itemRepository.save(item);
        log.info("Вещь {} добавлена (id={})", saved.getName(), saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto) {
        log.info("Изменение вещи id={} пользователя {}", itemId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (!oldItem.getOwner().equals(user.getId())) {
            log.warn("Пользователь с id {} не может редактировать вещь {}", user.getId(), itemId);
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        ItemMapper.updateItemFields(oldItem, itemDto);
        Item updated = itemRepository.save(oldItem);
        log.info("Вещь {} изменена (id={})", updated.getName(), updated.getId());

        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemWithBookingsDto findById(Long itemId, Long userId) {
        log.info("Поиск вещи {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id {} не найдена", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });
        log.info("Найдена вещь {}", item.getName());

        ItemWithBookingsDto itemDto = toItemWithBookingsDto(item, userId);
        return itemDto;
    }

    @Override
    public Collection<ItemWithBookingsDto> findByOwnerId(Long userId) {
        log.info("Поиск вещей пользователя {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        List<Item> items = itemRepository.findByOwner(user.getId());
        log.info("Найдено вещей - {}", items.size());
        return items.stream()
                .map(item -> toItemWithBookingsDto(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> findByText(Long userId, String text) {
        log.info("Поиск вещей по строке {}", text);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (!hasText(text)) {
            log.info("Строка \"{}\" не содержит текст", text);
            return List.of();
        }
        List<Item> items = itemRepository.searchByText(text);
        log.info("Пользователь {} запросил поиск, найдено вещей - {}",
                user.getName(), items.size());
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentDto) {
        log.info("Добавление комментария от пользователя {} к вещи {}", userId, itemId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id {} не найдена", itemId);
                    return new NotFoundException("Вещь с id " + itemId + " не найдена");
                });

        boolean hasBooked = bookingRepository.existsApprovedBookingByBookerAndItemAndEndBefore(
                userId, itemId, LocalDateTime.now());
        if (!hasBooked) {
            log.warn("Пользователь {} не арендовал вещь {} или срок не истёк", userId, itemId);
            throw new ValidationException("Пользователь не арендовал эту вещь или срок аренды ещё не истёк");
        }

        Comment comment = CommentMapper.toComment(commentDto, user, item);
        commentRepository.save(comment);
        log.info("Комментарий к вещи {} сохранён с id {}", itemId, comment.getId());
        return CommentMapper.toCommentDto(comment);
    }

    private ItemWithBookingsDto toItemWithBookingsDto(Item item, Long userId) {
        log.debug("Формирование ItemWithBookingsDto для вещи {}, пользователь {}", item.getId(), userId);
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        if (item.getOwner().equals(userId)) {
            log.debug("Пользователь является владельцем, загружаем даты бронирований");
            LocalDateTime now = LocalDateTime.now();

            bookingRepository.findLastApprovedByItem(item.getId(), now)
                    .ifPresent(booking -> {
                        dto.setLastBooking(new BookingShortDto(
                                booking.getId(),
                                booking.getBooker().getId(),
                                booking.getStart(),
                                booking.getEnd()
                        ));
                    });

            bookingRepository.findNextApprovedByItem(item.getId(), now)
                    .ifPresent(booking -> {
                        dto.setNextBooking(new BookingShortDto(
                                booking.getId(),
                                booking.getBooker().getId(),
                                booking.getStart(),
                                booking.getEnd()
                        ));
                    });
        }

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        dto.setComments(commentDtos);

        return dto;
    }
}
