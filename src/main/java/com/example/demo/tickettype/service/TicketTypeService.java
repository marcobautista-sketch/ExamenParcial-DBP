package com.example.demo.tickettype.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.campusevent.domain.CampusEvent;
import com.example.demo.campusevent.repository.CampusEventRepository;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.tickettype.domain.TicketType;
import com.example.demo.tickettype.domain.TicketTypeStatus;
import com.example.demo.tickettype.dto.TicketTypeRequestDto;
import com.example.demo.tickettype.dto.TicketTypeResponseDto;
import com.example.demo.tickettype.repository.TicketTypeRepository;
import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final CampusEventRepository campusEventRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketTypeResponseDto createTicketType(TicketTypeRequestDto dto, String username) {
        CampusEvent event = campusEventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + dto.getEventId()));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        if (!event.getOrganizer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("No tiene permisos para agregar tipos de entrada a este evento");
        }

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .registeredCount(0)
                .status(TicketTypeStatus.AVAILABLE)
                .build();

        TicketType saved = ticketTypeRepository.save(ticketType);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponseDto> getTicketTypesByEvent(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TicketTypeResponseDto mapToDto(TicketType ticketType) {
        return TicketTypeResponseDto.builder()
                .id(ticketType.getId())
                .eventId(ticketType.getEvent().getId())
                .name(ticketType.getName())
                .capacity(ticketType.getCapacity())
                .registeredCount(ticketType.getRegisteredCount())
                .status(ticketType.getStatus())
                .build();
    }
}
