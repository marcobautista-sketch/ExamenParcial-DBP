package com.example.demo.eventregistration.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RegistrationCreatedEvent {
    private final Long registrationId;
    private final String attendeeEmail;
    private final String eventTitle;
    private final String ticketTypeName;
}
