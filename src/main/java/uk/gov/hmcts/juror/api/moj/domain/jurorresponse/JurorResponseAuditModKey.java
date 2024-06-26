package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode
public class JurorResponseAuditModKey implements Serializable {
    private String jurorNumber;
    private LocalDateTime changed;
}
