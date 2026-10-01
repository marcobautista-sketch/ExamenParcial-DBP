package com.example.demo.integration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.auth.dto.AuthResponseDto;
import com.example.demo.auth.dto.UserLoginRequestDto;
import com.example.demo.auth.dto.UserRegisterRequestDto;
import com.example.demo.campusevent.domain.Category;
import com.example.demo.campusevent.domain.EventStatus;
import com.example.demo.campusevent.dto.CampusEventRequestDto;
import com.example.demo.campusevent.dto.CampusEventResponseDto;
import com.example.demo.eventregistration.domain.RegistrationStatus;
import com.example.demo.eventregistration.dto.EventRegistrationRequestDto;
import com.example.demo.eventregistration.dto.EventRegistrationResponseDto;
import com.example.demo.tickettype.dto.TicketTypeRequestDto;
import com.example.demo.tickettype.dto.TicketTypeResponseDto;
import com.example.demo.user.domain.Role;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthAndRegistrationIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_campuseventdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driverClassName", () -> "org.postgresql.Driver");
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:integrationdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        }
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Flujo de integración completo: Registro, Login con JWT y Inscripción a Evento con PostgresSQL/Testcontainers")
    void testFullRegistrationFlowWithJwtAndPostgres() {
        // 1. Registro de Organizador
        UserRegisterRequestDto orgRegister = UserRegisterRequestDto.builder()
                .username("organizer_user")
                .email("organizer@utec.edu.pe")
                .password("password123")
                .role(Role.ORGANIZER)
                .build();

        ResponseEntity<AuthResponseDto> orgRegResp = restTemplate.postForEntity(
                "/auth/register", orgRegister, AuthResponseDto.class
        );
        assertEquals(HttpStatus.CREATED, orgRegResp.getStatusCode());
        assertNotNull(orgRegResp.getBody());

        // 2. Registro de Asistente (Attendee)
        UserRegisterRequestDto attendeeRegister = UserRegisterRequestDto.builder()
                .username("attendee_user")
                .email("attendee@utec.edu.pe")
                .password("password123")
                .role(Role.ATTENDEE)
                .build();

        ResponseEntity<AuthResponseDto> attRegResp = restTemplate.postForEntity(
                "/auth/register", attendeeRegister, AuthResponseDto.class
        );
        assertEquals(HttpStatus.CREATED, attRegResp.getStatusCode());

        // 3. Login de Organizador para obtener Token JWT
        UserLoginRequestDto orgLogin = UserLoginRequestDto.builder()
                .username("organizer_user")
                .password("password123")
                .build();

        ResponseEntity<AuthResponseDto> orgLoginResp = restTemplate.postForEntity(
                "/auth/login", orgLogin, AuthResponseDto.class
        );
        assertEquals(HttpStatus.OK, orgLoginResp.getStatusCode());
        String orgToken = orgLoginResp.getBody().getToken();
        assertNotNull(orgToken);

        // 4. Crear Evento con Token JWT de Organizador
        HttpHeaders orgHeaders = new HttpHeaders();
        orgHeaders.setContentType(MediaType.APPLICATION_JSON);
        orgHeaders.setBearerAuth(orgToken);

        CampusEventRequestDto eventRequest = CampusEventRequestDto.builder()
                .title("Feria de Proyectos CS")
                .description("Presentacion de proyectos estudiantiles")
                .category(Category.TECHNOLOGY)
                .eventDate(LocalDateTime.now().plusDays(15))
                .location("Auditorio UTEC")
                .status(EventStatus.DRAFT)
                .build();

        HttpEntity<CampusEventRequestDto> createEventEntity = new HttpEntity<>(eventRequest, orgHeaders);
        ResponseEntity<CampusEventResponseDto> createEventResp = restTemplate.exchange(
                "/api/events", HttpMethod.POST, createEventEntity, CampusEventResponseDto.class
        );
        assertEquals(HttpStatus.CREATED, createEventResp.getStatusCode());
        Long eventId = createEventResp.getBody().getId();
        assertEquals("Feria de Proyectos CS", createEventResp.getBody().getTitle());
        assertEquals("organizer_user", createEventResp.getBody().getOrganizerUsername());

        // 5. Publicar Evento
        HttpEntity<Void> publishEntity = new HttpEntity<>(orgHeaders);
        ResponseEntity<CampusEventResponseDto> publishResp = restTemplate.exchange(
                "/api/events/" + eventId + "/publish", HttpMethod.PATCH, publishEntity, CampusEventResponseDto.class
        );
        assertEquals(HttpStatus.OK, publishResp.getStatusCode());
        assertEquals(EventStatus.PUBLISHED, publishResp.getBody().getStatus());

        // 6. Crear Tipo de Entrada (TicketType)
        TicketTypeRequestDto ticketRequest = TicketTypeRequestDto.builder()
                .eventId(eventId)
                .name("Entrada General")
                .capacity(100)
                .build();

        HttpEntity<TicketTypeRequestDto> createTicketEntity = new HttpEntity<>(ticketRequest, orgHeaders);
        ResponseEntity<TicketTypeResponseDto> createTicketResp = restTemplate.exchange(
                "/api/ticket-types", HttpMethod.POST, createTicketEntity, TicketTypeResponseDto.class
        );
        assertEquals(HttpStatus.CREATED, createTicketResp.getStatusCode());
        Long ticketTypeId = createTicketResp.getBody().getId();

        // 7. Login de Asistente para obtener Token JWT
        UserLoginRequestDto attLogin = UserLoginRequestDto.builder()
                .username("attendee_user")
                .password("password123")
                .build();

        ResponseEntity<AuthResponseDto> attLoginResp = restTemplate.postForEntity(
                "/auth/login", attLogin, AuthResponseDto.class
        );
        assertEquals(HttpStatus.OK, attLoginResp.getStatusCode());
        String attToken = attLoginResp.getBody().getToken();
        assertNotNull(attToken);

        // 8. Inscribirse al Evento con Token JWT de Asistente
        HttpHeaders attHeaders = new HttpHeaders();
        attHeaders.setContentType(MediaType.APPLICATION_JSON);
        attHeaders.setBearerAuth(attToken);

        EventRegistrationRequestDto registrationRequest = EventRegistrationRequestDto.builder()
                .eventId(eventId)
                .ticketTypeId(ticketTypeId)
                .build();

        HttpEntity<EventRegistrationRequestDto> registerEntity = new HttpEntity<>(registrationRequest, attHeaders);
        ResponseEntity<EventRegistrationResponseDto> registrationResp = restTemplate.exchange(
                "/api/registrations", HttpMethod.POST, registerEntity, EventRegistrationResponseDto.class
        );

        assertEquals(HttpStatus.CREATED, registrationResp.getStatusCode());
        assertNotNull(registrationResp.getBody());
        assertEquals(eventId, registrationResp.getBody().getEventId());
        assertEquals("Feria de Proyectos CS", registrationResp.getBody().getEventTitle());
        assertEquals("attendee_user", registrationResp.getBody().getAttendeeUsername());
        assertEquals(RegistrationStatus.CONFIRMED, registrationResp.getBody().getStatus());

        // 9. Consultar mis inscripciones como asistente
        HttpEntity<Void> myRegistrationsEntity = new HttpEntity<>(attHeaders);
        ResponseEntity<EventRegistrationResponseDto[]> myRegistrationsResp = restTemplate.exchange(
                "/api/registrations/me", HttpMethod.GET, myRegistrationsEntity, EventRegistrationResponseDto[].class
        );

        assertEquals(HttpStatus.OK, myRegistrationsResp.getStatusCode());
        assertTrue(myRegistrationsResp.getBody().length > 0);
        assertEquals(eventId, myRegistrationsResp.getBody()[0].getEventId());
    }
}
