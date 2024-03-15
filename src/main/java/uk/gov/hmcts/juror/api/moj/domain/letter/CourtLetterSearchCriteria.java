package uk.gov.hmcts.juror.api.moj.domain.letter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourtLetterSearchCriteria {

    private String jurorNumber;
    private String jurorName;
    private String postcode;
    private String poolNumber;
    private boolean includePrinted;

    public String jurorNumber() {
        return jurorNumber;
    }

    public String jurorName() {
        return jurorName;
    }

    public String postcode() {
        return postcode;
    }

    public String poolNumber() {
        return poolNumber;
    }

    public boolean includePrinted() {
        return includePrinted;
    }
}