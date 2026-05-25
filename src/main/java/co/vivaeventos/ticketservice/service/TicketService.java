package co.vivaeventos.ticketservice.service;

import co.vivaeventos.ticketservice.model.Ticket;
import co.vivaeventos.ticketservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final QRCodeService qrCodeService;

    public Ticket generateTicket(Long orderId, Long eventId, String eventName, 
                                  String eventDate, String location, 
                                  String ticketType, Integer quantity, 
                                  Double totalAmount, String userEmail) {
        
        log.info("Generando boleta para orden: {}", orderId);
        
        // Datos que irán en el QR
        String qrData = String.format(
            "ORDER:%d|EVENT:%s|DATE:%s|TYPE:%s|QTY:%d",
            orderId, eventName, eventDate, ticketType, quantity
        );
        
        // Generar imagen QR en Base64
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrData, 300, 300);
        
        // Crear boleta
        Ticket ticket = new Ticket();
        ticket.setOrderId(orderId);
        ticket.setEventId(eventId);
        ticket.setEventName(eventName);
        ticket.setEventDate(eventDate);
        ticket.setLocation(location);
        ticket.setTicketType(ticketType);
        ticket.setQuantity(quantity);
        ticket.setTotalAmount(totalAmount);
        ticket.setUserEmail(userEmail);
        ticket.setQrCode(qrCodeBase64);
        
        Ticket saved = ticketRepository.save(ticket);
        log.info("Boleta generada: {}", saved.getTicketNumber());
        
        return saved;
    }
    
    public Ticket getTicketByOrderId(Long orderId) {
        return ticketRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada para la orden: " + orderId));
    }
    
    public Ticket validateTicket(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada"));
        
        // NO lanzar excepción, solo devolver el ticket con su estado
        // El Validation Service decidirá qué hacer
        return ticket;  
    }
    
    public Ticket markAsUsed(Long orderId) {
        Ticket ticket = getTicketByOrderId(orderId);
        ticket.setUsed(true);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> findByUserEmailAndEventNameContaining(String userEmail, String eventName) {
    return ticketRepository.findByUserEmailAndEventNameContainingIgnoreCase(userEmail, eventName);
    }
}