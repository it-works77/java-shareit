package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto add(@RequestHeader(USER_ID_HEADER) Long userId,
                               @RequestBody ItemCreateRequestDto itemCreateRequestDto) {
        return itemService.addByUserId(userId, itemCreateRequestDto);
    }

    @GetMapping("/{itemId}")
    public ItemGetByIdResponseDto get(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long itemId) {
        return itemService.get(userId, itemId);
    }

    @GetMapping
    public List<ItemGetAllResponseDto> getAllByUserId(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.getAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @RequestParam(name = "text") String text) {
        return itemService.search(text);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody ItemUpdateRequestDto itemUpdateRequestDto) {
        return itemService.updateById(userId, itemId, itemUpdateRequestDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        itemService.remove(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody CommentCreateRequestDto commentCreateRequestDto) {
        return itemService.addComment(userId, itemId, commentCreateRequestDto);
    }
}
