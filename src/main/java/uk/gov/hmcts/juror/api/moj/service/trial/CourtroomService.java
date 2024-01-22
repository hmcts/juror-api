package uk.gov.hmcts.juror.api.moj.service.trial;

import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsListDto;

import java.util.List;

public interface CourtroomService {
    List<CourtroomsListDto> getCourtroomsForLocation(List<String> locCode);
}
