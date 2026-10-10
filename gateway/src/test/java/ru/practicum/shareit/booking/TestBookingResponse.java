package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestBookingResponse {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private TestUserShort booker;
    private TestItemShort item;
}
