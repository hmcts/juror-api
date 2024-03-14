package uk.gov.hmcts.juror.api.moj.service;

import io.jsonwebtoken.Claims;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public interface JwtService {

    String extractSubject(String jwt,String secret);


    Claims extractClaims(String jwt, String secret);

    Date extractExpiration(String jwt,String secret);

    boolean isJwtExpired(String jwt, String secret);

    String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key secretKey,
                            Map<String, Object> claims);

    String generateBureauJwtToken(String id, BureauJWTPayload payload);
}
