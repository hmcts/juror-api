package uk.gov.hmcts.juror.api.moj.domain.messages;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode
public class MessageKey implements Serializable {
    private String jurorNumber;
    private LocalDateTime fileDatetime;
    private String userName;
    private String locationCode;
}