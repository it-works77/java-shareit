package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.RequestItemResponseDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestResponseDtoTest {
    private final JacksonTester<ItemRequestResponseDto> json;

    @Test
    void shouldSerializeItemRequestResponseDto() throws IOException {
        ItemRequestResponseDto dto = getDtoWithItems(null);
        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.items").isNull();
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-10-16T11:28:53.1234567");
    }

    @Test
    void shouldSerializeNullItems() throws IOException {
        ItemRequestResponseDto dto = getDtoWithItems(null);
        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.items").isNull();

    }

    @Test
    void shouldSerializeEmptyItems() throws IOException {
        ItemRequestResponseDto dto = getDtoWithItems(List.of());
        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();

    }

    private static ItemRequestResponseDto getDtoWithItems(List<RequestItemResponseDto> items) {
        LocalDateTime time = LocalDateTime.of(2026, 10, 16,
                11, 28, 53, 123_456_700);
        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Test")
                .created(time)
                .items(items)
                .build();
        return dto;
    }
}