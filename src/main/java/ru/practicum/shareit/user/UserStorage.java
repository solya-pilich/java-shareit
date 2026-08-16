package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Collection<User> findAll();

    User findById(Long userId);

    void delete(Long userId);

    Optional<User> findByEmail(String email);
}
