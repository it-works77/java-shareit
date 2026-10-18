package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestCreateResponseDto add(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestBody ItemRequestRequestDto itemRequestRequestDto) {
        return itemRequestService.addByUserId(userId, itemRequestRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getAllByRequesterId(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getAllByRequestorId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllNotMy(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getAllNotMy(userId);
    }
}
