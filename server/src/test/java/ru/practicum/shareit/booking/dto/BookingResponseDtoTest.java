package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingResponseDtoTest {
    private final JacksonTester<BookingResponseDto> json;

    @Test
    void shouldSerializeBookingResponseDto() throws IOException {
        BookingResponseDto dto = getDto(BookingStatus.APPROVED);
        JsonContent<BookingResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-10-16T11:28:53.1234567");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-10-17T11:28:53.1234567");

        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");

        // Вложенный объект booker
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);

        // Вложенный объект item
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("test");
    }

    private static BookingResponseDto getDto(BookingStatus status) {
        LocalDateTime start = LocalDateTime.of(2026, 10, 16,
                11, 28, 53, 123_456_700);
        LocalDateTime end = LocalDateTime.of(2026, 10, 17,
                11, 28, 53, 123_456_700);

        return BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(status)
                .booker(new BookingResponseDto.Booker(1L))
                .item(new BookingResponseDto.Item(1L, "test"))
                .build();
    }
}