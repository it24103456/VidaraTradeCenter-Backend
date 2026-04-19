package com.vidara.tradecenter.support.repository;

import com.vidara.tradecenter.support.model.SupportTicket;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserId(Long userId);

    List<SupportTicket> findByStatus(TicketStatus status);

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(TicketStatus status);
}
