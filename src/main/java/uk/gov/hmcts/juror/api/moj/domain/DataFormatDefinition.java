package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataFormatDefinition {

    private int sequenceNumber;
    private int positionFrom;
    private int positionTo;
    private int length;

    public DataFormatDefinition(int sequenceNumber, int positionFrom, int positionTo, int length) {
        this.sequenceNumber = sequenceNumber;
        this.positionFrom = positionFrom;
        this.positionTo = positionTo;
        this.length = length;
    }
}
