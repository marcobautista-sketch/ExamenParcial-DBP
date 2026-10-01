package com.example.demo.eventregistration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRegistrationRequestDto {

    @NotNull(message = "El id del evento es obligatorio")
    private Long eventId;

    @NotNull(message = "El id del tipo de entrada es obligatorio")
    private Long ticketTypeId;
}
