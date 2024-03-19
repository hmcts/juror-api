package uk.gov.hmcts.juror.api.moj.service.administration;

import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomWithIdDto;

import java.util.List;

public interface AdministrationCourtRoomService {
    void createCourtRoom(String locCode, CourtRoomDto courtRoomDto);

    List<CourtRoomWithIdDto> viewCourtRooms(String locCode);

    CourtRoomWithIdDto viewCourtRoom(String locCode, Long id);

    void updateCourtRoom(String locCode, Long id, CourtRoomDto courtRoomDto);
}
