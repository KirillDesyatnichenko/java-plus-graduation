package ru.yandex.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto dto);

    List<CategoryDto> toDtoList(List<Category> categories);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    void updateFromDto(CategoryDto dto, @MappingTarget Category entity);

}