package ru.yandex.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.PageParams;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.dto.UserShortDto;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityValidator entityValidator;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        validateNewUserRequest(request);
        ensureEmailUnique(request.getEmail(), null);

        User user = userMapper.toEntity(request);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = entityValidator.ensureAndGet(userRepository, id, "Пользователь");
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, PageParams pageParams) {
        PageRequest pageable = PageRequest.of(pageParams.getPageNumber(),
                Math.max(1, pageParams.getSize()), Sort.by("id").ascending());
        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
        } else {
            users = userRepository.findAllBy(pageable);
        }
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        entityValidator.ensureExists(userRepository, id, "Пользователь");
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = entityValidator.ensureAndGet(userRepository, id, "Пользователь");

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            ensureEmailUnique(userDto.getEmail(), id);
        }

        userMapper.updateEntityFromDto(userDto, user);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public UserShortDto getUserShortById(Long id) {
        User user = entityValidator.ensureAndGet(userRepository, id, "Пользователь");
        return userMapper.toUserShortDto(user);
    }

    @Override
    public List<UserShortDto> getUsersShort(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<User> users = userRepository.findAllById(ids);
        return users.stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toList());
    }

    private void validateNewUserRequest(NewUserRequest req) {
        if (req == null) {
            throw new ValidationException("Запрос на добавление пользователя не должен быть пустым");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
    }

    private void ensureEmailUnique(String email, Long existingUserIdToIgnore) {
        userRepository.findByEmail(email).ifPresent(u -> {
            if (existingUserIdToIgnore == null || !Objects.equals(u.getId(), existingUserIdToIgnore)) {
                throw new ValidationException("Email должен быть уникальным!");
            }
        });
    }
}