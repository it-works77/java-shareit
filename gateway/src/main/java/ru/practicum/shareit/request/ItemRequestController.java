package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

@Controller
@RequestMapping("/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                      @Valid @RequestBody ItemRequestRequestDto itemRequestRequestDto) {
        return itemRequestClient.addByUserId(userId, itemRequestRequestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) @Positive Long userId,
                                          @PathVariable Long requestId) {
        return itemRequestClient.getById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequesterId(@RequestHeader(USER_ID_HEADER) @Positive Long userId) {
        return itemRequestClient.getAllByRequestorId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllNotMy(@RequestHeader(USER_ID_HEADER) @Positive Long userId) {
        return itemRequestClient.getAllNotMy(userId);
    }


}
