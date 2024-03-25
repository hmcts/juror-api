package uk.gov.hmcts.juror.api.moj.repository.letter;

import com.querydsl.core.Tuple;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;

import java.time.LocalDate;

public interface CourtPrintLetterRepository {
    Tuple retrievePrintInformation(String jurorNumber, CourtLetterType courtLetterType, boolean welsh, String owner);

    Tuple retrievePrintInformation(String jurorNumber, CourtLetterType courtLetterType, boolean welsh, String owner,
                                   String trialNumber);

    Tuple retrievePrintInformationBasedOnLetterSpecificDate(String jurorNumber, CourtLetterType letterType,
                                                            boolean welsh, String owner, LocalDate letterDate);
}
