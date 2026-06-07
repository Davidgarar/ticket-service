package co.vivaeventos.ticketservice.service;

import co.vivaeventos.ticketservice.client.EmailServiceClient; // <-- NUEVO IMPORT
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
    private final EmailServiceClient emailServiceClient; // <-- NUEVA INYECCIÓN

    public Ticket generateTicket(Long orderId, Long eventId, String eventName, 
                                  String eventDate, String location, 
                                  String ticketType, Integer quantity, 
                                  Double totalAmount, String userEmail) { //
        
        log.info("Generando boleta para orden: {}", orderId); //
        
        // Datos que irán en el QR
        String qrData = String.format(
            "ORDER:%d|EVENT:%s|DATE:%s|TYPE:%s|QTY:%d",
            orderId, eventName, eventDate, ticketType, quantity
        ); //
        
        // Generar imagen QR en Base64
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrData, 300, 300); //
        
        // Crear boleta
        Ticket ticket = new Ticket(); //
        ticket.setOrderId(orderId); //
        ticket.setEventId(eventId); //
        ticket.setEventName(eventName); //
        ticket.setEventDate(eventDate); //
        ticket.setLocation(location); //
        ticket.setTicketType(ticketType); //
        ticket.setQuantity(quantity); //
        ticket.setTotalAmount(totalAmount); //
        ticket.setUserEmail(userEmail); //
        ticket.setQrCode(qrCodeBase64); //
        
        Ticket saved = ticketRepository.save(ticket); //
        log.info("Boleta generada: {}", saved.getTicketNumber()); //
        
        // === NUEVA LÓGICA: Envío de correo inmediato de confirmación ===
        try {
            String subject = "🎟️ Confirmación de compra - " + eventName;
            String body = String.format(
                "¡Hola!\n\nTu compra para el evento '%s' ha sido confirmada de manera exitosa.\n\n" +
                "Detalles de tu asistencia:\n" +
                "▪️ Fecha: %s\n" +
                "▪️ Lugar: %s\n" +
                "▪️ Tipo de Entrada: %s\n" +
                "▪️ Cantidad: %d boleta(s)\n\n" +
                "Tu código QR ya se encuentra disponible en la aplicación móvil de VivaEventos. ¡Disfruta la experiencia!",
                eventName, eventDate, location, ticketType, quantity
            );
            
            // Consumo síncrono del cliente Feign (el email-service gestionará el hilo asíncrono)
            emailServiceClient.sendEmail(new EmailServiceClient.EmailRequest(userEmail, subject, body));
            log.info("Distribución de correo solicitada satisfactoriamente para: {}", userEmail);
        } catch (Exception e) {
            log.error("⚠️ Fallo no crítico al despachar correo a través de email-service: {}", e.getMessage());
        }
        
        return saved; //
    }
    
    public Ticket getTicketByOrderId(Long orderId) { //
        return ticketRepository.findByOrderId(orderId) //
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada para la orden: " + orderId)); //
    } //
    
    public Ticket validateTicket(String ticketNumber) { //
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber) //
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada")); //
        return ticket;  //
    } //
    
    public Ticket markAsUsed(Long orderId) { //
        Ticket ticket = getTicketByOrderId(orderId); //
        ticket.setUsed(true); //
        return ticketRepository.save(ticket); //
    } //

    public List<Ticket> findByUserEmailAndEventNameContaining(String userEmail, String eventName) { //
        return ticketRepository.findByUserEmailAndEventNameContainingIgnoreCase(userEmail, eventName); //
    } //
}