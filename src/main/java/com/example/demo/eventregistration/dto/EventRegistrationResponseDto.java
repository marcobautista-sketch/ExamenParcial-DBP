package com.example.demo.eventregistration.dto;

import java.time.LocalDateTime;

import com.example.demo.eventregistration.domain.RegistrationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRegistrationResponseDto {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long ticketTypeId;
    private String ticketTypeName;
    private Long attendeeId;
    private String attendeeUsername;
    private LocalDateTime registeredAt;
    private RegistrationStatus status;
}
