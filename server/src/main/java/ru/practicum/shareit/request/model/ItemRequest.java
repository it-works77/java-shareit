package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Sprint add-item-requests.
 * Ещё одна сущность, которая вам понадобится, — запрос вещи ItemRequest. Пользователь создаёт запрос, если нужная
 * ему вещь не найдена при поиске. В запросе указывается, что именно он ищет. В ответ на запрос другие пользователи
 * могут добавить нужную вещь.
 */
@Entity
@Table(name = "requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // текст запроса, в котором пользователь описывает, какая именно вещь ему нужна
    @Column(name = "description", nullable = false)
    private String description;

    // пользователь, который создал запрос
    @ManyToOne
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}