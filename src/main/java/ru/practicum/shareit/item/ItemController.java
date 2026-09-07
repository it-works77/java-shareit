package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemRequestDto;
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
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto add(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                               @Valid @RequestBody ItemRequestDto itemRequestDto) {
        // TODO catch exc MissingRequestHeaderException
        return itemService.addByUserId(userId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto get(@PathVariable @Positive Long itemId) {
        return itemService.get(itemId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                  @RequestParam(name = "text") @NotBlank String text) {
        return itemService.search(userId, text);
    }

    @GetMapping
    public List<ItemResponseDto> getAllByUserId(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        // TODO Move X-Sharer-User-Id to app config
        return itemService.getAllByUserId(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@PathVariable Long itemId,
                                  @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemService.updateById(itemId, itemRequestDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable @Positive Long itemId) {
        itemService.remove(itemId);
    }
}
