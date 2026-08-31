package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {

    Item add(Item item);

    Item update(Item item);

    Item findById(Long itemId);

    Collection<Item> findByOwnerId(Long userId);

    Collection<Item> findByText(String text);

}
