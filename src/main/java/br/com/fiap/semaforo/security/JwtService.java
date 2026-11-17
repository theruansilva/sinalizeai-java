package br.com.fiap.semaforo.security;

import br.com.fiap.semaforo.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey chave;
    private final Duration validade;

    public JwtService(@Value("${jwt.secret:}") String segredo, @Value("${jwt.expiration:PT8H}") Duration validade) {
        if (segredo.isBlank()) {
            log.warn("JWT_SECRET não definido; usando chave aleatória gerada na inicialização");
            this.chave = Jwts.SIG.HS256.key().build();
        } else {
            this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        }
        this.validade = validade;
    }

    public String gerar(Usuario u) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(u.getEmail())
                .claim("papel", u.getPapel().name())
                .issuer("semaforo-api")
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave)
                .compact();
    }

    public Optional<String> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(chave).requireIssuer("semaforo-api").build()
                    .parseSignedClaims(token).getPayload();
            return Optional.ofNullable(c.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long validadeSegundos() {
        return validade.toSeconds();
    }
}
