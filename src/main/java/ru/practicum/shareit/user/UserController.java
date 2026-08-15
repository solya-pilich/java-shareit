package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user);
        UserDto userDto = userService.create(user);
        log.info("Создан пользователь с id: {}", userDto.getId());
        return userDto;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@Valid @RequestBody UserUpdateDto user,
                          @PathVariable Long userId) {
        log.info("Запрос на изменение пользователя с id: {}", userId);
        UserDto userDto = userService.update(user, userId);
        log.info("Пользователь с id: {} изменен", userDto.getId());
        return userDto;
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Запрос на получение всех пользователей");
        Collection<UserDto> users = userService.findAll();
        log.info("Получен список из {} пользователей", users.size());
        return users;
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        log.info("Запрос на получение пользователя с id: {}", userId);
        UserDto userDto = userService.findById(userId);
        log.info("Получен пользователь с id: {}", userId);
        return userDto;
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("Запрос на удаление пользователя с id: {}", userId);
        userService.delete(userId);
        log.info("Пользователь с id: {} удален", userId);
    }
}