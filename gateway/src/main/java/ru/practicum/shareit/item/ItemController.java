package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;

@Controller
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                      @Valid @RequestBody ItemCreateRequestDto itemCreateRequestDto) {
        log.info("Adding item {}", itemCreateRequestDto);
        return itemClient.add(userId, itemCreateRequestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                      @PathVariable @Positive Long itemId) {
        log.info("Getting item with id {}", itemId);
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUserId(@RequestHeader(USER_ID_HEADER) @Positive Long userId) {
        log.info("Getting all items by user id {}", userId);
        return itemClient.getAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                         @RequestParam(name = "text") String text) {
        return itemClient.search(userId, text);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                         @PathVariable @Positive Long itemId,
                                         @Valid @RequestBody ItemUpdateRequestDto itemUpdateRequestDto) {
        log.info("Updating item with id {}", itemId);
        return itemClient.updateById(userId, itemId, itemUpdateRequestDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable @Positive Long itemId) {
        // TODO Нет проверки пользователя...
        log.info("Deleting item with id {}", itemId);
        return itemClient.remove(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                             @PathVariable @Positive Long itemId,
                                             @Valid @RequestBody CommentCreateRequestDto commentCreateRequestDto) {
        log.info("Adding comment to item with id {}", itemId);
        return itemClient.addComment(userId, itemId, commentCreateRequestDto);
    }
}
