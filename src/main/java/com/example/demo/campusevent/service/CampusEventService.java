package com.example.demo.campusevent.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.campusevent.domain.CampusEvent;
import com.example.demo.campusevent.domain.EventStatus;
import com.example.demo.campusevent.dto.CampusEventRequestDto;
import com.example.demo.campusevent.dto.CampusEventResponseDto;
import com.example.demo.campusevent.repository.CampusEventRepository;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampusEventService {

    private final CampusEventRepository campusEventRepository;
    private final UserRepository userRepository;

    @Transactional
    public CampusEventResponseDto createEvent(CampusEventRequestDto dto, String username) {
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        if (organizer.getRole() != Role.ORGANIZER && organizer.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Solo los organizadores y administradores pueden crear eventos");
        }

        CampusEvent event = CampusEvent.builder()
                .organizer(organizer)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .eventDate(dto.getEventDate())
                .location(dto.getLocation())
                .status(dto.getStatus() != null ? dto.getStatus() : EventStatus.DRAFT)
                .build();

        CampusEvent savedEvent = campusEventRepository.save(event);
        return mapToDto(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<CampusEventResponseDto> getFuturePublishedEvents() {
        return campusEventRepository.findByStatusAndEventDateAfter(EventStatus.PUBLISHED, LocalDateTime.now())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CampusEventResponseDto getEventById(Long id) {
        CampusEvent event = campusEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        return mapToDto(event);
    }

    @Transactional
    public CampusEventResponseDto updateEvent(Long id, CampusEventRequestDto dto, String username) {
        CampusEvent event = campusEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        validateOwnershipOrAdmin(event, currentUser);

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setCategory(dto.getCategory());
        event.setEventDate(dto.getEventDate());
        event.setLocation(dto.getLocation());
        if (dto.getStatus() != null) {
            event.setStatus(dto.getStatus());
        }

        CampusEvent updated = campusEventRepository.save(event);
        return mapToDto(updated);
    }

    @Transactional
    public CampusEventResponseDto publishEvent(Long id, String username) {
        CampusEvent event = campusEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        validateOwnershipOrAdmin(event, currentUser);

        event.setStatus(EventStatus.PUBLISHED);
        CampusEvent updated = campusEventRepository.save(event);
        return mapToDto(updated);
    }

    private void validateOwnershipOrAdmin(CampusEvent event, User user) {
        if (!event.getOrganizer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("No tiene permisos para modificar este evento. No es el organizador ni un administrador.");
        }
    }

    public CampusEventResponseDto mapToDto(CampusEvent event) {
        return CampusEventResponseDto.builder()
                .id(event.getId())
                .organizerId(event.getOrganizer().getId())
                .organizerUsername(event.getOrganizer().getUsername())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .status(event.getStatus())
                .build();
    }
}
