package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Size(min = 1, message = "Имя не может быть пустым, если указано")
    private String name;

    @Email(message = "Некорректный email")
    private String email;
}
