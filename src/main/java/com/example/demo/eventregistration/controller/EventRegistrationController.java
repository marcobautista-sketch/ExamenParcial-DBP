package com.example.demo.eventregistration.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.eventregistration.dto.EventRegistrationRequestDto;
import com.example.demo.eventregistration.dto.EventRegistrationResponseDto;
import com.example.demo.eventregistration.service.EventRegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    @PostMapping
    public ResponseEntity<EventRegistrationResponseDto> register(
            @Valid @RequestBody EventRegistrationRequestDto dto,
            Principal principal
    ) {
        EventRegistrationResponseDto response = registrationService.registerAttendee(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<EventRegistrationResponseDto>> getMyRegistrations(Principal principal) {
        List<EventRegistrationResponseDto> list = registrationService.getMyRegistrations(principal.getName());
        return ResponseEntity.ok(list);
    }
}
