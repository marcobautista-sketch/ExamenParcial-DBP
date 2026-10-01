package com.example.demo.campusevent.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.campusevent.domain.CampusEvent;
import com.example.demo.campusevent.domain.Category;
import com.example.demo.campusevent.domain.EventStatus;

@Repository
public interface CampusEventRepository extends JpaRepository<CampusEvent, Long> {
    
    List<CampusEvent> findByStatusAndEventDateAfter(EventStatus status, LocalDateTime date);
    
    List<CampusEvent> findByOrganizerId(Long organizerId);

    List<CampusEvent> findByCategoryAndStatus(Category category, EventStatus status);
}
