package com.example.demo.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ConfiguracionService configuracionService;

    @Override
    @Async // Para enviar correos en un hilo separado y no bloquear la respuesta HTTP
    public void enviarCorreoSimple(String para, String asunto, String texto) {
        String apiKey = configuracionService.getValor("SENDGRID_API_KEY", "");
        
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR CRÍTICO: No se ha configurado SENDGRID_API_KEY. El correo no se enviará.");
            return;
        }

        try {
            String remitente = configuracionService.getValor("SMTP_USERNAME", "soporterobotechti@gmail.com");
            
            Email from = new Email(remitente);
            String subject = asunto;
            Email to = new Email(para);
            Content content = new Content("text/plain", texto);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            System.out.println("Correo enviado con SendGrid a: " + para + " | Status: " + response.getStatusCode());

        } catch (IOException e) {
            System.err.println("Error al enviar correo con SendGrid: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
 
