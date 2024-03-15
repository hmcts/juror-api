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
import uk.gov.hmcts.juror.api.moj.domain.authentication.CourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.EmailDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthenticationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        AuthenticationController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + AuthenticationControllerTest.BASE_URL)
public class AuthenticationControllerTest {

    public static final String BASE_URL = "/api/v1/auth/moj";

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private UserService userService;
    @InjectMocks
    private AuthenticationController authenticationController;

    @Nested
    @DisplayName("POST " + ViewCourts.URL)
    class ViewCourts {
        public static final String URL = BASE_URL + "/courts";

        @Test
        void positiveTypical() throws Exception {
            List<CourtDto> courts = List.of(
                new CourtDto("1", "Court 1", CourtType.MAIN),
                new CourtDto("2", "Court 2", CourtType.SATELLITE));

            doReturn(courts).when(userService).getCourts(any());

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(new EmailDto(TestConstants.VALID_EMAIL))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(courts)));

            verify(userService, times(1))
                .getCourts(TestConstants.VALID_EMAIL);
            verifyNoMoreInteractions(userService);
        }


        @Test
        void negativeInvalidPayload() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(new EmailDto(""))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET " + CreateJwt.URL)
    class CreateJwt {
        public static final String URL = BASE_URL + "/jwt/{loc_code}";

        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        @Test
        void positiveTypical() throws Exception {
            JwtDto jwtDto = new JwtDto(TestConstants.JWT);
            doReturn(jwtDto).when(userService).createJwt(any(), any());

            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(new EmailDto(TestConstants.VALID_EMAIL))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(jwtDto)));

            verify(userService, times(1))
                .createJwt(TestConstants.VALID_EMAIL, TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(userService);
        }


        @Test
        void negativeInvalidPayload() throws Exception {
            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(new EmailDto(""))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(post(toUrl(TestConstants.INVALID_COURT_LOCATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(new EmailDto(TestConstants.VALID_EMAIL))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }
}
