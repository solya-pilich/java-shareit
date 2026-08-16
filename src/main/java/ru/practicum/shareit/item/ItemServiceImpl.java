package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    public ItemDto add(Long userId, ItemDto itemDto) {
        log.info("Добавление вещи для пользователя {}", userId);
        User user = userStorage.findById(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        Item saved = itemStorage.add(item);
        log.info("Вещь {} добавлена (id={})", saved.getName(), saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto) {
        log.info("Изменение вещи пользователя {}", userId);
        User user = userStorage.findById(userId);
        Item oldItem = itemStorage.findById(itemId);
        if (!oldItem.getOwner().equals(user.getId())) {
            log.warn("Пользователь с id {} не может редактировать вещь {}", user.getId(), itemId);
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        ItemMapper.updateItemFields(oldItem, itemDto);
        Item updated = itemStorage.update(oldItem);
        log.info("Вещь {} изменена (id={})", updated.getName(), updated.getId());

        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto findById(Long itemId) {
        log.info("Поиск вещи {}", itemId);
        Item item = itemStorage.findById(itemId);
        log.info("Найдена вещь {}", item.getName());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> findByOwnerId(Long userId) {
        log.info("Поиск вещей пользователя {}", userId);
        User user = userStorage.findById(userId);
        Collection<Item> items = itemStorage.findByOwnerId(user.getId());
        log.info("Найдено вещей - {}", items.size());
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> findByText(Long userId, String text) {
        log.info("Поиск вещей по строке {}", text);
        User user = userStorage.findById(userId);
        Collection<Item> items = itemStorage.findByText(text);
        log.info("Пользователь {} запросил поиск, найдено вещей - {}",
                user.getName(), items.size());
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
