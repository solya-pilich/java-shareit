package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

interface UserService {
    UserDto create(User user);

    UserDto update(UserUpdateDto user, Long userId);

    Collection<UserDto> findAll();

    UserDto findById(Long userId);

    void delete(Long userId);
}
