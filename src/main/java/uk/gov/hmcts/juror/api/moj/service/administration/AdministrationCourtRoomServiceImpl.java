package uk.gov.hmcts.juror.api.moj.service.administration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomWithIdDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class AdministrationCourtRoomServiceImpl implements AdministrationCourtRoomService {

    private final CourtroomRepository courtroomRepository;
    private final CourtLocationRepository courtLocationRepository;


    @Override
    @Transactional
    public void createCourtRoom(String locCode, CourtRoomDto courtRoomDto) {
        CourtLocation courtLocation = getCourtLocation(locCode);
        courtroomRepository.save(Courtroom.builder()
            .courtLocation(courtLocation)
            .roomNumber(courtRoomDto.getRoomName())
            .description(courtRoomDto.getRoomDescription())
            .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtRoomWithIdDto> viewCourtRooms(String locCode) {
        return courtroomRepository.findByCourtLocationLocCode(locCode).stream()
            .map(CourtRoomWithIdDto::new)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourtRoomWithIdDto viewCourtRoom(String locCode, Long id) {
        return new CourtRoomWithIdDto(getCourtRoom(locCode, id));
    }

    @Override
    @Transactional
    public void updateCourtRoom(String locCode, Long id, CourtRoomDto courtRoomDto) {
        Courtroom courtroom = getCourtRoom(locCode, id);
        courtroom.setRoomNumber(courtRoomDto.getRoomName());
        courtroom.setDescription(courtRoomDto.getRoomDescription());
        courtroomRepository.save(courtroom);
    }

    CourtLocation getCourtLocation(String locCode) {
        return courtLocationRepository.findByLocCode(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court location not found", null));
    }


    Courtroom getCourtRoom(String locCode, Long id) {
        return courtroomRepository.findByCourtLocationLocCodeAndId(locCode, id)
            .orElseThrow(() -> new MojException.NotFound("Court room not found", null));
    }
}
