package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;

import java.time.LocalDate;

public interface StraightThroughProcessorService {

    boolean isValidForStraightThroughAcceptance(String jurorNumber, String owner, boolean canServeOnSummonsDate);

    void processAgeDisqualification(PaperResponse paperResponse, LocalDate returnDate, JurorPool poolMember,
                                    BureauJwtPayload payload);

    void processAgeDisqualification(DigitalResponse jurorDigitalResponse, JurorPool poolMember,
                                    BureauJwtPayload payload);

    boolean isValidForStraightThroughAgeDisqualification(PaperResponse paperResponse, LocalDate returnDate,
                                                         JurorPool poolMember);

    boolean isValidForStraightThroughAgeDisqualification(DigitalResponse jurorDigitalResponse,
                                                         LocalDate returnDate, JurorPool poolMember);
}
