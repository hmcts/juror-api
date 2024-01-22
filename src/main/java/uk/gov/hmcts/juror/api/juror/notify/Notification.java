package uk.gov.hmcts.juror.api.juror.notify;

import lombok.Data;

import java.util.Map;

@Data
abstract class Notification {
    protected String templateId;
    protected String referenceNumber;
    protected Map<String, String> payload;
}
