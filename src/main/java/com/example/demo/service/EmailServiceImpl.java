

package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ConfiguracionService configuracionService;

    @Override
    @Async // Para enviar correos en un hilo separado y no bloquear la respuesta HTTP
    public void enviarCorreoSimple(String para, String asunto, String texto) {
        try {
            // Construir el sender dinámicamente con los valores de la BD
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(configuracionService.getValor("SMTP_HOST", "smtp.gmail.com"));
            mailSender.setPort(configuracionService.getValorInt("SMTP_PORT", 587));
            
            String username = configuracionService.getValor("SMTP_USERNAME", "ramirezsolongabriel91@gmail.com");
            mailSender.setUsername(username);
            mailSender.setPassword(configuracionService.getValor("SMTP_PASSWORD", "yhvp xlzo qppr aheg"));

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", configuracionService.getValor("SMTP_AUTH", "true"));
            props.put("mail.smtp.starttls.enable", configuracionService.getValor("SMTP_STARTTLS", "true"));
            
            // Configuración para estabilidad en la nube (Render)
            props.put("mail.smtp.connectiontimeout", "10000"); // 10 segundos
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
            props.put("mail.smtp.ssl.trust", "*"); // Confiar en el certificado del servidor (Soluciona problemas de handshake)
            // props.put("mail.debug", "true"); // Descomentar para ver logs detallados de envío

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username); // Es buena práctica establecer el remitente explícitamente
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
 
