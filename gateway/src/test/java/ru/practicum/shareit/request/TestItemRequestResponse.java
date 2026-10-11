package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestItemRequestResponse {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<TestItemShortForRequest> items;
}
