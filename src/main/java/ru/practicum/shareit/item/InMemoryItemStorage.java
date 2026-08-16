package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item add(Item item) {
        if (item.getId() == null) {
            item.setId(getNextId());
        }
        items.put(item.getId(), item);
        log.debug("Вещь сохранена в хранилище с id {}", item.getId());
        return item;
    }

    @Override
    public Item update(Item item) {
        throwIfNotFound(item.getId());
        items.put(item.getId(), item);
        log.debug("Вещь {} изменена в хранилище", item);
        return item;
    }

    @Override
    public Item findById(Long itemId) {
        throwIfNotFound(itemId);
        Item item = items.get(itemId);
        return item;
    }

    @Override
    public Collection<Item> findByOwnerId(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> findByText(String text) {
        if (!hasText(text)) {
            log.info("Строка \"{}\" не содержит текст", text);
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(lowerText)
                        || item.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

    private void throwIfNotFound(Long itemId) {
        if (!items.containsKey(itemId)) {
            log.warn("В хранилище не найдена вещь с id {}", itemId);
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
    }

    private Long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Получен ID {}", newId);
        return newId;
    }
}
