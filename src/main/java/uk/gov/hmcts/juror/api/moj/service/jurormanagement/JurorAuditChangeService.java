package uk.gov.hmcts.juror.api.moj.service.jurormanagement;


import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;

import java.util.Map;

public interface JurorAuditChangeService {


    Map<String, Boolean> initChangedPropertyMap(Juror juror,
                                                JurorNameDetailsDto jurorNameDetailsDto);

    Map<String, Boolean> initChangedPropertyMap(Juror juror,
                                                AbstractJurorResponse jurorResponse);

    boolean hasTitleChanged(String updatedTitle, String originalTitle);

    boolean hasNameChanged(String updatedFirstName, String originalFirstname,
                           String updatedLastName, String originalLastname);

    void recordApprovalHistoryEvent(String jurorNumber, ApprovalDecision approvalDecision,
                                    String auditorUsername, String poolNumber);

    void recordContactLog(Juror juror, String auditorUsername,
                          String contactEnquiryCode, String notes);

    void recordPersonalDetailsHistory(String propertyName, Juror juror, String poolNumber, String auditorUsername);

}
