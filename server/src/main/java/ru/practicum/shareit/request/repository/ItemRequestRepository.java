package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    // Запросы должны возвращаться отсортированными от более новых к более старым
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    // Запросы должны возвращаться отсортированными от более новых к более старым
    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId);
}
