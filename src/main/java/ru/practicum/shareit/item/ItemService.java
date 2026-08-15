package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

interface ItemService {

    ItemDto add(Long userId, ItemDto item);

    ItemDto update(Long userId, Long itemId, ItemUpdateDto item);

    ItemDto findById(Long userId, Long itemId);

    Collection<ItemDto> findByOwnerId(Long userId);

    Collection<ItemDto> findByText(Long userId, String text);

}
