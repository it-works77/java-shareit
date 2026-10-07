package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestServiceImpl itemRequestService;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllNotMy_whenThreeUsersWithRequests_thenReturnAllNotMyRequests() {
        User myUser = saveUser("myUser", "myUser@email.com");
        User requestor2 = saveUser("requestor2", "requestor2@email.com");
        User requestor3 = saveUser("requestor3", "requestor3@email.com");

        // При проверках далее зависимость от времени создания (недетерминированное поведение теста)
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        ItemRequest myUserRequest1 = saveItemRequest(myUser, "myUserRequest1",
                now.plusMinutes(1));
        ItemRequest myUserRequest2WithItem = saveItemRequest(myUser, "myUserRequest2WithItem",
                now.plusMinutes(2));

        ItemRequest requestor2Request1WithItems = saveItemRequest(requestor3, "requestor2Request1WithItem",
                now.plusMinutes(3));
        ItemRequest requestor2Request2WithItem = saveItemRequest(requestor2, "requestor2Request2WithItem",
                now.plusMinutes(4));
        ItemRequest requestor3Request1 = saveItemRequest(requestor3, "requestor3Request1",
                now.plusMinutes(5));
        ItemRequest requestor3Request2 = saveItemRequest(requestor3, "requestor3Request2",
                now.plusMinutes(6));

        Item myUserItem1 = saveItem(myUser, "item1", "myUserItem1", true, null);

        // Мои две вещи для чужого запроса
        Item myUserItem2 = saveItem(myUser, "item2", "myUserItem2", true,
                requestor2Request1WithItems);
        Item myUserItem3 = saveItem(myUser, "item3", "myUserItem3", false,
                requestor2Request1WithItems);

        // Моя вещь, одна для чужого запроса
        Item myUserItem4 = saveItem(myUser, "item4", "myUserItem4", true,
                requestor2Request2WithItem);

        // Вещь для моего запроса (не должно быть в списке)
        Item requestor3Item1 = saveItem(requestor3, "item5", "requestor3Item1", true,
                myUserRequest2WithItem);

        List<ItemRequestResponseDto> requestDtos = itemRequestService.getAllNotMy(myUser.getId());

        assertEquals(4, requestDtos.size());

        // Отсортированы: самый новый - первый
        assertEquals(requestor3Request2.getId(), requestDtos.get(0).getId());
        assertEquals(requestor2Request1WithItems.getId(), requestDtos.get(3).getId());

        // Вещи должны быть добавлены в ответ
        assertEquals(0, requestDtos.get(0).getItems().size());
        assertEquals(0, requestDtos.get(1).getItems().size());
        assertEquals(1, requestDtos.get(2).getItems().size());
        assertEquals(2, requestDtos.get(3).getItems().size());

        // В ответ добавлены корректные вещи
        assertEquals(myUserItem2.getId(), requestDtos.get(3).getItems().get(0).getId());
        assertEquals(myUserItem3.getId(), requestDtos.get(3).getItems().get(1).getId());
    }

    private Item saveItem(User owner, String name, String description, boolean isAvailable, ItemRequest itemRequest) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description(description)
                .available(isAvailable)
                .ownerId(owner.getId())
                .requestId(itemRequest == null ? null : itemRequest.getId())
                .build());
    }

    private ItemRequest saveItemRequest(User user, String description, LocalDateTime created) {
        return itemRequestRepository.save(ItemRequest.builder()
                .description(description)
                .requestor(user)
                .created(created)
                .build());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }
}