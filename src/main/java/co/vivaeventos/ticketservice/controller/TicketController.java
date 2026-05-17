package co.vivaeventos.ticketservice.controller;

import co.vivaeventos.ticketservice.model.Ticket;
import co.vivaeventos.ticketservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/generate")
    public ResponseEntity<Ticket> generateTicket(@RequestBody GenerateTicketRequest request) {
        System.out.println("Generando boleta para orden: " + request.getOrderId());
        
        Ticket ticket = ticketService.generateTicket(
            request.getOrderId(),
            request.getEventId(),
            request.getEventName(),
            request.getEventDate(),
            request.getLocation(),
            request.getTicketType(),
            request.getQuantity(),
            request.getTotalAmount(),
            request.getUserEmail()
        );
        
        return ResponseEntity.ok(ticket);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Ticket> getTicketByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ticketService.getTicketByOrderId(orderId));
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTicket(@RequestBody ValidateRequest request) {
        Ticket ticket = ticketService.validateTicket(request.getTicketNumber());
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("ticketNumber", ticket.getTicketNumber());
        response.put("eventName", ticket.getEventName());
        response.put("eventDate", ticket.getEventDate());
        response.put("location", ticket.getLocation());
        response.put("ticketType", ticket.getTicketType());
        response.put("quantity", ticket.getQuantity());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/use/{orderId}")
    public ResponseEntity<Ticket> markAsUsed(@PathVariable Long orderId) {
        return ResponseEntity.ok(ticketService.markAsUsed(orderId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ticket>> searchTicketsByEventName(
            @RequestParam String eventName,
            @RequestParam String userEmail) {
        
        List<Ticket> tickets = ticketService.findByUserEmailAndEventNameContaining(userEmail, eventName);
        return ResponseEntity.ok(tickets);
    }
    
    // Clases DTO con getters y setters
    public static class GenerateTicketRequest {
        private Long orderId;
        private Long eventId;
        private String eventName;
        private String eventDate;
        private String location;
        private String ticketType;
        private Integer quantity;
        private Double totalAmount;
        private String userEmail;
        
        // Getters y Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        
        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }
        
        public String getEventDate() { return eventDate; }
        public void setEventDate(String eventDate) { this.eventDate = eventDate; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getTicketType() { return ticketType; }
        public void setTicketType(String ticketType) { this.ticketType = ticketType; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    }
    
    public static class ValidateRequest {
        private String ticketNumber;
        
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    }
}