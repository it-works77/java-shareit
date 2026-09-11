package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityAlreadyExistsException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserInMemoryRepositoryImpl implements UserRepository {
    private final HashMap<Long, User> users = new HashMap<>();
    private Long currentUserId = 1L;

    @Override
    public User add(User user) {
        user.setId(getId());

        if (isEmailExists(user.getEmail())) {
            throw new EntityAlreadyExistsException("Пользователь с email=%s уже существует"
                    .formatted(user.getEmail()));
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("userId должен быть задан");
        }

        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            throw new EntityNotFoundException("Пользователь не найден по id=%s".formatted(user.getId()));
        }

        // Email has to be unique in the repository
        if (user.getEmail() != null) {
            Optional<User> existingUserWithSameEmailOpt = getUserByEmail(user.getEmail());

            if (existingUserWithSameEmailOpt.isPresent()) {
                User existingUserWithSameEmail = existingUserWithSameEmailOpt.get();

                if (!Objects.equals(existingUserWithSameEmail.getId(), user.getId())) {
                    throw new EntityAlreadyExistsException("Пользователь с email=%s уже существует"
                            .formatted(user.getEmail()));
                }
            }
        }

        Optional.ofNullable(user.getName()).ifPresent(existingUser::setName);
        Optional.ofNullable(user.getEmail()).ifPresent(existingUser::setEmail);
        return existingUser;
    }

    private Optional<User> getUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> Objects.equals(email, user.getEmail()))
                .findFirst();
    }

    @Override
    public Optional<User> get(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    @Override
    public boolean remove(Long id) {
        return users.remove(id) != null;
    }

    private Long getId() {
        return currentUserId++;
    }

    private boolean isEmailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> Objects.equals(email, user.getEmail()));
    }
}
