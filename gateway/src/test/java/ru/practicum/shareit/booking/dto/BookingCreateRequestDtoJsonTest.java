package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<BookingCreateRequestDto> json;

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 8, 12, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 9, 12, 0, 0);

    @Test
    void serialize_datesAsIsoStrings() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(START)
                .end(END)
                .build();

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-08T12:00:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-09T12:00:00");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }

    @Test
    void serialize_exposesDatesValidDerivedProperty() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(START)
                .end(END)
                .build();

        assertThat(json.write(dto)).hasJsonPathBooleanValue("$.datesValid");
        assertThat(json.write(dto)).extractingJsonPathBooleanValue("$.datesValid").isEqualTo(true);
    }

    @Test
    void deserialize_isoStringsToLocalDateTime() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2026-10-08T12:00:00\",\"end\":\"2026-10-09T12:00:00\"}";

        BookingCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getStart()).isEqualTo(START);
        assertThat(dto.getEnd()).isEqualTo(END);
        assertThat(dto.getItemId()).isEqualTo(1L);
    }

    @Test
    void deserialize_withoutBookerId_givesNull() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2026-10-08T12:00:00\",\"end\":\"2026-10-09T12:00:00\"}";

        BookingCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getBookerId()).isNull();
    }

    @Test
    void deserialize_withBookerId_keepsValue() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2026-10-08T12:00:00\","
                + "\"end\":\"2026-10-09T12:00:00\",\"bookerId\":5}";

        BookingCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getBookerId()).isEqualTo(5L);
    }

    @Test
    void deserialize_ignoresUnknownProperties() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2026-10-08T12:00:00\","
                + "\"end\":\"2026-10-09T12:00:00\",\"unknownField\":\"x\"}";

        BookingCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
    }

    @Test
    void roundTrip_keepsAllFields() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(7L)
                .start(START)
                .end(END)
                .bookerId(5L)
                .build();

        BookingCreateRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getItemId()).isEqualTo(7L);
        assertThat(parsed.getStart()).isEqualTo(START);
        assertThat(parsed.getEnd()).isEqualTo(END);
        assertThat(parsed.getBookerId()).isEqualTo(5L);
    }
}
