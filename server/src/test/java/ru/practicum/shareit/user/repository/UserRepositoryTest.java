package ru.practicum.shareit.user.repository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_whenExists_returnsUser() {
        User saved = userRepository.save(User.builder().name("John").email("john@mail.com").build());

        Optional<User> found = userRepository.findByEmail("john@mail.com");

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findByEmail_whenNotExists_returnsEmpty() {
        userRepository.save(User.builder().name("John").email("john@mail.com").build());

        assertTrue(userRepository.findByEmail("nobody@mail.com").isEmpty());
    }
}
