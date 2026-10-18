package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
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

    @Test
    void addByUserId_whenUserExists_thenReturnCreated() {
        User requestor = saveUser("requestor", "requestor@email.com");

        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription("Need a drill");

        ItemRequestCreateResponseDto response = itemRequestService.addByUserId(requestor.getId(), dto);

        assertNotNull(response.getId());
        assertEquals("Need a drill", response.getDescription());
        assertNotNull(response.getCreated());
    }

    @Test
    void addByUserId_whenUserNotFound_thenThrow() {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription("Need a drill");

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.addByUserId(999L, dto));
    }

    @Test
    void getById_whenRequestHasItems_thenReturnWithItems() {
        User requestor = saveUser("requestor", "requestor@email.com");
        User owner = saveUser("owner", "owner@email.com");
        ItemRequest request = saveItemRequest(requestor, "Need a drill",
                LocalDateTime.of(2026, 1, 2, 0, 0));
        Item item = saveItem(owner, "item1", "Drill", true, request);

        ItemRequestResponseDto response = itemRequestService.getById(requestor.getId(), request.getId());

        assertEquals(request.getId(), response.getId());
        assertEquals(1, response.getItems().size());
        assertEquals(item.getId(), response.getItems().get(0).getId());
    }

    @Test
    void getById_whenNoItems_thenReturnEmptyItems() {
        User requestor = saveUser("requestor", "requestor@email.com");
        ItemRequest request = saveItemRequest(requestor, "Need a drill",
                LocalDateTime.of(2026, 1, 2, 0, 0));

        ItemRequestResponseDto response = itemRequestService.getById(requestor.getId(), request.getId());

        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void getById_whenRequestNotFound_thenThrow() {
        User requestor = saveUser("requestor", "requestor@email.com");

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getById(requestor.getId(), 999L));
    }

    @Test
    void getAllByRequestorId_whenRequestsExist_thenReturnSortedWithItems() {
        User requestor = saveUser("requestor", "requestor@email.com");
        User owner = saveUser("owner", "owner@email.com");
        LocalDateTime now = LocalDateTime.of(2026, 1, 3, 0, 0);
        ItemRequest older = saveItemRequest(requestor, "older", now.plusMinutes(1));
        ItemRequest newer = saveItemRequest(requestor, "newer", now.plusMinutes(2));
        Item item = saveItem(owner, "item1", "Drill", true, newer);

        List<ItemRequestResponseDto> result = itemRequestService.getAllByRequestorId(requestor.getId());

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
        assertEquals(older.getId(), result.get(1).getId());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(item.getId(), result.get(0).getItems().get(0).getId());
        assertTrue(result.get(1).getItems().isEmpty());
    }

    @Test
    void getAllByRequestorId_whenEmpty_thenReturnEmpty() {
        User requestor = saveUser("requestor", "requestor@email.com");

        assertTrue(itemRequestService.getAllByRequestorId(requestor.getId()).isEmpty());
    }

    @Test
    void getAllByRequestorId_whenUserNotFound_thenThrow() {
        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getAllByRequestorId(999L));
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