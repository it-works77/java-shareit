package ru.practicum.shareit.item.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByItemId_returnsItemComments() {
        User owner = user("owner");
        User author = user("author");
        Item item = item(owner, "Drill");
        Item other = item(owner, "Saw");
        comment(author, item, "Nice", LocalDateTime.now().minusHours(2));
        comment(author, item, "Great", LocalDateTime.now().minusHours(1));
        comment(author, other, "Other", LocalDateTime.now());

        List<Comment> result = commentRepository.findAllByItemId(item.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(comment -> comment.getItem().getId().equals(item.getId())));
    }

    @Test
    void findAllByItemOwnerIdOrderByCreatedDesc_filtersAndSorts() {
        User owner = user("owner");
        User stranger = user("stranger");
        User author = user("author");
        Item first = item(owner, "Drill");
        Item second = item(owner, "Saw");
        Item foreign = item(stranger, "Hammer");
        LocalDateTime now = LocalDateTime.now();
        comment(author, first, "old", now.minusHours(3));
        Comment newest = comment(author, second, "new", now.minusHours(1));
        Comment middle = comment(author, first, "mid", now.minusHours(2));
        comment(author, foreign, "foreign", now);

        List<Comment> result = commentRepository.findAllByItemOwnerIdOrderByCreatedDesc(owner.getId());

        assertEquals(3, result.size());
        assertEquals(newest.getId(), result.get(0).getId());
        assertEquals(middle.getId(), result.get(1).getId());
    }

    @Test
    void findAllByItemOwnerIdOrderByCreatedDesc_whenEmpty_returnsEmpty() {
        User owner = user("owner");

        assertTrue(commentRepository.findAllByItemOwnerIdOrderByCreatedDesc(owner.getId()).isEmpty());
    }

    private User user(String name) {
        return userRepository.save(User.builder().name(name).email(name + "@mail.com").build());
    }

    private Item item(User owner, String name) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description("d")
                .available(true)
                .ownerId(owner.getId())
                .build());
    }

    private Comment comment(User author, Item item, String text, LocalDateTime created) {
        return commentRepository.save(Comment.builder()
                .author(author)
                .item(item)
                .text(text)
                .created(created)
                .build());
    }
}
