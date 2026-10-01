package com.example.demo.eventregistration.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.campusevent.domain.CampusEvent;
import com.example.demo.campusevent.domain.EventStatus;
import com.example.demo.campusevent.repository.CampusEventRepository;
import com.example.demo.eventregistration.domain.EventRegistration;
import com.example.demo.eventregistration.domain.RegistrationStatus;
import com.example.demo.eventregistration.dto.EventRegistrationRequestDto;
import com.example.demo.eventregistration.dto.EventRegistrationResponseDto;
import com.example.demo.eventregistration.event.RegistrationCreatedEvent;
import com.example.demo.eventregistration.repository.EventRegistrationRepository;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.tickettype.domain.TicketType;
import com.example.demo.tickettype.domain.TicketTypeStatus;
import com.example.demo.tickettype.repository.TicketTypeRepository;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final CampusEventRepository campusEventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EventRegistrationResponseDto registerAttendee(EventRegistrationRequestDto dto, String username) {
        User attendee = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        CampusEvent event = campusEventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + dto.getEventId()));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("No es posible inscribirse. El evento no está publicado.");
        }

        TicketType ticketType = ticketTypeRepository.findById(dto.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada no encontrado con id: " + dto.getTicketTypeId()));

        if (!ticketType.getEvent().getId().equals(event.getId())) {
            throw new BadRequestException("El tipo de entrada no corresponde al evento especificado.");
        }

        if (ticketType.getStatus() == TicketTypeStatus.SOLD_OUT || ticketType.getRegisteredCount() >= ticketType.getCapacity()) {
            throw new BadRequestException("Entrada sin cupos disponibles.");
        }

        if (registrationRepository.existsByAttendeeIdAndEventId(attendee.getId(), event.getId())) {
            throw new BadRequestException("Inscripción duplicada. El usuario ya está registrado en este evento.");
        }

        ticketType.setRegisteredCount(ticketType.getRegisteredCount() + 1);
        if (ticketType.getRegisteredCount() >= ticketType.getCapacity()) {
            ticketType.setStatus(TicketTypeStatus.SOLD_OUT);
        }
        ticketTypeRepository.save(ticketType);

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .ticketType(ticketType)
                .attendee(attendee)
                .registeredAt(LocalDateTime.now())
                .status(RegistrationStatus.CONFIRMED)
                .build();

        EventRegistration savedRegistration = registrationRepository.save(registration);

        eventPublisher.publishEvent(RegistrationCreatedEvent.builder()
                .registrationId(savedRegistration.getId())
                .attendeeEmail(attendee.getEmail())
                .eventTitle(event.getTitle())
                .ticketTypeName(ticketType.getName())
                .build());

        return mapToDto(savedRegistration);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponseDto> getMyRegistrations(String username) {
        User attendee = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        return registrationRepository.findByAttendeeId(attendee.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EventRegistrationResponseDto mapToDto(EventRegistration registration) {
        return EventRegistrationResponseDto.builder()
                .id(registration.getId())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .ticketTypeId(registration.getTicketType().getId())
                .ticketTypeName(registration.getTicketType().getName())
                .attendeeId(registration.getAttendee().getId())
                .attendeeUsername(registration.getAttendee().getUsername())
                .registeredAt(registration.getRegisteredAt())
                .status(registration.getStatus())
                .build();
    }
}
