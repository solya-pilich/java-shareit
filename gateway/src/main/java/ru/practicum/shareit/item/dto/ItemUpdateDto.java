package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateDto {

    @Size(min = 1, message = "Название не может быть пустым")
    private String name;

    @Size(min = 1, message = "Описание не может быть пустым")
    private String description;

    private Boolean available;
}
