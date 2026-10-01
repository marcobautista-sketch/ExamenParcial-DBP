package com.example.demo.tickettype.dto;

import com.example.demo.tickettype.domain.TicketTypeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketTypeResponseDto {
    private Long id;
    private Long eventId;
    private String name;
    private int capacity;
    private int registeredCount;
    private TicketTypeStatus status;
}
