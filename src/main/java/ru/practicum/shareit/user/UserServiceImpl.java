package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.exception.DuplicateDataException;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto create(User user) {
        log.info("Создание пользователя {}", user);
        userStorage.findByEmail(user.getEmail())
                .ifPresent(existing -> {
                    log.warn("Пользователь с email {} уже существует", user.getEmail());
                    throw new DuplicateDataException("Пользователь с таким email уже существует");
                });

        User newUser = userStorage.create(user);
        log.info("Создан пользователь с id: {}", newUser.getId());
        return UserMapper.toUserDto(newUser);
    }

    @Override
    public UserDto update(UserUpdateDto userDto, Long userId) {
        log.info("Изменение пользователя с id {}", userId);
        User oldUser = userStorage.findById(userId);
        userStorage.findByEmail(userDto.getEmail())
                .ifPresent(existing -> {
                    log.warn("Пользователь с email {} уже существует", userDto.getEmail());
                    throw new DuplicateDataException("Пользователь с таким email уже существует");
                });

        UserMapper.updateUserFields(oldUser, userDto);
        User updatedUser = userStorage.update(oldUser);
        log.info("Пользователь с id: {} изменен", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public Collection<UserDto> findAll() {
        log.info("Получение всех пользователей");
        Collection<UserDto> userDtos = userStorage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("Получен список из {} пользователей", userDtos.size());
        return userDtos;
    }

    @Override
    public UserDto findById(Long userId) {
        log.info("Получение пользователя с id: {}", userId);
        return UserMapper.toUserDto(userStorage.findById(userId));
    }

    @Override
    public void delete(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);
        userStorage.delete(userId);
    }
}
