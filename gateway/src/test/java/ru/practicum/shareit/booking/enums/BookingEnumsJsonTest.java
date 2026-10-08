package ru.practicum.shareit.booking.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
class BookingEnumsJsonTest {
    @Autowired
    private JacksonTester<BookingStatus> statusJson;

    @Autowired
    private JacksonTester<BookingRequestState> stateJson;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void status_serializesByName() throws Exception {
        assertThat(statusJson.write(BookingStatus.WAITING)).extractingJsonPathStringValue("$").isEqualTo("WAITING");
    }

    @Test
    void status_deserializesUppercase() throws Exception {
        assertThat(statusJson.parseObject("\"APPROVED\"")).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void status_lowercaseFailsUnlikeStateFrom() throws Exception {
        assertThatThrownBy(() -> objectMapper.readValue("\"waiting\"", BookingStatus.class))
                .isInstanceOf(com.fasterxml.jackson.databind.JsonMappingException.class);
    }

    @Test
    void status_unknownValueFails() throws Exception {
        assertThatThrownBy(() -> objectMapper.readValue("\"UNKNOWN\"", BookingStatus.class))
                .isInstanceOf(com.fasterxml.jackson.databind.JsonMappingException.class);
    }

    @Test
    void state_serializesByName() throws Exception {
        assertThat(stateJson.write(BookingRequestState.ALL)).extractingJsonPathStringValue("$").isEqualTo("ALL");
    }

    @Test
    void state_lowercaseFailsViaJackson() throws Exception {
        assertThatThrownBy(() -> objectMapper.readValue("\"waiting\"", BookingRequestState.class))
                .isInstanceOf(com.fasterxml.jackson.databind.JsonMappingException.class);
    }
}
