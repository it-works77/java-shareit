package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.RequestItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestCreateResponseDto addByUserId(Long requestorId, ItemRequestRequestDto itemRequestRequestDto) {
        log.info("Add item request: itemRequestDto={}", itemRequestRequestDto);
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(requestorId)));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestRequestDto, requestor);
        itemRequestRepository.save(itemRequest);

        log.info("Item request added: itemRequest={}", itemRequest);
        return ItemRequestMapper.toItemRequestCreateResponseDto(itemRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        log.info("Get item request by id={}", requestId);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(userId)));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос на вещь не найден по id=%s"
                        .formatted(requestId)));

        // Получить вещи, у которых указан requestId (вещь создана в ответ на запрос)
        List<Item> items = itemRepository.findByRequestId(requestId);

        /* получить данные об одном конкретном запросе вместе с данными об ответах на него в том же формате,
         *  что и в эндпоинте GET /requests (метод getAllByRequesterId)
         * */
        log.info("Item request found: itemRequest={}", itemRequest);
        return ItemRequestMapper.toItemRequestResponseDto(itemRequest,
                items.stream()
                        .map(ItemMapper::mapItemToRequestItemResponseDto)
                        .toList());
    }

    // получить список своих запросов вместе с данными об ответах на них.

    /**
     * Возвращает список запросов текущего пользователя вместе с ответами на них.
     * Для запроса должны быть указаны описание, дата и время создания,
     * а также список ответов в формате: id вещи, название, id владельца.
     * <p>
     * Запросы сортируются по дате создания в порядке убывания. Для каждого запроса
     * подгружаются связанные вещи (ответы) и преобразуются в DTO.
     *
     * @param requestorId идентификатор пользователя, создавшего запросы
     * @return список DTO запросов с ответами; пустой список, если у пользователя нет запросов
     * @throws EntityNotFoundException если пользователь с указанным идентификатором не найден
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getAllByRequestorId(Long requestorId) {
        log.info("Get all item requests by requestor id: requestorId={}", requestorId);
        userRepository.findById(requestorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(requestorId)));

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId);
        if (itemRequests.isEmpty()) {
            return List.of();
        }

        List<Item> itemList = itemRepository.findAllByRequestIdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .toList());
        log.info("Requestor id={} items found: items.size={}", requestorId, itemList.size());

        return enrichWithItems(itemRequests, itemList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getAllNotMy(Long requestorId) {
        log.info("Get other users' item requests: requestorId={}", requestorId);
        userRepository.findById(requestorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(requestorId)));

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(requestorId);
        if (itemRequests.isEmpty()) {
            return List.of();
        }

        List<Item> items = itemRepository.findAllByRequestIdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .toList());
        log.info("Items found (other users than Requestor id={}: items.size={}", requestorId, items.size());

        return enrichWithItems(itemRequests, items);
    }

    private List<ItemRequestResponseDto> enrichWithItems(List<ItemRequest> itemRequests,
                                                         List<Item> items) {
        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return itemRequests.stream()
                .map(itemRequest -> {
                    List<RequestItemResponseDto> requestItemResponseDtos =
                            itemsByRequestId.getOrDefault(itemRequest.getId(), List.of()).stream()
                                    .map(ItemMapper::mapItemToRequestItemResponseDto)
                                    .toList();
                    return ItemRequestMapper.toItemRequestResponseDto(itemRequest, requestItemResponseDtos);
                })
                .toList();
    }
}
