package co.vivaeventos.ticketservice.scheduler;

import co.vivaeventos.ticketservice.client.EmailServiceClient;
import co.vivaeventos.ticketservice.model.Ticket;
import co.vivaeventos.ticketservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventReminderScheduler {

    private final TicketRepository ticketRepository;
    private final EmailServiceClient emailServiceClient;

    // Se ejecuta de manera automática cada mañana a las 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void executeDailyReminders() {
        String tomorrowDate = LocalDate.now().plusDays(1).toString();
        log.info("⏰ Cron Job Activo: Buscando boletas vigentes para el día de mañana ({})", tomorrowDate);

        List<Ticket> ticketsForTomorrow = ticketRepository.findByEventDateAndUsedFalse(tomorrowDate);
        log.info("Se identificaron {} registros de asistencia para notificar.", ticketsForTomorrow.size());

        for (Ticket ticket : ticketsForTomorrow) {
            String subject = "⏰ Recordatorio: ¡Mañana es tu evento " + ticket.getEventName() + "!";
            String body = String.format(
                "¡Hola! Te escribimos de VivaEventos para recordarte que mañana es el gran día.\n\n" +
                "📌 Evento: %s\n" +
                "📍 Ubicación: %s\n" +
                "📅 Fecha: %s\n\n" +
                "Recuerda tener cargado tu código QR desde tu cuenta (Ticket N°: %s) para agilizar el ingreso en las puertas de control de logística.",
                ticket.getEventName(), ticket.getLocation(), ticket.getEventDate(), ticket.getTicketNumber()
            );

            try {
                emailServiceClient.sendEmail(new EmailServiceClient.EmailRequest(ticket.getUserEmail(), subject, body));
            } catch (Exception e) {
                log.error("No se pudo procesar el recordatorio para el ticket {}: {}", ticket.getTicketNumber(), e.getMessage());
            }
        }
    }
}