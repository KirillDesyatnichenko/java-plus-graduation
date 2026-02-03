package ru.yandex.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    private int from = 0;
    private int size = 10;

    public int getPageNumber() {
        if (size <= 0) return 0;
        return from / size;
    }
}