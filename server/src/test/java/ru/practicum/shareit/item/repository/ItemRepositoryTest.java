package ru.practicum.shareit.item.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void getAllByOwnerId_returnsOnlyOwned() {
        User owner = user("owner");
        User other = user("other");
        Item first = item(owner, "Drill", "Power drill", true, null);
        Item second = item(owner, "Saw", "Hand saw", true, null);
        item(other, "Hammer", "Claw hammer", true, null);

        List<Item> result = itemRepository.getAllByOwnerId(owner.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().map(Item::getId).toList().containsAll(List.of(first.getId(), second.getId())));
    }

    @Test
    void getAllByOwnerId_whenEmpty_returnsEmpty() {
        User owner = user("owner");

        assertTrue(itemRepository.getAllByOwnerId(owner.getId()).isEmpty());
    }

    @Test
    void findAllByText_matchesNameAndDescriptionCaseInsensitive() {
        User owner = user("owner");
        Item byName = item(owner, "Дрель", "Обычный инструмент", true, null);
        Item byDescription = item(owner, "Молоток", "Аккумуляторная ДРЕЛЬ внутри", true, null);
        item(owner, "Saw", "Hand saw", true, null);
        // недоступная вещь с совпадением не должна попадать
        item(owner, "Дрель Pro", "Сломанная", false, null);

        List<Item> result = itemRepository.findAllByText("дрель");

        assertEquals(2, result.size());
        assertTrue(result.stream().map(Item::getId).toList()
                .containsAll(List.of(byName.getId(), byDescription.getId())));
    }

    @Test
    void findAllByText_whenNoMatch_returnsEmpty() {
        User owner = user("owner");
        item(owner, "Drill", "Power drill", true, null);

        assertTrue(itemRepository.findAllByText("hammer").isEmpty());
    }

    @Test
    void existsByOwnerId_returnsTrueAndFalse() {
        User owner = user("owner");
        User stranger = user("stranger");
        item(owner, "Drill", "Power drill", true, null);

        assertTrue(itemRepository.existsByOwnerId(owner.getId()));
        assertFalse(itemRepository.existsByOwnerId(stranger.getId()));
    }

    @Test
    void findByRequestId_returnsLinkedItems() {
        User owner = user("owner");
        ItemRequest request = itemRequest(owner, "Need a drill");
        Item linked = item(owner, "Drill", "Power drill", true, request.getId());
        item(owner, "Saw", "Hand saw", true, null);

        List<Item> result = itemRepository.findByRequestId(request.getId());

        assertEquals(1, result.size());
        assertEquals(linked.getId(), result.get(0).getId());
        assertTrue(itemRepository.findByRequestId(999999L).isEmpty());
    }

    @Test
    void findAllByRequestIdIn_returnsLinkedAndSkipsNullRequestId() {
        User owner = user("owner");
        ItemRequest firstRequest = itemRequest(owner, "Need a drill");
        ItemRequest secondRequest = itemRequest(owner, "Need a saw");
        Item first = item(owner, "Drill", "Power drill", true, firstRequest.getId());
        Item second = item(owner, "Saw", "Hand saw", true, secondRequest.getId());
        item(owner, "Hammer", "Claw hammer", true, null);

        List<Item> result = itemRepository.findAllByRequestIdIn(
                List.of(firstRequest.getId(), secondRequest.getId()));

        assertEquals(2, result.size());
        assertTrue(result.stream().map(Item::getId).toList()
                .containsAll(List.of(first.getId(), second.getId())));
        assertTrue(itemRepository.findAllByRequestIdIn(List.of(999999L)).isEmpty());
    }

    private User user(String name) {
        return userRepository.save(User.builder().name(name).email(name + "@mail.com").build());
    }

    private ItemRequest itemRequest(User requestor, String description) {
        return itemRequestRepository.save(ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build());
    }

    private Item item(User owner, String name, String description, boolean available, Long requestId) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .ownerId(owner.getId())
                .requestId(requestId)
                .build());
    }
}
