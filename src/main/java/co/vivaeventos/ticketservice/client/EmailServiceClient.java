package co.vivaeventos.ticketservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Apuntamos al nombre del microservicio y su url directa en el puerto 8088
@FeignClient(name = "email-service", url = "http://localhost:8088")
public interface EmailServiceClient {

    @PostMapping("/api/v1/emails/send")
    void sendEmail(@RequestBody EmailRequest request);

    // Este DTO espejo permite estructurar el JSON idéntico al que espera el email-service
    class EmailRequest {
        private String to;
        private String subject;
        private String body;

        public EmailRequest(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
    }
}