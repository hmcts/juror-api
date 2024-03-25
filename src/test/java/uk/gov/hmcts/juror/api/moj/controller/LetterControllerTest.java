package uk.gov.hmcts.juror.api.moj.controller;

import jakarta.servlet.ServletException;
import lombok.SneakyThrows;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CertificateOfExemptionRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JurorForExemptionListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialExemptionListDto;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.RequestInformationLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterPrintService;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterService;
import uk.gov.hmcts.juror.api.moj.service.trial.ExemptionCertificateService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LetterController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {LetterController.class})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class LetterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestInformationLetterService requestInformationLetterService;
    @MockBean
    private CourtLetterService courtLetterService;
    @MockBean
    private CourtLetterPrintService courtLetterPrintService;
    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;
    @MockBean
    private ReissueLetterService reissueLetterService;

    @MockBean
    private ExemptionCertificateService exemptionCertificateService;

    @Test
    public void test_post_requestInformation_bureauUser_happyPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("111111111")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Request Letter queued for juror number 111111111")));

        Mockito.verify(requestInformationLetterService, Mockito.times(1))
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_courtUser() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("111111111")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isForbidden());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_bureauUser_noJurorNumber() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_bureauUser_invalidJurorNumber_tooLong() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("1111111111")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_bureauUser_invalidJurorNumber_tooShort() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("11111111")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_bureauUser_invalidJurorNumber_alphaNumeric() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("L111l111!")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_bureauUser_noReplyMethod() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("111111111")
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void test_post_requestInformation_bureauUser_emptyMissingInfoList() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        AdditionalInformationDto additionalInformationDto = AdditionalInformationDto.builder()
            .jurorNumber("111111111")
            .replyMethod(ReplyMethod.PAPER)
            .build();
        Mockito.doNothing().when(requestInformationLetterService)
            .requestInformation(jwtPayload, additionalInformationDto);

        mockMvc.perform(post("/api/v1/moj/letter/request-information")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(additionalInformationDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(requestInformationLetterService, Mockito.never())
            .requestInformation(jwtPayload, additionalInformationDto);
    }

    @Test
    public void testReissueLetterListInvalidRequestJurorNumberPoolNumber() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
            .jurorNumber("111111111")
            .poolNumber("415220110")
            .letterType(LetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/reissue-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(reissueLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(reissueLetterService, Mockito.never())
            .reissueLetterList(reissueLetterListRequestDto);
    }

    @Test
    public void testReissueLetterListInvalidRequestShowAllJurorNumber() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
            .jurorNumber("111111111")
            .showAllQueued(true)
            .letterType(LetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/reissue-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(reissueLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(reissueLetterService, Mockito.never())
            .reissueLetterList(reissueLetterListRequestDto);
    }

    @Test
    public void testReissueLetterListInvalidRequestShowAllPoolNumber() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
            .poolNumber("111111111")
            .showAllQueued(true)
            .letterType(LetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/reissue-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(reissueLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(reissueLetterService, Mockito.never())
            .reissueLetterList(reissueLetterListRequestDto);
    }

    @Test
    public void testReissueLetterInvalidRequestJurorNumberPoolNumber() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
            .letters(new ArrayList<>())
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/reissue-letter")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(reissueLetterRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(reissueLetterService, Mockito.never())
            .reissueLetter(reissueLetterRequestDto);
    }


    @Test
    public void testDeleteLetterInvalidRequestEmpty() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
            .letters(new ArrayList<>())
            .build();

        mockMvc.perform(delete("/api/v1/moj/letter/delete-pending-letter")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(reissueLetterRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(reissueLetterService, Mockito.never())
            .deletePendingLetter(reissueLetterRequestDto);
    }

    @Test
    @SneakyThrows
    @DisplayName("Invalid request - juror number and pool number")
    public void courtLetter_getEligibleList_invalidRequestJurorNumberPoolNumber() {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        CourtLetterListRequestDto courtLetterListRequestDto = CourtLetterListRequestDto.builder()
            .jurorNumber("111111111")
            .poolNumber("415220110")
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/court-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(courtLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(courtLetterService, Mockito.never())
            .getEligibleList(courtLetterListRequestDto);
    }

    @Test
    @SneakyThrows
    @DisplayName("Invalid request - juror name and pool number")
    public void courtLetter_getEligibleList_invalidRequestJurorNamePoolNumber() {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        CourtLetterListRequestDto courtLetterListRequestDto = CourtLetterListRequestDto.builder()
            .jurorName("Test")
            .poolNumber("415220110")
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/court-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(courtLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(courtLetterService, Mockito.never())
            .getEligibleList(courtLetterListRequestDto);
    }

    @Test
    @SneakyThrows
    @DisplayName("Invalid request - juror postcode and pool number")
    public void courtLetter_getEligibleList_invalidRequestJurorPostcodePoolNumber() {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        CourtLetterListRequestDto courtLetterListRequestDto = CourtLetterListRequestDto.builder()
            .jurorPostcode("CH1 2AN")
            .poolNumber("415220110")
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/court-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(courtLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(courtLetterService, Mockito.never())
            .getEligibleList(courtLetterListRequestDto);
    }

    @Test
    @SneakyThrows
    @DisplayName("Invalid request - include printed only")
    public void courtLetter_getEligibleList_invalidRequestIncludePrintedOnly() {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        CourtLetterListRequestDto courtLetterListRequestDto = CourtLetterListRequestDto.builder()
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .includePrinted(true)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/court-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(courtLetterListRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(courtLetterService, Mockito.never())
            .getEligibleList(courtLetterListRequestDto);
    }

    @Test
    @SneakyThrows
    @DisplayName("Valid request")
    public void courtLetter_getEligibleList_validRequest() {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        CourtLetterListRequestDto courtLetterListRequestDto = CourtLetterListRequestDto.builder()
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .jurorNumber("123456789")
            .build();

        Mockito.doReturn(new LetterListResponseDto()).when(courtLetterService)
            .getEligibleList(Mockito.any());

        mockMvc.perform(post("/api/v1/moj/letter/court-letter-list")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(courtLetterListRequestDto)))
            .andExpect(status().isOk());

        Mockito.verify(courtLetterService, Mockito.times(1))
            .getEligibleList(Mockito.any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Invalid request - no letter type")
    public void courtLetter_printCourtLetters_invalidRequest_nullLetterType() {
        String courtOwner = "415";
        String username = "COURT_USER";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        PrintLettersRequestDto printLettersRequestDto = PrintLettersRequestDto.builder()
            .jurorNumbers(List.of("111111111"))
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/print-court-letter")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(printLettersRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(courtLetterPrintService, Mockito.never())
            .getPrintLettersData(printLettersRequestDto, username);
    }

    @Test
    @SneakyThrows
    @DisplayName("Invalid request - no juror numbers")
    public void courtLetter_printCourtLetters_invalidRequest_nullJurorNumbers() {
        String courtOwner = "415";
        String username = "COURT_USER";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        PrintLettersRequestDto printLettersRequestDto = PrintLettersRequestDto.builder()
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .build();

        mockMvc.perform(post("/api/v1/moj/letter/print-court-letter")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(printLettersRequestDto)))
            .andExpect(status().isBadRequest());

        Mockito.verify(courtLetterPrintService, Mockito.never())
            .getPrintLettersData(printLettersRequestDto, username);
    }

    @Test
    @SneakyThrows
    @DisplayName("Valid request")
    public void courtLetter_printCourtLetter_validRequest() {
        String courtOwner = "415";
        String username = "COURT_USER";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        PrintLettersRequestDto printLettersRequestDto = PrintLettersRequestDto.builder()
            .jurorNumbers(List.of("111111111"))
            .letterType(CourtLetterType.DEFERRAL_GRANTED)
            .build();

        Mockito.doReturn(List.of(PrintLetterDataResponseDto.builder().build())).when(courtLetterPrintService)
            .getPrintLettersData(printLettersRequestDto, username);

        mockMvc.perform(post("/api/v1/moj/letter/print-court-letter")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(printLettersRequestDto)))
            .andExpect(status().isOk());

        Mockito.verify(courtLetterPrintService, Mockito.times(1))
            .getPrintLettersData(Mockito.any(), Mockito.any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - Print certificate of exemption")
    public void courtLetter_printCourtLetter_CertificateOfExemption_happyPath() {
        String courtOwner = "415";
        String username = "COURT_USER";
        String caseNumber = "T1000000";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));
        CertificateOfExemptionRequestDto certificateOfExemptionRequestDto =
            CertificateOfExemptionRequestDto.builder()
            .jurorNumbers(List.of("111111111"))
            .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION)
            .judge("SIR DREDD")
            .exemptionPeriod("5")
            .trialNumber(caseNumber)
            .build();

        Mockito.doReturn(List.of(PrintLetterDataResponseDto.builder().build())).when(courtLetterPrintService)
            .getPrintLettersData(certificateOfExemptionRequestDto, username);

        mockMvc.perform(post("/api/v1/moj/letter/print-certificate-of-exemption")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(certificateOfExemptionRequestDto)))
            .andExpect(status().isOk());

        Mockito.verify(courtLetterPrintService, Mockito.times(1))
            .getPrintLettersData(certificateOfExemptionRequestDto, username);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - jurors-exemption-list - happy path")
    public void courtLetter_CertificateOfExemption_jurorExemptionList_happyPath() {
        String courtOwner = "415";
        String username = "COURT_USER";
        String caseNumber = "T10000000";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        Mockito.doReturn(List.of(JurorForExemptionListDto.builder().build()))
            .when(exemptionCertificateService).getJurorsForExemptionList(caseNumber,courtOwner);

        mockMvc.perform(get("/api/v1/moj/letter/jurors-exemption-list?case_number=T10000000&court_location=415")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication()))
            .andExpect(status().isOk());

        Mockito.verify(exemptionCertificateService, Mockito.times(1))
            .getJurorsForExemptionList(caseNumber, courtOwner);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - jurors-exemption-list - no query params")
    public void courtLetter_CertificateOfExemption_jurorExemptionList_noQueryParams() {
        String courtOwner = "415";
        String username = "COURT_USER";
        String caseNumber = "T1000000";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        Mockito.doReturn(List.of(JurorForExemptionListDto.builder().build()))
            .when(exemptionCertificateService).getJurorsForExemptionList(caseNumber,courtOwner);

        mockMvc.perform(get("/api/v1/moj/letter/jurors-exemption-list")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication()))
            .andExpect(status().isBadRequest());


        Mockito.verify(exemptionCertificateService, Mockito.times(0))
            .getJurorsForExemptionList(caseNumber, courtOwner);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - juror-exemption-list - invalid query param")
    public void courtLetter_CertificateOfExemption_jurorExemptionList_invalidQueryParam() {
        String courtOwner = "415";
        String username = "COURT_USER";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        TrialExemptionListDto dto = TrialExemptionListDto.builder()
            .trialType("Civil")
            .defendants("TEST DEFENDANT")
            .trialNumber("T1")
            .judge("SIR DREDD")
            .startDate(LocalDate.now()).build();

        Mockito.doReturn(Collections.singletonList(dto)).when(exemptionCertificateService)
            .getTrialExemptionList(courtOwner);

        Assertions.assertThrows(ServletException.class, () -> {
            mockMvc.perform(get("/api/v1/moj/letter/jurors"
                    + "-exemption"
                    + "-list"
                    + "?case_number=c&court_location=a")
                    .principal(
                        SecurityUtil.getActiveUsersBureauJwtAuthentication()))
                .andExpect(status().isBadRequest());
        });
        Mockito.verify(exemptionCertificateService, Mockito.times(0))
            .getTrialExemptionList(courtOwner);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - trials-exemption-list - no query params")
    public void courtLetter_CertificateOfExemption_trialsExemptionList_noQueryParams() {
        String courtOwner = "415";
        String username = "COURT_USER";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        TrialExemptionListDto dto = TrialExemptionListDto.builder()
                .trialType("Civil")
                .defendants("TEST DEFENDANT")
                .trialNumber("T1")
                .judge("SIR DREDD")
                .startDate(LocalDate.now()).build();

        Mockito.doReturn(Collections.singletonList(dto)).when(exemptionCertificateService)
            .getTrialExemptionList(courtOwner);


        mockMvc.perform(get("/api/v1/moj/letter/trials-exemption-list")
                .principal(SecurityUtil.getActiveUsersBureauJwtAuthentication()))
            .andExpect(status().isBadRequest());

        Mockito.verify(exemptionCertificateService, Mockito.times(0))
            .getTrialExemptionList(courtOwner);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - trials-exemption-list - invalid query param")
    public void courtLetter_CertificateOfExemption_trialsExemptionList_invalidQueryParam() {
        String courtOwner = "415";
        String username = "COURT_USER";

        TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

        TrialExemptionListDto dto = TrialExemptionListDto.builder()
            .trialType("Civil")
            .defendants("TEST DEFENDANT")
            .trialNumber("T1")
            .judge("SIR DREDD")
            .startDate(LocalDate.now()).build();

        Mockito.doReturn(Collections.singletonList(dto)).when(exemptionCertificateService)
            .getTrialExemptionList(courtOwner);

        Assertions.assertThrows(ServletException.class, () -> {
            mockMvc.perform(get("/api/v1/moj/letter/trials"
                    + "-exemption-list?court_location"
                    + "={court_location}", "a")
                    .principal(
                        SecurityUtil.getActiveUsersBureauJwtAuthentication()).contentType(MediaType.APPLICATION_JSON));
        });

        Mockito.verify(exemptionCertificateService, Mockito.times(0))
            .getTrialExemptionList(courtOwner);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - trials-exemption-list - invalid payload")
    public void courtLetter_CertificateOfExemption_trialsExemptionList_invalidPayload() {

        createInvalidPayload();

        TrialExemptionListDto dto = TrialExemptionListDto.builder()
            .trialType("Civil")
            .defendants("TEST DEFENDANT")
            .trialNumber("T1")
            .judge("SIR DREDD")
            .startDate(LocalDate.now()).build();

        Mockito.doReturn(Collections.singletonList(dto)).when(exemptionCertificateService)
            .getTrialExemptionList("415");

        Assertions.assertThrows(MojException.Forbidden.class,() ->
            mockMvc.perform(
                get("/api/v1/moj/letter/trials-exemption-list?court_location=415")
                .principal(
                    SecurityUtil.getActiveUsersBureauJwtAuthentication()).contentType(MediaType.APPLICATION_JSON)));

        verifyNoMoreInteractions(exemptionCertificateService);
    }

    @Test
    @SneakyThrows
    @DisplayName("Certificate of Exemption - juror-exemption-list - invalid payload")
    public void courtLetter_CertificateOfExemption_jurorExemptionList_invalidPayload() {
        createInvalidPayload();

        Assertions.assertThrows(MojException.Forbidden.class, () ->
            mockMvc.perform(
                get("/api/v1/moj/letter/jurors-exemption-list?case_number=T10000000&court_location=415")
                .principal(
                    SecurityUtil.getActiveUsersBureauJwtAuthentication()).contentType(MediaType.APPLICATION_JSON)));

        verifyNoMoreInteractions(exemptionCertificateService);
    }

    private void createInvalidPayload() {
        BureauJwtAuthentication auth = mock(BureauJwtAuthentication.class);
        when(auth.getPrincipal())
            .thenReturn(TestUtils.createJwt("a", "", "1", null));
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }
}

