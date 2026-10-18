package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestCreateResponseDtoTest {
    private final JacksonTester<ItemRequestCreateResponseDto> json;

    @Test
    void shouldSerializeItemRequestResponseDto() throws IOException {
        ItemRequestCreateResponseDto dto = getDtoWithItems(null);
        JsonContent<ItemRequestCreateResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-10-16T11:28:53.1234567");
    }

    private static ItemRequestCreateResponseDto getDtoWithItems(List<ItemRequestCreateResponseDto> items) {
        LocalDateTime time = LocalDateTime.of(2026, 10, 16,
                11, 28, 53, 123_456_700);
        ItemRequestCreateResponseDto dto = ItemRequestCreateResponseDto.builder()
                .id(1L)
                .description("Test")
                .created(time)
                .build();
        return dto;
    }

}