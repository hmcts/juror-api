package uk.gov.hmcts.juror.api.utils;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

public final class DataConversionUtil {

    private DataConversionUtil() {
        //Empty private constructor
    }

    public static JSONObject getExceptionDetails(ResponseEntity<String> responseEntity) {
        return new JSONObject(responseEntity.getBody());
    }
}
