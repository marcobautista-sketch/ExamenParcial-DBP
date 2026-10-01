package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.demo.campusevent.domain.CampusEvent;
import com.example.demo.campusevent.domain.Category;
import com.example.demo.campusevent.domain.EventStatus;
import com.example.demo.campusevent.repository.CampusEventRepository;
import com.example.demo.eventregistration.domain.EventRegistration;
import com.example.demo.eventregistration.domain.RegistrationStatus;
import com.example.demo.eventregistration.repository.EventRegistrationRepository;
import com.example.demo.tickettype.domain.TicketType;
import com.example.demo.tickettype.domain.TicketTypeStatus;
import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
class RepositoryDataJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CampusEventRepository campusEventRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    private User organizer;
    private User attendee;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
                .username("organizer1")
                .email("org@utec.edu.pe")
                .password("password123")
                .role(Role.ORGANIZER)
                .build();
        entityManager.persist(organizer);

        attendee = User.builder()
                .username("attendee1")
                .email("att@utec.edu.pe")
                .password("password123")
                .role(Role.ATTENDEE)
                .build();
        entityManager.persist(attendee);
    }

    @Test
    @DisplayName("DataJpaTest: Búsqueda exitosa de eventos futuros en estado PUBLISHED")
    void testSearchFuturePublishedEvents() {
        CampusEvent pastEvent = CampusEvent.builder()
                .organizer(organizer)
                .title("Evento Pasado")
                .description("Descripción")
                .category(Category.ACADEMIC)
                .eventDate(LocalDateTime.now().minusDays(2))
                .location("Auditorio A")
                .status(EventStatus.PUBLISHED)
                .build();

        CampusEvent draftEvent = CampusEvent.builder()
                .organizer(organizer)
                .title("Evento Borrador Futuro")
                .description("Descripción")
                .category(Category.TECHNOLOGY)
                .eventDate(LocalDateTime.now().plusDays(5))
                .location("Lab 1")
                .status(EventStatus.DRAFT)
                .build();

        CampusEvent futurePublishedEvent = CampusEvent.builder()
                .organizer(organizer)
                .title("Feria de Proyectos CS")
                .description("Presentacion de proyectos estudiantiles")
                .category(Category.TECHNOLOGY)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location("Auditorio UTEC")
                .status(EventStatus.PUBLISHED)
                .build();

        entityManager.persist(pastEvent);
        entityManager.persist(draftEvent);
        entityManager.persist(futurePublishedEvent);
        entityManager.flush();

        List<CampusEvent> futureEvents = campusEventRepository.findByStatusAndEventDateAfter(
                EventStatus.PUBLISHED, LocalDateTime.now()
        );

        assertEquals(1, futureEvents.size());
        assertEquals("Feria de Proyectos CS", futureEvents.get(0).getTitle());
    }

    @Test
    @DisplayName("DataJpaTest: Restricción de inscripción única lanzando excepción al duplicar")
    void testUniqueRegistrationConstraint() {
        CampusEvent event = CampusEvent.builder()
                .organizer(organizer)
                .title("Feria de Proyectos CS")
                .description("Presentacion de proyectos estudiantiles")
                .category(Category.TECHNOLOGY)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location("Auditorio UTEC")
                .status(EventStatus.PUBLISHED)
                .build();
        entityManager.persist(event);

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name("Entrada General")
                .capacity(50)
                .registeredCount(0)
                .status(TicketTypeStatus.AVAILABLE)
                .build();
        entityManager.persist(ticketType);
        entityManager.flush();

        EventRegistration registration1 = EventRegistration.builder()
                .event(event)
                .ticketType(ticketType)
                .attendee(attendee)
                .registeredAt(LocalDateTime.now())
                .status(RegistrationStatus.CONFIRMED)
                .build();
        entityManager.persist(registration1);
        entityManager.flush();

        boolean exceptionThrown = false;
        try {
            EventRegistration duplicateRegistration = EventRegistration.builder()
                    .event(event)
                    .ticketType(ticketType)
                    .attendee(attendee)
                    .registeredAt(LocalDateTime.now())
                    .status(RegistrationStatus.CONFIRMED)
                    .build();
            entityManager.persist(duplicateRegistration);
            entityManager.flush();
        } catch (Exception ex) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown, "Se esperaba una excepción debido a la restricción única (attendee_id, event_id)");
    }
}
