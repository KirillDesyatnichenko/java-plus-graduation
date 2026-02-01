package ru.yandex.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.repository.CompilationRepository;
import ru.yandex.practicum.event.dao.EventRepository;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EntityValidator entityValidator;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        log.info("Создание новой подборки с названием: {}", compilationDto.getTitle());

        // Проверка уникальности названия
        if (compilationRepository.existsByTitle(compilationDto.getTitle())) {
            throw new ConflictException("Подборка с названием='" + compilationDto.getTitle() + "' уже существует");
        }

        List<Event> events = getEventsByIds(compilationDto.getEvents());

        Compilation compilation = compilationMapper.toEntity(compilationDto, events);
        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Подборка создана с id: {}", savedCompilation.getId());
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("Обновление подборки с id: {}", compId);

        Compilation compilation = entityValidator.ensureAndGet(
                compilationRepository, compId, "Подборка"
        );

        // Проверка уникальности названия при обновлении
        if (request.getTitle() != null && !request.getTitle().equals(compilation.getTitle())) {
            if (compilationRepository.existsByTitle(request.getTitle())) {
                throw new ConflictException("Подборка с названием='" + request.getTitle() + "' уже существует");
            }
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            List<Event> events = getEventsByIds(request.getEvents());
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка обновлена с id: {}", compId);

        return compilationMapper.toDto(updatedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        // Проверяем существование перед удалением
        entityValidator.ensureExists(compilationRepository, compId, "Подборка");

        compilationRepository.deleteById(compId);
        log.info("Подборка удалена с id: {}", compId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение подборки с pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilationsPage = compilationRepository.findByPinned(pinned, pageable);

        return compilationMapper.toDtoList(compilationsPage.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки по id: {}", compId);

        Compilation compilation = entityValidator.ensureAndGet(
                compilationRepository, compId, "Подборка"
        );

        return compilationMapper.toDto(compilation);
    }

    private List<Event> getEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        List<Event> events = eventRepository.findAllById(eventIds);

        // Проверяем, что все события найдены
        if (events.size() != eventIds.size()) {
            List<Long> foundIds = events.stream().map(Event::getId).toList();
            List<Long> missingIds = eventIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NotFoundException("События с id=" + missingIds + " не найдены");
        }

        return events;
    }
}