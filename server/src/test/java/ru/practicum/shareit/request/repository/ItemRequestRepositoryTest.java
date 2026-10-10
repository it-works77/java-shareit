package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByRequestorIdNotOrderByCreatedDesc_whenRequestsExist_thenReturnOnlyOtherUsersRequestsSorted() {
        LocalDateTime now = LocalDateTime.now();

        User requestor = userRepository.save(User.builder()
                .name("requestor")
                .email("requestor@example.com")
                .build());

        User otherUser = userRepository.save(User.builder()
                .name("other")
                .email("other@example.com")
                .build());

        User anotherUser = userRepository.save(User.builder()
                .name("another")
                .email("another@example.com")
                .build());

        // Запрос самого пользователя — не должен попасть в выборку
        itemRequestRepository.save(ItemRequest.builder()
                .description("Собственный запрос")
                .requestor(requestor)
                .created(now)
                .build());

        ItemRequest olderRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("Старый запрос другого пользователя")
                .requestor(otherUser)
                .created(now.minusHours(2))
                .build());

        ItemRequest newerRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("Новый запрос другого пользователя")
                .requestor(anotherUser)
                .created(now.minusHours(1))
                .build());

        List<ItemRequest> result =
                itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(requestor.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        // Сначала более новый, затем более старый
        assertEquals(newerRequest.getId(), result.get(0).getId());
        assertEquals(olderRequest.getId(), result.get(1).getId());

        // Запросы текущего пользователя не возвращаются
        assertTrue(result.stream()
                .noneMatch(request -> request.getRequestor().getId().equals(requestor.getId())));
    }

    @Test
    void findAllByRequestorIdOrderByCreatedDesc_whenRequestsExist_thenReturnSortedDesc() {
        LocalDateTime now = LocalDateTime.now();

        User requestor = userRepository.save(User.builder()
                .name("requestor")
                .email("requestor@example.com")
                .build());

        User otherUser = userRepository.save(User.builder()
                .name("other")
                .email("other@example.com")
                .build());

        ItemRequest olderRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("Старый запрос")
                .requestor(requestor)
                .created(now.minusHours(2))
                .build());

        ItemRequest newerRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("Новый запрос")
                .requestor(requestor)
                .created(now.minusHours(1))
                .build());

        itemRequestRepository.save(ItemRequest.builder()
                .description("Чужой запрос")
                .requestor(otherUser)
                .created(now)
                .build());

        List<ItemRequest> result =
                itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestor.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(newerRequest.getId(), result.get(0).getId());
        assertEquals(olderRequest.getId(), result.get(1).getId());
    }

    @Test
    void findAllByRequestorIdOrderByCreatedDesc_whenEmpty_returnsEmpty() {
        User requestor = userRepository.save(User.builder()
                .name("requestor")
                .email("requestor@example.com")
                .build());

        assertTrue(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestor.getId()).isEmpty());
    }
}

