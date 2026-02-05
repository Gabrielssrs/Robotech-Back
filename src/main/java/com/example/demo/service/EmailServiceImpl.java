

package com.example.demo.service;

import com.example.demo.service.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async // Para enviar correos en un hilo separado y no bloquear la respuesta HTTP
    public void enviarCorreoSimple(String para, String asunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(para);
            message.setSubject(asunto);
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) {
            // Manejar la excepción, por ejemplo, loguearla.
            System.err.println("Error al enviar correo: " + e.getMessage());
        }
    }
}
 


