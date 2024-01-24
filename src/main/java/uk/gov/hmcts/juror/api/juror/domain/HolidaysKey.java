package uk.gov.hmcts.juror.api.juror.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode
public class HolidaysKey implements Serializable {
    private Date holiday;
    private String owner;

}
