package uk.gov.hmcts.juror.api.moj.repository.letter;

import com.querydsl.core.Tuple;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;

public interface CourtPrintLetterRepository {
    Tuple retrievePrintInformation(String jurorNumber, CourtLetterType courtLetterType, boolean welsh, String owner);
}
