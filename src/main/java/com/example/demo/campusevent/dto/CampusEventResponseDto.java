package com.example.demo.campusevent.dto;

import java.time.LocalDateTime;

import com.example.demo.campusevent.domain.Category;
import com.example.demo.campusevent.domain.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampusEventResponseDto {
    private Long id;
    private Long organizerId;
    private String organizerUsername;
    private String title;
    private String description;
    private Category category;
    private LocalDateTime eventDate;
    private String location;
    private EventStatus status;
}
