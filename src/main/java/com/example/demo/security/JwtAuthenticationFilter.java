package com.example.demo.security;

import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Si no hay cabecera de autorización o no empieza con "Bearer ", continuamos sin autenticar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraemos el token (quitando el prefijo "Bearer ").
        jwt = authHeader.substring(7);

        try {
            // 3. Extraemos el correo del token.
            userEmail = jwtUtil.extractUsername(jwt);

            // 4. Si tenemos el correo y no hay una autenticación activa en el contexto de seguridad...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Cargamos los detalles del usuario desde la base de datos.
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 6. Validamos el token.
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    // 7. Si el token es válido, creamos un objeto de autenticación.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No necesitamos credenciales (contraseña) aquí
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 8. Establecemos la autenticación en el contexto de seguridad.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si hay algún error con el token, lo registramos pero no interrumpimos la cadena.
            logger.warn("No se pudo procesar la autenticación JWT: " + e.getMessage());
        }

        // 9. Continuamos con el siguiente filtro en la cadena.
        filterChain.doFilter(request, response);
    }
}