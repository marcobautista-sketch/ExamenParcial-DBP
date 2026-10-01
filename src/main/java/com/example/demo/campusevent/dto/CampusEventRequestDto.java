package com.example.demo.campusevent.dto;

import java.time.LocalDateTime;

import com.example.demo.campusevent.domain.Category;
import com.example.demo.campusevent.domain.EventStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampusEventRequestDto {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "La categoría es obligatoria")
    private Category category;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDateTime eventDate;

    @NotBlank(message = "La ubicación es obligatoria")
    private String location;

    private EventStatus status;
}
