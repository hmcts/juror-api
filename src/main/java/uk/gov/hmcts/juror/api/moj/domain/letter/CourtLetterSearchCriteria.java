package uk.gov.hmcts.juror.api.moj.domain.letter;

import lombok.Builder;

@Builder
public record CourtLetterSearchCriteria(String jurorNumber, String jurorName, String postcode,
                                        String poolNumber, boolean includePrinted) {
}