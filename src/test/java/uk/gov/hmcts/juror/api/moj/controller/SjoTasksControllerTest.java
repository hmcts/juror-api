package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.SjoTasksService;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SjoTasksController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        SjoTasksController.class,
        RestResponseEntityExceptionHandler.class,
        BulkServiceImpl.class,
        JurorPoolService.class,
    }
)
@DisplayName("Controller: " + SjoTasksControllerTest.BASE_URL)
class SjoTasksControllerTest {
    public static final String BASE_URL = "/api/v1/moj/sjo-tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BulkService bulkService;

    @MockBean
    private SjoTasksService sjoTasksService;

    @MockBean
    private JurorPoolService jurorPoolService;

    @Nested
    @DisplayName("POST " + GetFailedToAttendJurors.URL)
    class GetFailedToAttendJurors {
        public static final String URL = BASE_URL + "/juror/search";

        @Test
        void positiveTypicalPoolSearch() throws Exception {
            JurorPoolSearch jurorPoolSearch = JurorPoolSearch.builder()
                .poolNumber("415")
                .jurorStatus(IJurorStatus.FAILED_TO_ATTEND)
                .pageLimit(5)
                .pageNumber(1)
                .build();

            JurorDetailsDto response1 = JurorDetailsDto.builder()
                .jurorNumber("111111111")
                .build();
            JurorDetailsDto response2 = JurorDetailsDto.builder()
                .jurorNumber("111111112")
                .build();
            JurorDetailsDto response3 = JurorDetailsDto.builder()
                .jurorNumber("111111113")
                .build();

            PaginatedList<JurorDetailsDto> result = new PaginatedList<>();
            result.setData(List.of(response1, response2, response3));

            when(jurorPoolService.search(jurorPoolSearch)).thenReturn(result);

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(jurorPoolSearch)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.size()", CoreMatchers.is(3)))
                .andExpect(jsonPath("$.data.[0].juror_number", CoreMatchers.is("111111111")))
                .andExpect(jsonPath("$.data.[1].juror_number", CoreMatchers.is("111111112")))
                .andExpect(jsonPath("$.data.[2].juror_number", CoreMatchers.is("111111113")));

            verify(jurorPoolService, times(1)).search(jurorPoolSearch);
            verifyNoMoreInteractions(jurorPoolService);
        }

        @Test
        void negativeBadPayload() throws Exception {
            // Missing pool number
            JurorPoolSearch jurorPoolSearch = JurorPoolSearch.builder()
                .pageLimit(5)
                .pageNumber(1)
                .build();

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(jurorPoolSearch)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verifyNoMoreInteractions(sjoTasksService);
        }
    }

    @Nested
    @DisplayName("PATCH " + UndoFailedToAttendJurors.URL)
    class UndoFailedToAttendJurors {
        public static final String URL = BASE_URL + "/failed-to-attend/undo";

        @Test
        void positiveTypical() throws Exception {
            JurorAndPoolRequest request = JurorAndPoolRequest.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();

            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(List.of(request))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(sjoTasksService, times(1))
                .undoFailedToAttendStatus(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);
        }

        @Test
        void positiveMultiple() throws Exception {
            JurorAndPoolRequest request1 = JurorAndPoolRequest.builder()
                .jurorNumber("111111111")
                .poolNumber("111111111")
                .build();
            JurorAndPoolRequest request2 = JurorAndPoolRequest.builder()
                .jurorNumber("111111112")
                .poolNumber("111111112")
                .build();
            JurorAndPoolRequest request3 = JurorAndPoolRequest.builder()
                .jurorNumber("111111113")
                .poolNumber("111111113")
                .build();

            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(List.of(request1, request2, request3))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted());

            verify(sjoTasksService, times(1)).undoFailedToAttendStatus("111111111", "111111111");
            verify(sjoTasksService, times(1)).undoFailedToAttendStatus("111111112", "111111112");
            verify(sjoTasksService, times(1)).undoFailedToAttendStatus("111111113", "111111113");

            verifyNoMoreInteractions(sjoTasksService);
        }

        @Test
        void negativeBadRequest() throws Exception {
            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(List.of())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
        }
    }

}
