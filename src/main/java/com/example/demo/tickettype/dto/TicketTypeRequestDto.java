package com.example.demo.tickettype.dto;

import jakarta.validation.constraints.Min;
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
public class TicketTypeRequestDto {

    @NotNull(message = "El id del evento es obligatorio")
    private Long eventId;

    @NotBlank(message = "El nombre del tipo de entrada es obligatorio")
    private String name;

    @Min(value = 1, message = "La capacidad debe ser de al menos 1 persona")
    private int capacity;
}
