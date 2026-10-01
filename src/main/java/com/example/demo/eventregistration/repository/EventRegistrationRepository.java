package com.example.demo.eventregistration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.eventregistration.domain.EventRegistration;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    
    boolean existsByAttendeeIdAndEventId(Long attendeeId, Long eventId);

    Optional<EventRegistration> findByAttendeeIdAndEventId(Long attendeeId, Long eventId);

    List<EventRegistration> findByAttendeeId(Long attendeeId);

    List<EventRegistration> findByEventId(Long eventId);
}
