package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReasonableAdjustmentDetailsDto;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.JurorPaperResponseService;
import uk.gov.hmcts.juror.api.moj.service.StraightThroughProcessorService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = JurorPaperResponseController.class, excludeAutoConfiguration =
    {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = { JurorPaperResponseController.class, RestResponseEntityExceptionHandler.class})
@SuppressWarnings("PMD.LawOfDemeter")
class JurorPaperResponseControllerTest {

    private static final String OWNER_BUREAU = "400";
    private static final String USERNAME_BUREAU = "BUREAU_USER";
    private static final String VALID_JUROR_NUMBER = "123456789";
    private static final String UPDATE_PAPER_SPECIAL_NEEDS_URI =
        "/api/v1/moj/juror-paper-response/juror/{jurorNumber}/details/special-needs";
    private static final String ASSISTANCE_TYPE_D = "D";
    private static final String ASSISTANCE_TYPE_DETAILS_D = "Diet";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JurorPaperResponseService jurorPaperResponseService;

    @MockBean
    private StraightThroughProcessorService straightThroughProcessorService;

    @MockBean
    private SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    private BureauJWTPayload bureauJwtPayload;


    @Test
    void testUpdatePaperSpecialNeedsDetailsHappyPath() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        List<JurorPaperResponseDto.ReasonableAdjustment> mockReasonableAdjustmentList = new ArrayList<>();
        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment = JurorPaperResponseDto.ReasonableAdjustment
            .builder().assistanceType(ASSISTANCE_TYPE_D).assistanceTypeDetails(ASSISTANCE_TYPE_DETAILS_D).build();
        mockReasonableAdjustmentList.add(reasonableAdjustment);

        ReasonableAdjustmentDetailsDto mockReasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        mockReasonableAdjustmentDetailsDto.setReasonableAdjustments(mockReasonableAdjustmentList);

        Mockito.doNothing().when(jurorPaperResponseService).updateReasonableAdjustmentsDetails(bureauJwtPayload,
            mockReasonableAdjustmentDetailsDto, VALID_JUROR_NUMBER);

        mockMvc.perform(MockMvcRequestBuilders.patch(
            UPDATE_PAPER_SPECIAL_NEEDS_URI, VALID_JUROR_NUMBER)
            .principal(mockPrincipal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.asJsonString(mockReasonableAdjustmentDetailsDto)))
            .andExpect(status().isNoContent());

        verify(jurorPaperResponseService, times(1)).updateReasonableAdjustmentsDetails(any(),
            eq(mockReasonableAdjustmentDetailsDto), eq(VALID_JUROR_NUMBER));
    }

    @Test
    void testUpdatePaperSpecialNeedsDetailsHappyPathEmptyReasonableAdjustments() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        List<JurorPaperResponseDto.ReasonableAdjustment> mockReasonableAdjustmentList = new ArrayList<>();

        ReasonableAdjustmentDetailsDto mockReasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        mockReasonableAdjustmentDetailsDto.setReasonableAdjustments(mockReasonableAdjustmentList);

        Mockito.doNothing().when(jurorPaperResponseService).updateReasonableAdjustmentsDetails(bureauJwtPayload,
            mockReasonableAdjustmentDetailsDto, VALID_JUROR_NUMBER);

        mockMvc.perform(MockMvcRequestBuilders.patch(
                    UPDATE_PAPER_SPECIAL_NEEDS_URI, VALID_JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(mockReasonableAdjustmentDetailsDto)))
            .andExpect(status().isNoContent());

        verify(jurorPaperResponseService, times(1))
            .updateReasonableAdjustmentsDetails(any(), eq(mockReasonableAdjustmentDetailsDto), eq(VALID_JUROR_NUMBER));
    }


    @ParameterizedTest
    @ValueSource(strings = {" ", "1234567891", "12345678"})
    @NullSource
    void testUpdatePaperSpecialNeedsDetailsInvalidJurorNumber(String jurorNumber) throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        List<JurorPaperResponseDto.ReasonableAdjustment> mockReasonableAdjustmentList = new ArrayList<>();
        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment = JurorPaperResponseDto.ReasonableAdjustment
            .builder().assistanceType(ASSISTANCE_TYPE_D).assistanceTypeDetails(ASSISTANCE_TYPE_DETAILS_D).build();
        mockReasonableAdjustmentList.add(reasonableAdjustment);

        ReasonableAdjustmentDetailsDto mockReasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        mockReasonableAdjustmentDetailsDto.setReasonableAdjustments(mockReasonableAdjustmentList);

        Mockito.doNothing().when(jurorPaperResponseService).updateReasonableAdjustmentsDetails(bureauJwtPayload,
            mockReasonableAdjustmentDetailsDto, jurorNumber);

        mockMvc.perform(MockMvcRequestBuilders.patch(
                "/api/v1/moj/juror-paper-response/juror/" + jurorNumber + "/details/special-needs")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(mockReasonableAdjustmentDetailsDto)))
            .andExpect(status().isBadRequest());

        verify(jurorPaperResponseService, times(0))
            .updateReasonableAdjustmentsDetails(any(), eq(mockReasonableAdjustmentDetailsDto), eq(jurorNumber));
    }

    @Test
    void testUpdatePaperReasonableAdjustmentDetailsNullRequestBody() throws Exception {
        bureauJwtPayload = TestUtils.createJwt(OWNER_BUREAU, USERNAME_BUREAU);
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(bureauJwtPayload);

        Mockito.doNothing().when(jurorPaperResponseService).updateReasonableAdjustmentsDetails(bureauJwtPayload,
            null, VALID_JUROR_NUMBER);

        mockMvc.perform(MockMvcRequestBuilders.patch(
                    UPDATE_PAPER_SPECIAL_NEEDS_URI, VALID_JUROR_NUMBER)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(null)))
            .andExpect(status().isBadRequest());

        verify(jurorPaperResponseService, times(0))
            .updateReasonableAdjustmentsDetails(any(), any(), any());
    }
}