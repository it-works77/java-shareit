package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.EntityAlreadyExistsException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserInMemoryRepositoryImplTest {

    private UserInMemoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserInMemoryRepositoryImpl();
    }

    @Test
    void add_whenNewUser_returnsUserWithGeneratedId() {
        User user = User.builder().name("John").email("john@example.com").build();

        User saved = repository.add(user);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("John", saved.getName());
        assertEquals("john@example.com", saved.getEmail());
    }

    @Test
    void add_whenEmailAlreadyExists_throwsEntityAlreadyExistsException() {
        User user1 = User.builder().name("John").email("john@example.com").build();
        repository.add(user1);
        User user2 = User.builder().name("Jane").email("john@example.com").build();

        assertThrows(EntityAlreadyExistsException.class, () -> repository.add(user2));
    }

    @Test
    void add_multipleUsers_incrementsId() {
        User user1 = User.builder().name("John").email("john@example.com").build();
        User user2 = User.builder().name("Jane").email("jane@example.com").build();
        User user3 = User.builder().name("Bob").email("bob@example.com").build();

        repository.add(user1);
        repository.add(user2);
        User saved3 = repository.add(user3);

        assertEquals(3L, saved3.getId());
    }

    @Test
    void update_whenIdIsNull_throwsIllegalArgumentException() {
        User user = User.builder().id(null).name("John").email("john@example.com").build();

        assertThrows(IllegalArgumentException.class, () -> repository.update(user));
    }

    @Test
    void update_whenUserNotFound_throwsEntityNotFoundException() {
        User user = User.builder().id(999L).name("John").email("john@example.com").build();

        assertThrows(EntityNotFoundException.class, () -> repository.update(user));
    }

    @Test
    void update_whenFound_updatesOnlyProvidedFields() {
        User user = User.builder().name("John").email("john@example.com").build();
        User saved = repository.add(user);

        User update = User.builder().id(saved.getId()).name("Jane").build();
        User updated = repository.update(update);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("Jane", updated.getName());
        assertEquals("john@example.com", updated.getEmail());
    }

    @Test
    void update_whenEmailChangedToExisting_throwsEntityAlreadyExistsException() {
        User user1 = User.builder().name("John").email("john@example.com").build();
        User user2 = User.builder().name("Jane").email("jane@example.com").build();
        repository.add(user1);
        User saved2 = repository.add(user2);

        User update = User.builder().id(saved2.getId()).email("john@example.com").build();

        assertThrows(EntityAlreadyExistsException.class, () -> repository.update(update));
    }

    @Test
    void update_whenEmailChangedToOwnCurrent_passes() {
        User user = User.builder().name("John").email("john@example.com").build();
        User saved = repository.add(user);

        User update = User.builder().id(saved.getId()).email("john@example.com").name("Jane").build();
        User updated = repository.update(update);

        assertEquals("Jane", updated.getName());
        assertEquals("john@example.com", updated.getEmail());
    }

    @Test
    void get_whenExists_returnsUser() {
        User user = User.builder().name("John").email("john@example.com").build();
        User saved = repository.add(user);

        Optional<User> found = repository.get(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("John", found.get().getName());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void get_whenNotExists_returnsEmpty() {
        Optional<User> found = repository.get(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() {
        List<User> all = repository.getAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void getAll_whenHasUsers_returnsAllUsers() {
        User user1 = User.builder().name("John").email("john@example.com").build();
        User user2 = User.builder().name("Jane").email("jane@example.com").build();
        repository.add(user1);
        repository.add(user2);

        List<User> all = repository.getAll();

        assertEquals(2, all.size());
    }

    @Test
    void remove_whenExists_returnsTrueAndRemovesUser() {
        User user = User.builder().name("John").email("john@example.com").build();
        User saved = repository.add(user);

        boolean removed = repository.remove(saved.getId());

        assertTrue(removed);
        assertTrue(repository.get(saved.getId()).isEmpty());
    }

    @Test
    void remove_whenNotExists_returnsFalse() {
        boolean removed = repository.remove(999L);

        assertFalse(removed);
    }

    @Test
    void remove_thenAdd_generatesNewId() {
        User user = User.builder().name("John").email("john@example.com").build();
        User saved = repository.add(user);
        repository.remove(saved.getId());

        User newUser = User.builder().name("Jane").email("jane@example.com").build();
        User newSaved = repository.add(newUser);

        assertEquals(2L, newSaved.getId());
    }
}