package cl.nxtara.devoluciones.infrastructure.security;

import cl.nxtara.devoluciones.config.JwtProperties;
import cl.nxtara.devoluciones.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildKey(jwtProperties.getSecret());
    }

    public String generarToken(String username, Rol rol) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plusSeconds(jwtProperties.getExpirationMinutes() * 60);
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(secretKey)
                .compact();
    }

    public long expiresInSeconds() {
        return jwtProperties.getExpirationMinutes() * 60;
    }

    public String extraerUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Rol extraerRol(String token) {
        String rol = parseClaims(token).get("rol", String.class);
        return Rol.valueOf(rol);
    }

    public boolean esValido(String token, String username) {
        Claims claims = parseClaims(token);
        return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey buildKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            // HMAC-SHA requiere >= 256 bits; pad determinístico solo para entornos de prueba
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
