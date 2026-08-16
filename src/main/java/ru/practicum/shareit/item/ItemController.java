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
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemUpdateDto itemDto) {
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                            @PathVariable Long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> findByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> findByText(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam String text) {
        return itemService.findByText(userId, text);
    }
}
