package ru.yandex.practicum.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/internal/users/{userId}")
    UserShortDto getUserShort(@PathVariable Long userId);

    @GetMapping("/internal/users")
    List<UserShortDto> getUsersShort(@RequestParam List<Long> ids);
}
