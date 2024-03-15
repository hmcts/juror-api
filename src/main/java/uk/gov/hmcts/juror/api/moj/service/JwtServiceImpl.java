package uk.gov.hmcts.juror.api.moj.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    private final Clock clock;

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Value("${jwt.expiry.bureau}")
    private Long bureauExpiry;

    @Autowired
    public JwtServiceImpl(Clock clock) {
        this.clock = clock;
    }

    private <T> T extractClaim(String jwt, String secret, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractClaims(jwt, secret));
    }

    @Override
    public String extractSubject(String jwt, String secret) {
        return extractClaim(jwt, secret, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String jwt, String secret) {
        return extractClaim(jwt, secret, Claims::getExpiration);
    }

    @Override
    public Claims extractClaims(String jwt, String secret) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        } catch (Exception exception) {
            throw new InvalidJwtAuthenticationException("Failed to parse JWT", exception);
        }
    }

    @Override
    public boolean isJwtExpired(String jwt, String secret) {
        return extractExpiration(jwt, secret).before(new Date(clock.millis()));
    }

    SecretKey getSigningKey(String jwtSecret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    public String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key secretKey,
                                   Map<String, Object> claims) {
        try {
            Date issuedAtDate = new Date(clock.millis());
            return Jwts.builder()
                .id(id)
                .issuer(issuer)
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAtDate)
                .expiration(new Date(issuedAtDate.getTime() + tokenValidity))
                .signWith(secretKey)
                .compact();
        } catch (Exception exception) {
            throw new InvalidJwtAuthenticationException("Failed to parse JWT", exception);
        }
    }

    @Override
    public String generateBureauJwtToken(String id, BureauJWTPayload payload) {
        return generateJwtToken(
            id,
            "juror",
            null,
            bureauExpiry,
            getSigningKey(bureauSecret),
            payload.toClaims()
        );
    }
}
