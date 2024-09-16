package uk.gov.hmcts.juror.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtPayload;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testing utility methods.
 */
@SuppressWarnings("PMD.UseConcurrentHashMap")//False Positive -- Need to support null values
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
                                       final SignatureAlgorithm algorithm, final String base64Key,
                                       final Instant expires) {

        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(Claims.ISSUED_AT, Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
        claimsMap.put("data", dataPayload);

        return Jwts.builder()
            .setClaims(claimsMap)
            .signWith(algorithm, base64Key)
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
                                       final SignatureAlgorithm algorithm, final String base64Key,
                                       final Instant expires) {

        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(Claims.ISSUED_AT, Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
        claimsMap.put("login", payload.getLogin());
        claimsMap.put("owner", payload.getOwner());
        claimsMap.put("locCode", payload.getLocCode());
        claimsMap.put("userLevel", payload.getUserLevel());
        claimsMap.put("staff", payload.getStaff());

        claimsMap.put("roles", payload.getRoles());
        claimsMap.put("permissions", payload.getPermissions());
        claimsMap.put("userType", payload.getUserType());
        claimsMap.put("activeUserType", payload.getActiveUserType() == null
            ? payload.getUserType() : payload.getActiveUserType());

        return Jwts.builder()
            .setClaims(claimsMap)
            .signWith(algorithm, base64Key)
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
    public static String mintHmacJwt(final SignatureAlgorithm algorithm, final String base64Key,
                                     final Instant expires) {
        final Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(Claims.EXPIRATION, Date.from(expires));
        claimsMap.put(Claims.ISSUED_AT, Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));

        return Jwts.builder()
            .setClaims(claimsMap)
            .signWith(algorithm, base64Key)
            .compact();
    }

    public static List<String> getValuesInJsonObject(JSONObject jsonObject, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (String currentKey : jsonObject.keySet()) {
            Object value = jsonObject.get(currentKey);
            if (currentKey.equals(key)) {
                accumulatedValues.add(value.toString());
            }

            if (value instanceof JSONObject) {
                accumulatedValues.addAll(getValuesInJsonObject((JSONObject) value, key));
            } else if (value instanceof JSONArray) {
                accumulatedValues.addAll(getValuesInJsonArray((JSONArray) value, key));
            }
        }
        return accumulatedValues;
    }

    public static List<String> getValuesInJsonArray(JSONArray jsonArray, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (Object obj : jsonArray) {
            if (obj instanceof JSONArray) {
                accumulatedValues.addAll(getValuesInJsonArray((JSONArray) obj, key));
            } else if (obj instanceof JSONObject) {
                accumulatedValues.addAll(getValuesInJsonObject((JSONObject) obj, key));
            }
        }
        return accumulatedValues;
    }

    public static String getJsonNthValue(JSONObject jsonObject, String key, int index) {
        List<String> values = getValuesInJsonObject(jsonObject, key);
        return values.size() >= index
            ? values.get(index - 1) : null;
    }
}
