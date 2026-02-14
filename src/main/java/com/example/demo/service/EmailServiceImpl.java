

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

    // Bloque estático para asegurar que la preferencia IPv4 se establezca al cargar la clase
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    @Override
    @Async // Para enviar correos en un hilo separado y no bloquear la respuesta HTTP
    public void enviarCorreoSimple(String para, String asunto, String texto) {
        try {
            // Construir el sender dinámicamente con los valores de la BD
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(configuracionService.getValor("SMTP_HOST", "smtp.gmail.com"));
            
            int port = configuracionService.getValorInt("SMTP_PORT", 587);
            mailSender.setPort(port);
            
            String username = configuracionService.getValor("SMTP_USERNAME", "soporterobotechti@gmail.com");
            mailSender.setUsername(username);
            mailSender.setPassword(configuracionService.getValor("SMTP_PASSWORD", ""));

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", configuracionService.getValor("SMTP_AUTH", "true"));
            
            // Lógica para alternar entre SSL (465) y STARTTLS (587)
            if (port == 465) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "false");
                // Aseguramos que no haya socketFactory explícito que cause conflictos con IPv6/Render
                props.remove("mail.smtp.socketFactory.class");
            } else {
                props.put("mail.smtp.ssl.enable", "false");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true"); // Obligatorio para puerto 587 en la nube
            }
            
            // Configuración para estabilidad en la nube (Render)
            props.put("mail.smtp.connectiontimeout", "30000"); // 30 segundos
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.smtp.writetimeout", "30000");
            props.put("mail.smtp.ssl.trust", "*"); // Confiar en el certificado del servidor (Soluciona problemas de handshake)
            props.put("mail.debug", "true"); // Activado para ver el log detallado de la conexión SMTP

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username); // Es buena práctica establecer el remitente explícitamente
            message.setTo(para);
            message.setSubject(asunto);
            message.setText(texto);

            // Log de diagnóstico restaurado
            System.out.println("Intentando enviar correo a: " + para + " | Host: " + mailSender.getHost() + ":" + mailSender.getPort() + " | Usuario: " + username);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Manejar la excepción, por ejemplo, loguearla.
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
 
