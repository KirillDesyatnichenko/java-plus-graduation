package dto;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = {EndpointHitDto.class})
class EndpointHitDtoTest {

    @Autowired
    private JacksonTester<EndpointHitDto> json;

    @Test
    void serialize() throws Exception {
        LocalDateTime time = LocalDateTime.of(2025, 9, 29, 16, 10, 44);

        EndpointHitDto hit = EndpointHitDto.builder()
                .id(1)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(time)
                .build();

        assertThat(this.json.write(hit)).hasJsonPathNumberValue("@.id");
        assertThat(this.json.write(hit)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(1);

        assertThat(this.json.write(hit)).hasJsonPathStringValue("@.app");
        assertThat(this.json.write(hit)).extractingJsonPathStringValue("@.app")
                .isEqualTo("ewm-main-service");

        assertThat(this.json.write(hit)).hasJsonPathStringValue("@.uri");
        assertThat(this.json.write(hit)).extractingJsonPathStringValue("@.uri")
                .isEqualTo("/events/1");

        assertThat(this.json.write(hit)).hasJsonPathStringValue("@.ip");
        assertThat(this.json.write(hit)).extractingJsonPathStringValue("@.ip")
                .isEqualTo("192.163.0.1");

        assertThat(this.json.write(hit)).hasJsonPathStringValue("@.timestamp");
        assertThat(this.json.write(hit)).extractingJsonPathStringValue("@.timestamp")
                .isEqualTo("2025-09-29 16:10:44");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"id\":1,\"app\":\"ewm-main-service\"," +
                "\"uri\":\"/events/1\",\"ip\":\"192.163.0.1\"," +
                "\"timestamp\":\"2025-09-30 16:10:44\"}";

        LocalDateTime time = LocalDateTime.of(2025, 9, 30, 16, 10, 44);

        assertThat(this.json.parse(content)).isEqualTo(EndpointHitDto.builder()
                .id(1)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(time)
                .build());

        assertThat(this.json.parseObject(content).getId()).isEqualTo(1);
        assertThat(this.json.parseObject(content).getApp()).isEqualTo("ewm-main-service");
        assertThat(this.json.parseObject(content).getUri()).isEqualTo("/events/1");
        assertThat(this.json.parseObject(content).getIp()).isEqualTo("192.163.0.1");
        assertThat(this.json.parseObject(content).getTimestamp()).isEqualTo(time);
    }
}
