package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь сохранен в хранилище с id {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        throwIfNotFound(user.getId());
        users.put(user.getId(), user);
        log.debug("Пользователь {} изменен в хранилище", user);
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User findById(Long userId) {
        return Optional.ofNullable(users.get(userId))
                .orElseThrow(() -> {
                    log.warn("В хранилище не найден пользователь с id {}", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
    }

    @Override
    public void delete(Long userId) {
        throwIfNotFound(userId);
        users.remove(userId);
        log.debug("В хранилище удален пользователь id {}", userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    private void throwIfNotFound(Long userId) {
        if (!users.containsKey(userId)) {
            log.warn("В хранилище не найден пользователь с id {}", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Получен ID {}", newId);
        return newId;
    }
}
