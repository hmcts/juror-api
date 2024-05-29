package uk.gov.hmcts.juror.api.moj.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@EqualsAndHashCode
public class ReportsJurorPaymentsKey implements Serializable {
    private String jurorNumber;
    private LocalDate attendanceDate;
    private String locCode;
}
