package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@Validated
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto add(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                               @Valid @RequestBody ItemCreateRequestDto itemCreateRequestDto) {
        return itemService.addByUserId(userId, itemCreateRequestDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto get(@PathVariable @Positive Long itemId) {
        return itemService.get(itemId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                  @RequestParam(name = "text") String text) {
        return itemService.search(userId, text);
    }

    @GetMapping

    public List<ItemResponseDto> getAllByUserId(@RequestHeader(USER_ID_HEADER) @Positive Long userId) {
        return itemService.getAllByUserId(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                  @PathVariable @Positive Long itemId,
                                  @Valid @RequestBody ItemUpdateRequestDto itemUpdateRequestDto) {
        return itemService.updateById(userId, itemId, itemUpdateRequestDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable @Positive Long itemId) {
        itemService.remove(itemId);
    }
}
