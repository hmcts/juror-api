package uk.gov.hmcts.juror.api.juror.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

/*
 *Composite key for {@Link Messages}
 *
 */
@EqualsAndHashCode
public class MessagesKey implements Serializable {
    private String jurorNumber;
    private String fileDatetime;
    private String userName;
    private String locationCode;
}
