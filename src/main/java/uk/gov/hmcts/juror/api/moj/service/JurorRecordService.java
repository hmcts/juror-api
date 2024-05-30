package uk.gov.hmcts.juror.api.moj.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.ConfirmIdentityDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ContactLogRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorOpticRefRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorRecordFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoliceCheckStatusDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessNameChangeRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessPendingJurorRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.UpdateAttendanceRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactEnquiryTypeListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactLogListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAttendanceDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorNotesDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorOverviewResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorRecordSearchDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorSummonsReplyResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorPaymentsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterJurorRecord;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;

public interface JurorRecordService {

    JurorDetailsResponseDto getJurorDetails(BureauJwtPayload payload, String jurorNumber, String locCode);

    FilterableJurorDetailsResponseDto getJurorDetails(FilterableJurorDetailsRequestDto request);

    JurorOverviewResponseDto getJurorOverview(BureauJwtPayload payload, String jurorNumber, String locCode);

    JurorRecordSearchDto searchJurorRecord(BureauJwtPayload payload, String jurorNumber);

    ContactLogListDto getJurorContactLogs(BureauJwtPayload payload, String jurorNumber);

    void createJurorContactLog(BureauJwtPayload payload, ContactLogRequestDto contactLogRequestDto);

    ContactEnquiryTypeListDto getContactEnquiryTypes();

    JurorNotesDto getJurorNotes(String jurorNumber, String owner);

    void setJurorNotes(String jurorNumber, String notes, String owner);

    void createJurorOpticReference(BureauJwtPayload payload, JurorOpticRefRequestDto opticsRefRequestDto);

    String getJurorOpticReference(String jurorNumber, String poolNumber, BureauJwtPayload payload);

    JurorBankDetailsDto getJurorBankDetails(String jurorNumber);

    BureauJurorDetailDto getBureauDetailsByJurorNumber(String jurorNumber, String owner);

    JurorSummonsReplyResponseDto getJurorSummonsReply(BureauJwtPayload payload, String jurorNumber, String locCode);

    @Transactional
    void setPendingNameChange(Juror juror, String pendingTitle,
                              String pendingFirstName, String pendingLastName);

    void fixErrorInJurorName(BureauJwtPayload payload, String jurorNumber, JurorNameDetailsDto jurorNameDetailsDto);

    void processPendingNameChange(BureauJwtPayload payload, String jurorNumber, ProcessNameChangeRequestDto requestDto);

    void editJurorDetails(BureauJwtPayload payload, EditJurorRecordRequestDto requestDto, String jurorNumber);

    PoliceCheckStatusDto updatePncStatus(String jurorNumber, PoliceCheck policeCheck);

    void createJurorRecord(BureauJwtPayload payload, JurorCreateRequestDto jurorCreateRequestDto);

    void updateJurorToFailedToAttend(String jurorNumber, String poolNumber);

    void undoUpdateJurorToFailedToAttend(String jurorNumber, String poolNumber);

    JurorAttendanceDetailsResponseDto getJurorAttendanceDetails(String locCode,
                                                                String jurorNumber,
                                                                BureauJwtPayload payload);

    JurorPaymentsResponseDto getJurorPayments(String jurorNumber, BureauJwtPayload payload);

    PendingJurorsResponseDto getPendingJurors(String locCode, PendingJurorStatus status);

    void processPendingJuror(ProcessPendingJurorRequestDto processPendingJurorRequestDto);

    void updateAttendance(UpdateAttendanceRequestDto dto);

    void editJurorsBankDetails(RequestBankDetailsDto dto);

    void confirmIdentity(ConfirmIdentityDto dto);

    void markResponded(String jurorNumber);

    PaginatedList<FilterJurorRecord> searchForJurorRecords(JurorRecordFilterRequestQuery query);
}
