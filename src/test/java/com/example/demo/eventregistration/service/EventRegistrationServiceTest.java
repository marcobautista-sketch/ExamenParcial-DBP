package com.example.demo.eventregistration.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.demo.campusevent.domain.CampusEvent;
import com.example.demo.campusevent.domain.Category;
import com.example.demo.campusevent.domain.EventStatus;
import com.example.demo.campusevent.repository.CampusEventRepository;
import com.example.demo.eventregistration.domain.EventRegistration;
import com.example.demo.eventregistration.domain.RegistrationStatus;
import com.example.demo.eventregistration.dto.EventRegistrationRequestDto;
import com.example.demo.eventregistration.dto.EventRegistrationResponseDto;
import com.example.demo.eventregistration.event.RegistrationCreatedEvent;
import com.example.demo.eventregistration.repository.EventRegistrationRepository;
import com.example.demo.exception.BadRequestException;
import com.example.demo.tickettype.domain.TicketType;
import com.example.demo.tickettype.domain.TicketTypeStatus;
import com.example.demo.tickettype.repository.TicketTypeRepository;
import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EventRegistrationServiceTest {

    @Mock
    private EventRegistrationRepository registrationRepository;

    @Mock
    private CampusEventRepository campusEventRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EventRegistrationService registrationService;

    private User attendee;
    private User organizer;
    private CampusEvent publishedEvent;
    private CampusEvent draftEvent;
    private TicketType availableTicket;
    private TicketType soldOutTicket;

    @BeforeEach
    void setUp() {
        attendee = User.builder()
                .id(1L)
                .username("student1")
                .email("student1@utec.edu.pe")
                .password("encoded_pass")
                .role(Role.ATTENDEE)
                .build();

        organizer = User.builder()
                .id(2L)
                .username("organizer1")
                .email("organizer@utec.edu.pe")
                .role(Role.ORGANIZER)
                .build();

        publishedEvent = CampusEvent.builder()
                .id(100L)
                .organizer(organizer)
                .title("Feria de Proyectos CS")
                .description("Presentacion de proyectos estudiantiles")
                .category(Category.TECHNOLOGY)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location("Auditorio UTEC")
                .status(EventStatus.PUBLISHED)
                .build();

        draftEvent = CampusEvent.builder()
                .id(101L)
                .organizer(organizer)
                .title("Evento en Borrador")
                .category(Category.ACADEMIC)
                .eventDate(LocalDateTime.now().plusDays(5))
                .location("Aula 101")
                .status(EventStatus.DRAFT)
                .build();

        availableTicket = TicketType.builder()
                .id(200L)
                .event(publishedEvent)
                .name("Entrada General")
                .capacity(50)
                .registeredCount(10)
                .status(TicketTypeStatus.AVAILABLE)
                .build();

        soldOutTicket = TicketType.builder()
                .id(201L)
                .event(publishedEvent)
                .name("VIP")
                .capacity(5)
                .registeredCount(5)
                .status(TicketTypeStatus.SOLD_OUT)
                .build();
    }

    @Test
    @DisplayName("Inscripción exitosa cuando el evento está publicado y hay cupos")
    void registerAttendee_Success() {
        EventRegistrationRequestDto requestDto = EventRegistrationRequestDto.builder()
                .eventId(publishedEvent.getId())
                .ticketTypeId(availableTicket.getId())
                .build();

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(attendee));
        when(campusEventRepository.findById(publishedEvent.getId())).thenReturn(Optional.of(publishedEvent));
        when(ticketTypeRepository.findById(availableTicket.getId())).thenReturn(Optional.of(availableTicket));
        when(registrationRepository.existsByAttendeeIdAndEventId(attendee.getId(), publishedEvent.getId())).thenReturn(false);

        EventRegistration savedRegistration = EventRegistration.builder()
                .id(1000L)
                .event(publishedEvent)
                .ticketType(availableTicket)
                .attendee(attendee)
                .registeredAt(LocalDateTime.now())
                .status(RegistrationStatus.CONFIRMED)
                .build();

        when(registrationRepository.save(any(EventRegistration.class))).thenReturn(savedRegistration);

        EventRegistrationResponseDto response = registrationService.registerAttendee(requestDto, "student1");

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        assertEquals(publishedEvent.getId(), response.getEventId());
        assertEquals(11, availableTicket.getRegisteredCount());
        verify(registrationRepository).save(any(EventRegistration.class));
        verify(eventPublisher).publishEvent(any(RegistrationCreatedEvent.class));
    }

    @Test
    @DisplayName("Error al intentar inscribirse en un evento no publicado (status DRAFT)")
    void registerAttendee_FailsWhenEventNotPublished() {
        EventRegistrationRequestDto requestDto = EventRegistrationRequestDto.builder()
                .eventId(draftEvent.getId())
                .ticketTypeId(availableTicket.getId())
                .build();

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(attendee));
        when(campusEventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                registrationService.registerAttendee(requestDto, "student1")
        );

        assertEquals("No es posible inscribirse. El evento no está publicado.", exception.getMessage());
    }

    @Test
    @DisplayName("Error al intentar inscribirse en una entrada sin cupos disponibles")
    void registerAttendee_FailsWhenTicketTypeSoldOut() {
        EventRegistrationRequestDto requestDto = EventRegistrationRequestDto.builder()
                .eventId(publishedEvent.getId())
                .ticketTypeId(soldOutTicket.getId())
                .build();

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(attendee));
        when(campusEventRepository.findById(publishedEvent.getId())).thenReturn(Optional.of(publishedEvent));
        when(ticketTypeRepository.findById(soldOutTicket.getId())).thenReturn(Optional.of(soldOutTicket));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                registrationService.registerAttendee(requestDto, "student1")
        );

        assertEquals("Entrada sin cupos disponibles.", exception.getMessage());
    }
}
