package uk.gov.hmcts.juror.api.moj.controller;

import lombok.SneakyThrows;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterPrintService;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.RequestInformationLetterService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Test
    public void test_post_requestInformation_bureauUser_happyPath() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

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

}

