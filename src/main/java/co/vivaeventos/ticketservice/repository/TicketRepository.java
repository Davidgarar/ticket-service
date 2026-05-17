package co.vivaeventos.ticketservice.repository;

import co.vivaeventos.ticketservice.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByOrderId(Long orderId);
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByUserEmailAndEventNameContainingIgnoreCase(String userEmail, String eventName);

    
}
