package com.flamedavid.eurovision.security;

import com.flamedavid.eurovision.entities.User;
import com.flamedavid.eurovision.exceptions.UnauthorizedException;
import com.flamedavid.eurovision.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Estrai e verifica l'utente dal token
        UUID userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(
            () -> new UnauthorizedException("Token non valido")
        );

        // Verifica la firma e scadenza del token
        if (!jwtUtil.isTokenValid(token, user)) {
            throw new UnauthorizedException("Token non valido");
        }

        // Autentica l’utente
        var auth = new UsernamePasswordAuthenticationToken(
            user,
            null,
            List.of(new SimpleGrantedAuthority(user.isAdmin() ? "ADMIN" : "USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }

}
