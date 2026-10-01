package com.example.demo.config.jwt;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.user.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final Jwt jwt;

    /**
     * Genera un token JWT para un nombre de usuario básico.
     */
    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    /**
     * Genera un token JWT para un nombre de usuario con claims adicionales.
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwt.getExpirationInMillis());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(jwt.getSigningKey())
                .compact();
    }

    /**
     * Genera un token JWT a partir de un objeto UserDetails de Spring Security.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", userDetails.getAuthorities());
        return generateToken(userDetails.getUsername(), extraClaims);
    }

    /**
     * Genera un token JWT a partir de un objeto Authentication de Spring Security.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    /**
     * Genera un token JWT a partir de la entidad User de la aplicación.
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        if (user.getRole() != null) {
            extraClaims.put("role", user.getRole().name());
        }
        if (user.getEmail() != null) {
            extraClaims.put("email", user.getEmail());
        }
        return generateToken(user.getUsername(), extraClaims);
    }

    /**
     * Extrae el nombre de usuario (subject) contenido en el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token utilizando una función extractor.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims contenidos en el token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwt.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Valida si el token corresponde al usuario y no ha expirado.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Valida la firma del token y comprueba si no ha expirado.
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Comprueba si el token ya ha expirado según la fecha actual.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}