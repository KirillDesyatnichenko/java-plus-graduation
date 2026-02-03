package ru.yandex.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.event.dao.EventRepository;
import ru.yandex.practicum.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;
    private final EntityValidator entityValidator;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategory) {
        if (categoryRepository.existsByName(newCategory.getName())) {
            throw new ValidationException("Категория с таким именем уже существует!");
        }

        Category saved = categoryRepository.save(mapper.toEntity(newCategory));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = entityValidator.ensureAndGet(categoryRepository, catId, "Категория");

        long relatedEvents = eventRepository.countByCategoryId(catId);
        if (relatedEvents > 0) {
            throw new ValidationException("Категория не пуста!");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category existing = entityValidator.ensureAndGet(categoryRepository, catId, "Категория");

        if (!existing.getName().equalsIgnoreCase(categoryDto.getName())
                && categoryRepository.existsByName(categoryDto.getName())) {
            throw new ValidationException("Категория с таким именем уже существует!");
        }

        mapper.updateFromDto(categoryDto, existing);
        existing.setId(catId);
        Category saved = categoryRepository.save(existing);
        return mapper.toDto(saved);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {

        int page = from / size;
        List<Category> list = categoryRepository.findAll(PageRequest.of(page, size)).getContent();
        return list.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = entityValidator.ensureAndGet(categoryRepository, catId, "Категория");
        return mapper.toDto(category);
    }
}