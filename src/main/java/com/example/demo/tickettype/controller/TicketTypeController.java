package com.example.demo.tickettype.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.tickettype.dto.TicketTypeRequestDto;
import com.example.demo.tickettype.dto.TicketTypeResponseDto;
import com.example.demo.tickettype.service.TicketTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<TicketTypeResponseDto> createTicketType(
            @Valid @RequestBody TicketTypeRequestDto dto,
            Principal principal
    ) {
        TicketTypeResponseDto response = ticketTypeService.createTicketType(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketTypeResponseDto>> getTicketTypesByEvent(@PathVariable Long eventId) {
        List<TicketTypeResponseDto> list = ticketTypeService.getTicketTypesByEvent(eventId);
        return ResponseEntity.ok(list);
    }
}
