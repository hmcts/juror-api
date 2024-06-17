package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.FailedToAttendListResponse;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;

import java.util.List;

public interface SjoTasksService {

    PaginatedList<FailedToAttendListResponse> search(JurorPoolSearch request);

    void undoFailedToAttendStatus(String jurorNumber, String poolNumber);

}
