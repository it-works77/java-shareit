package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User add(User user);

    User update(User user);

    Optional<User> get(Long id);

    List<User> getAll();

    boolean remove(Long id);
}
