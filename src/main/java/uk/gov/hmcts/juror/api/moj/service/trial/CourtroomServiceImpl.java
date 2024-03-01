package uk.gov.hmcts.juror.api.moj.service.trial;

import com.querydsl.core.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsListDto;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class CourtroomServiceImpl implements CourtroomService {
    @Autowired
    private CourtroomRepository courtroomRepository;

    @Override
    public List<CourtroomsListDto> getCourtroomsForLocation(List<String> courts) {
        List<CourtroomsListDto> courtroomsListDto = new ArrayList<>();
        List<Tuple> courtRooms = courtroomRepository.getCourtroomsForLocation(courts);
        HashMap<String, List<CourtroomsDto>> courtroomsHashmap = new HashMap<>();
        for (Tuple courtroom : courtRooms) {
            initialiseHashMap(courtroomsHashmap,courtroom.get(5, String.class));
            CourtroomsDto dto = createCourtroomsDto(
                courtroom.get(0, Long.class),
                courtroom.get(1, String.class),
                courtroom.get(2, String.class),
                courtroom.get(3, String.class),
                courtroom.get(4, String.class)
            );
            courtroomsHashmap.get(courtroom.get(5, String.class)).add(dto);
        }

        courtroomsHashmap.forEach((k, v) -> {
            CourtroomsListDto dto = createCourtroomsListDto(k);
            dto.setCourtRooms(v);
            courtroomsListDto.add(dto);
        });
        return courtroomsListDto;
    }

    private static void initialiseHashMap(AbstractMap<String, List<CourtroomsDto>> courtroomsHashmap,
                                          String courtLocation) {
        courtroomsHashmap.putIfAbsent(courtLocation, new ArrayList<>());
    }

    private static CourtroomsListDto createCourtroomsListDto(String courtLocation) {
        CourtroomsListDto dto = new CourtroomsListDto();
        dto.setCourtLocation(courtLocation);
        return dto;
    }

    private static CourtroomsDto createCourtroomsDto(Long id, String owner, String locCode, String description,
                                                     String roomNumber) {
        CourtroomsDto dto = new CourtroomsDto();
        dto.setId(id);
        dto.setRoomNumber(roomNumber);
        dto.setDescription(description);
        dto.setOwner(owner);
        dto.setLocCode(locCode);
        return dto;
    }
}
