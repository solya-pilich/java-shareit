package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

interface ItemService {

    ItemDto add(Long userId, ItemDto item);

    ItemDto update(Long userId, Long itemId, ItemUpdateDto item);

    ItemWithBookingsDto findById(Long itemId, Long userId);

    Collection<ItemWithBookingsDto> findByOwnerId(Long userId);

    Collection<ItemDto> findByText(Long userId, String text);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentDto);
}
