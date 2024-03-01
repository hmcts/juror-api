package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
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
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeCreateDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationJudgeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdministrationJudgeController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        AdministrationJudgeController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + AdministrationJudgeControllerTest.BASE_URL)
public class AdministrationJudgeControllerTest {

    public static final String BASE_URL = "/api/v1/moj/administration/judges";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationJudgeService administrationJudgeService;

    @InjectMocks
    private AdministrationJudgeController administrationJudgeController;

    @Nested
    @DisplayName("GET " + ViewJudgeDetails.URL)
    class ViewJudgeDetails {
        public static final String URL = BASE_URL + "/{judge_id}";

        private String toUrl(long judgeId) {
            return toUrl(String.valueOf(judgeId));
        }

        private String toUrl(String judgeId) {
            return URL.replace("{judge_id}", judgeId);
        }

        @Test
        void positiveTypical() throws Exception {
            JudgeDetailsDto judgeDetailsDto = new JudgeDetailsDto();
            judgeDetailsDto.setJudgeCode("TestJudgeCode");
            when(administrationJudgeService.viewJudge(123L)).thenReturn(judgeDetailsDto);

            mockMvc.perform(get(toUrl(123L)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.judge_code", CoreMatchers.is("TestJudgeCode")))
            ;


            verify(administrationJudgeService, times(1))
                .viewJudge(123L);
            verifyNoMoreInteractions(administrationJudgeService);
        }

        @Test
        void negativeInvalidCodeType() throws Exception {
            mockMvc.perform(get(toUrl("INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationJudgeService);
        }
    }

    @Nested
    @DisplayName("DELETE " + DeleteJudge.URL)
    class DeleteJudge {
        public static final String URL = BASE_URL + "/{judge_id}";

        private String toUrl(long judgeId) {
            return toUrl(String.valueOf(judgeId));
        }

        private String toUrl(String judgeId) {
            return URL.replace("{judge_id}", judgeId);
        }

        @Test
        void positiveTypical() throws Exception {

            mockMvc.perform(delete(toUrl(123L)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());
            verify(administrationJudgeService, times(1))
                .deleteJudge(123L);
            verifyNoMoreInteractions(administrationJudgeService);
        }

        @Test
        void negativeInvalidCodeType() throws Exception {
            mockMvc.perform(delete(toUrl("INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationJudgeService);
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateJudgeDetails.URL)
    class UpdateJudgeDetails {
        public static final String URL = BASE_URL + "/{judge_id}";

        private String toUrl(long judgeId) {
            return toUrl(String.valueOf(judgeId));
        }

        private String toUrl(String judgeId) {
            return URL.replace("{judge_id}", judgeId);
        }

        private JudgeUpdateDto getValidPayload() {
            return JudgeUpdateDto.builder()
                .judgeCode("COD1")
                .judgeName("TestJudgeName")
                .isActive(true)
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            JudgeUpdateDto payload = getValidPayload();

            mockMvc.perform(put(toUrl(123L))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
            ;

            verify(administrationJudgeService, times(1))
                .updateJudge(123L, payload);
            verifyNoMoreInteractions(administrationJudgeService);
        }


        @Test
        void negativeInvalidCodeType() throws Exception {
            mockMvc.perform(put(toUrl("INVALID"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
            ;
            verifyNoMoreInteractions(administrationJudgeService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            JudgeUpdateDto payload = getValidPayload();
            payload.setJudgeCode("LONG1");
            mockMvc.perform(put(toUrl(123L))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
            ;
            verifyNoMoreInteractions(administrationJudgeService);
        }
    }

    @Nested
    @DisplayName("GET " + ViewAllJudgeDetails.URL)
    class ViewAllJudgeDetails {
        public static final String URL = BASE_URL;

        @Test
        void positiveTypicalNoFilter() throws Exception {
            JudgeDetailsDto judgeDetailsDto = new JudgeDetailsDto();
            judgeDetailsDto.setJudgeCode("TestJudgeCode1");


            JudgeDetailsDto judgeDetailsDto2 = new JudgeDetailsDto();
            judgeDetailsDto2.setJudgeCode("TestJudgeCode2");
            when(administrationJudgeService.viewAllJudges(any())).thenReturn(
                List.of(judgeDetailsDto, judgeDetailsDto2));

            mockMvc.perform(get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$[0].judge_code", CoreMatchers.is("TestJudgeCode1")))
                .andExpect(jsonPath("$[1].judge_code", CoreMatchers.is("TestJudgeCode2")))
            ;


            verify(administrationJudgeService, times(1))
                .viewAllJudges(null);
            verifyNoMoreInteractions(administrationJudgeService);
        }

        @Test
        void positiveTypicalWithIsActiveFilter() throws Exception {
            JudgeDetailsDto judgeDetailsDto = new JudgeDetailsDto();
            judgeDetailsDto.setJudgeCode("TestJudgeCode1");


            JudgeDetailsDto judgeDetailsDto2 = new JudgeDetailsDto();
            judgeDetailsDto2.setJudgeCode("TestJudgeCode2");
            when(administrationJudgeService.viewAllJudges(any())).thenReturn(
                List.of(judgeDetailsDto, judgeDetailsDto2));

            mockMvc.perform(get(URL)
                    .queryParam("is_active", "true"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$[0].judge_code", CoreMatchers.is("TestJudgeCode1")))
                .andExpect(jsonPath("$[1].judge_code", CoreMatchers.is("TestJudgeCode2")))
            ;


            verify(administrationJudgeService, times(1))
                .viewAllJudges(true);
            verifyNoMoreInteractions(administrationJudgeService);
        }

        @Test
        void negativeInvalidIsActive() throws Exception {
            mockMvc.perform(get(URL)
                    .queryParam("is_active", "INVALID"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(administrationJudgeService);
        }
    }

    @Nested
    @DisplayName("POST " + CreateJudgeDetails.URL)
    class CreateJudgeDetails {
        public static final String URL = BASE_URL;

        private JudgeCreateDto getValidPayload() {
            return JudgeCreateDto.builder()
                .judgeCode("COD1")
                .judgeName("TestJudgeName")
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            JudgeCreateDto payload = getValidPayload();

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
            ;
            verify(administrationJudgeService, times(1))
                .createJudge(payload);
            verifyNoMoreInteractions(administrationJudgeService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            JudgeCreateDto payload = getValidPayload();
            payload.setJudgeCode("LONG1");
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
            ;
            verifyNoMoreInteractions(administrationJudgeService);
        }
    }
}
