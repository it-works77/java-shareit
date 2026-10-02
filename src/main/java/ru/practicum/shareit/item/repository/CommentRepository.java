package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(Long id);

    // Все комментарии ко всем вещам владельца, от новых к старым
    List<Comment> findAllByItemOwnerIdOrderByCreatedDesc(Long ownerId);
}
