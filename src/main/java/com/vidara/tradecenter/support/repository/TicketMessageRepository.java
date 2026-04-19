package com.vidara.tradecenter.support.repository;

import com.vidara.tradecenter.support.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    List<TicketMessage> findByTicketIdOrderBySentAt(Long ticketId);

    long countByTicketId(Long ticketId);
}
