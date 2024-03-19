package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomWithIdDto;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationCourtRoomService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdministrationCourtRoomController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        AdministrationCourtRoomController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + AdministrationCourtRoomControllerTest.BASE_URL)
public class AdministrationCourtRoomControllerTest {

    public static final String BASE_URL = "/api/v1/moj/administration/court-rooms";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationCourtRoomService administrationCourtRoomService;

    @InjectMocks
    private AdministrationCourtRoomController administrationCourtRoomController;


    private CourtRoomDto getValidCourtRoomDto() {
        return CourtRoomDto.builder()
            .roomName(TestConstants.VALID_COURT_ROOM_NAME)
            .roomDescription(TestConstants.VALID_COURT_ROOM_DESC)
            .build();
    }

    private CourtRoomWithIdDto getValidCourtRoomWithIdDto() {
        return CourtRoomWithIdDto.builder()
            .id(1L)
            .roomName(TestConstants.VALID_COURT_ROOM_NAME)
            .roomDescription(TestConstants.VALID_COURT_ROOM_DESC)
            .build();
    }

    @Nested
    @DisplayName("GET " + ViewCourtRoomsDetails.URL)
    class ViewCourtRoomsDetails {
        public static final String URL = BASE_URL + "/{loc_code}";

        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }


        @Test
        void positiveTypical() throws Exception {
            CourtRoomWithIdDto courtRoomDto = getValidCourtRoomWithIdDto();
            doReturn(List.of(courtRoomDto)).when(administrationCourtRoomService).viewCourtRooms(any());

            mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(List.of(courtRoomDto))));

            verify(administrationCourtRoomService, times(1))
                .viewCourtRooms(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(get(toUrl(TestConstants.INVALID_COURT_LOCATION)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(administrationCourtRoomService);
        }
    }


    @Nested
    @DisplayName("POST " + CreateCourtRoom.URL)
    class CreateCourtRoom {
        public static final String URL = BASE_URL + "/{loc_code}";


        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }


        @Test
        void positiveTypical() throws Exception {
            CourtRoomDto courtRoomDto = getValidCourtRoomDto();

            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courtRoomDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(administrationCourtRoomService, times(1))
                .createCourtRoom(TestConstants.VALID_COURT_LOCATION, courtRoomDto);
            verifyNoMoreInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(post(toUrl(TestConstants.INVALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidCourtRoomDto())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            CourtRoomDto payload = getValidCourtRoomDto();
            payload.setRoomName(null);
            mockMvc.perform(post(toUrl(TestConstants.INVALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationCourtRoomService);
        }
    }

    @Nested
    @DisplayName("GET " + ViewCourtRoomDetails.URL)
    class ViewCourtRoomDetails {
        public static final String URL = BASE_URL + "/{loc_code}/{id}";

        private String toUrl(String locCode, String id) {
            return URL.replace("{loc_code}", locCode)
                .replace("{id}", id);
        }


        @Test
        void positiveTypical() throws Exception {
            CourtRoomWithIdDto courtRoomDto = getValidCourtRoomWithIdDto();
            doReturn(courtRoomDto).when(administrationCourtRoomService).viewCourtRoom(any(), any());

            mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, "1")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(courtRoomDto)));

            verify(administrationCourtRoomService, times(1))
                .viewCourtRoom(TestConstants.VALID_COURT_LOCATION, 1L);
            verifyNoMoreInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(get(toUrl(TestConstants.INVALID_COURT_LOCATION, "1")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidId() throws Exception {
            mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, "INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(administrationCourtRoomService);
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateCourtRoom.URL)
    class UpdateCourtRoom {
        public static final String URL = BASE_URL + "/{loc_code}/{id}";

        private String toUrl(String locCode, String id) {
            return URL.replace("{loc_code}", locCode)
                .replace("{id}", id);
        }


        @Test
        void positiveTypical() throws Exception {
            CourtRoomDto courtRoomDto = getValidCourtRoomDto();

            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION, "1"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courtRoomDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(administrationCourtRoomService, times(1))
                .updateCourtRoom(TestConstants.VALID_COURT_LOCATION, 1L, courtRoomDto);
            verifyNoMoreInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(put(toUrl(TestConstants.INVALID_COURT_LOCATION, "1"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidCourtRoomDto())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidId() throws Exception {
            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION, "INVALID"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidCourtRoomDto())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(administrationCourtRoomService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            CourtRoomDto courtRoomDto = getValidCourtRoomDto();
            courtRoomDto.setRoomName(null);
            mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION, "1"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courtRoomDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoInteractions(administrationCourtRoomService);
        }
    }
}
