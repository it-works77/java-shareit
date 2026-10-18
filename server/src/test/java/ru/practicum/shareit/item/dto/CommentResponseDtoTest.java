package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentResponseDtoTest {
    private final JacksonTester<CommentResponseDto> json;

    @Test
    void shouldSerializeCommentResponseDto() throws IOException {
        CommentResponseDto dto = getDto();
        JsonContent<CommentResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Text");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Author");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-10-16T11:28:53.1234567");

    }

    private static CommentResponseDto getDto() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 16,
                11, 28, 53, 123_456_700);
        return CommentResponseDto.builder()
                .id(1L)
                .text("Text")
                .authorName("Author")
                .created(time)
                .build();
    }
}