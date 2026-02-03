package ru.yandex.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.user.dto.UserShortDto;
import ru.yandex.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserShortDto getUserShort(@PathVariable Long userId) {
        return userService.getUserShortById(userId);
    }

    @GetMapping
    public List<UserShortDto> getUsersShort(@RequestParam List<Long> ids) {
        return userService.getUsersShort(ids);
    }
}
