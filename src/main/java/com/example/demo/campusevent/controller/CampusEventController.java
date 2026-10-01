package com.example.demo.campusevent.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.campusevent.dto.CampusEventRequestDto;
import com.example.demo.campusevent.dto.CampusEventResponseDto;
import com.example.demo.campusevent.service.CampusEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class CampusEventController {

    private final CampusEventService campusEventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<CampusEventResponseDto> createEvent(
            @Valid @RequestBody CampusEventRequestDto dto,
            Principal principal
    ) {
        CampusEventResponseDto response = campusEventService.createEvent(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CampusEventResponseDto>> getFuturePublishedEvents() {
        List<CampusEventResponseDto> events = campusEventService.getFuturePublishedEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampusEventResponseDto> getEventById(@PathVariable Long id) {
        CampusEventResponseDto event = campusEventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<CampusEventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CampusEventRequestDto dto,
            Principal principal
    ) {
        CampusEventResponseDto response = campusEventService.updateEvent(id, dto, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<CampusEventResponseDto> publishEvent(
            @PathVariable Long id,
            Principal principal
    ) {
        CampusEventResponseDto response = campusEventService.publishEvent(id, principal.getName());
        return ResponseEntity.ok(response);
    }
}
