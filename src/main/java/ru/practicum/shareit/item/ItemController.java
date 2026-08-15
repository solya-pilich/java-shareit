package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        log.info("Запрос на добавление вещи {} от пользователя с id {}", itemDto, userId);
        ItemDto result = itemService.add(userId, itemDto);
        log.info("Вещь {} добавлена", result);
        return result;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemUpdateDto itemDto) {
        log.info("Запрос на изменение вещи с id {} от пользователя с id {}", itemId, userId);
        ItemDto result = itemService.update(userId, itemId, itemDto);
        log.info("Вещь {} изменена", result);
        return result;
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                            @PathVariable Long itemId) {
        log.info("Запрос на получение вещи с id {} от пользователя с id {}", itemId, userId);
        ItemDto result = itemService.findById(userId, itemId);
        log.info("Получена вещь с id {}", result.getId());
        return result;
    }

    @GetMapping
    public Collection<ItemDto> findByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение всех вещей пользователя с id {}", userId);
        Collection<ItemDto> items = itemService.findByOwnerId(userId);
        log.info("Получен список из {} вещей", items.size());
        return items;
    }

    @GetMapping("/search")
    public Collection<ItemDto> findByText(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam String text) {
        log.info("Запрос на получение вещей по строчке {}", text);
        Collection<ItemDto> items = itemService.findByText(userId, text);
        log.info("Получен список из {} вещей", items.size());
        return items;
    }
}
