package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(User user) {
        log.info("Создание пользователя {}", user);
        userRepository.findByEmail(user.getEmail())
                .ifPresent(existing -> {
                    log.warn("Пользователь с email {} уже существует", user.getEmail());
                    throw new DuplicateDataException("Пользователь с таким email уже существует");
                });

        User newUser = userRepository.save(user);
        log.info("Создан пользователь с id: {}", newUser.getId());
        return UserMapper.toUserDto(newUser);
    }

    @Override
    @Transactional
    public UserDto update(UserUpdateDto userDto, Long userId) {
        log.info("Изменение пользователя с id {}", userId);
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        userRepository.findByEmail(userDto.getEmail())
                .ifPresent(existing -> {
                    log.warn("Пользователь с email {} уже существует", userDto.getEmail());
                    throw new DuplicateDataException("Пользователь с таким email уже существует");
                });

        UserMapper.updateUserFields(oldUser, userDto);
        User updatedUser = userRepository.save(oldUser);
        log.info("Пользователь с id: {} изменен", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public Collection<UserDto> findAll() {
        log.info("Получение всех пользователей");
        Collection<UserDto> userDtos = userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("Получен список из {} пользователей", userDtos.size());
        return userDtos;
    }

    @Override
    public UserDto findById(Long userId) {
        log.info("Получение пользователя с id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}
