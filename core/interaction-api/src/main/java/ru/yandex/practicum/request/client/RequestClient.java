package ru.yandex.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.request.client.dto.ConfirmedRequestCountDto;
import ru.yandex.practicum.request.client.dto.RequestCountDto;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {

    @GetMapping("/internal/requests/confirmed")
    List<ConfirmedRequestCountDto> getConfirmedCounts(@RequestParam List<Long> eventIds);

    @GetMapping("/internal/requests/counts")
    List<RequestCountDto> getRequestCounts(@RequestParam List<Long> eventIds);
}
