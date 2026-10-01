package com.example.demo.eventregistration.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RegistrationEventListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEventListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCreated(RegistrationCreatedEvent event) {
        log.info("[AFTER_COMMIT Event] Generando entrada digital y enviando correo de confirmación al email {} para el evento '{}' ({})",
                event.getAttendeeEmail(), event.getEventTitle(), event.getTicketTypeName());
        // Simulación de generación de entrada en PDF y envío asincrónico de notificación
    }
}
