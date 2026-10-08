package uk.gov.hmcts.juror.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.jurorer.JurorErJwtPayload;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtPayload;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * Testing utility methods.
 */
@SuppressWarnings("PMD.UseConcurrentHashMap") // False Positive -- Need to support null values
public final class TestUtil {

    private TestUtil() {

    }

    /**
     * Parse an Object to JSON byte array using Jackson.
     *
     * @param object Object to parse to JSON
     * @return JSON byte array
     */
    public static byte[] parseToJsonBytes(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }

    /**
     * Parse an Object to JSON string using Jackson.
     *
     * @param object Object to parse to JSON
     * @return JSON String
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String parseToJsonString(final Object object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    /**
     * Mint a fresh JWT token for the Juror Public endpoints for use in tests.
     *
     * @param dataPayload Payload content under the "data" claim
     * @param algorithm   Encryption algorithm
     * @param base64Key   Secret key
     * @param expires     Expiry date
     * @return Json Web Token
     */
    public static String mintPublicJwt(final PublicJwtPayload dataPayload,
                                       final MacAlgorithm algorithm,
                                       final String base64Key,
                                       final Instant expires) {

        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(
            Claims.ISSUED_AT,
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant())
        );
        claimsMap.put("data", dataPayload);

        return Jwts.builder()
            .claims(claimsMap)
            .signWith(getSigningKey(base64Key), algorithm)
            .compact();
    }

    /**
     * Mint a fresh JWT token for the Juror Bureau endpoints for use in tests.
     *
     * @param payload   Payload content entered into the claims by field name
     * @param algorithm Encryption algorithm
     * @param base64Key Secret key
     * @param expires   Expiry date
     * @return Json Web Token
     */
    public static String mintBureauJwt(final BureauJwtPayload payload,
                                       final MacAlgorithm algorithm,
                                       final String base64Key,
                                       final Instant expires) {

        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(
            Claims.ISSUED_AT,
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant())
        );
        claimsMap.put("login", payload.getLogin());
        claimsMap.put("owner", payload.getOwner());
        claimsMap.put("locCode", payload.getLocCode());
        claimsMap.put("userLevel", payload.getUserLevel());
        claimsMap.put("staff", payload.getStaff());

        claimsMap.put("roles", payload.getRoles());
        claimsMap.put("permissions", payload.getPermissions());
        claimsMap.put("userType", payload.getUserType());
        claimsMap.put(
            "activeUserType",
            payload.getActiveUserType() == null
                ? payload.getUserType()
                : payload.getActiveUserType()
        );

        return Jwts.builder()
            .claims(claimsMap)
            .signWith(getSigningKey(base64Key), algorithm)
            .compact();
    }

    /**
     * Mint a fresh JWT token for the Juror login endpoints only.
     *
     * @param algorithm Encryption algorithm
     * @param base64Key Secret key
     * @param expires   Expiry date
     * @return Json Web Token
     */
    public static String mintHmacJwt(final MacAlgorithm algorithm,
                                     final String base64Key,
                                     final Instant expires) {

        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(
            Claims.ISSUED_AT,
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant())
        );

        return Jwts.builder()
            .claims(claimsMap)
            .signWith(getSigningKey(base64Key), algorithm)
            .compact();
    }

    /**
     * Mint a fresh JWT token for the Juror ER portal endpoints for use in tests.
     *
     * @param dataPayload Payload content under the "data" claim
     * @param algorithm   Encryption algorithm
     * @param base64Key   Secret key
     * @param expires     Expiry date
     * @return Json Web Token
     */
    public static String mintJurorErJwt(final JurorErJwtPayload dataPayload,
                                        final MacAlgorithm algorithm,
                                        final String base64Key,
                                        final Instant expires) {

        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(
            Claims.ISSUED_AT,
            Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant())
        );
        claimsMap.put("username", dataPayload.getUsername());
        claimsMap.put("laCode", dataPayload.getLaCode());
        claimsMap.put("laName", dataPayload.getLaName());
        claimsMap.put("role", dataPayload.getRoles());

        return Jwts.builder()
            .claims(claimsMap)
            .signWith(getSigningKey(base64Key), algorithm)
            .compact();
    }

    /**
     * Creates an HMAC signing key from a Base64 encoded secret.
     *
     * @param base64Key Base64 encoded secret key
     * @return HMAC secret key
     */
    private static SecretKey getSigningKey(final String base64Key) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Key));
    }

    public static List<String> getValuesInJsonObject(JSONObject jsonObject, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (String currentKey : jsonObject.keySet()) {
            Object value = jsonObject.get(currentKey);
            if (currentKey.equals(key)) {
                accumulatedValues.add(value.toString());
            }

            if (value instanceof JSONObject object) {
                accumulatedValues.addAll(getValuesInJsonObject(object, key));
            } else if (value instanceof JSONArray array) {
                accumulatedValues.addAll(getValuesInJsonArray(array, key));
            }
        }
        return accumulatedValues;
    }

    public static List<String> getValuesInJsonArray(JSONArray jsonArray, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (Object obj : jsonArray) {
            if (obj instanceof JSONArray array) {
                accumulatedValues.addAll(getValuesInJsonArray(array, key));
            } else if (obj instanceof JSONObject object) {
                accumulatedValues.addAll(getValuesInJsonObject(object, key));
            }
        }
        return accumulatedValues;
    }

    public static String getJsonNthValue(JSONObject jsonObject, String key, int index) {
        List<String> values = getValuesInJsonObject(jsonObject, key);
        return values.size() >= index
            ? values.get(index - 1)
            : null;
    }
}
