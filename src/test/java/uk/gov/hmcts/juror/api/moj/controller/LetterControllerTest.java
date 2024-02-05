package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.RequestInformationLetterService;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LetterController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {LetterController.class})
public class LetterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestInformationLetterService requestInformationLetterService;
    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;
    @MockBean
    private ReissueLetterService reissueLetterService;

    @Before
    public void setupMocks() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

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
    public void testReissueDeferralLetterListInvalidRequestJurorNumberPoolNumber() throws Exception {
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
    public void testReissueDeferralLetterListInvalidRequestShowAllJurorNumber() throws Exception {
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
    public void testReissueDeferralLetterListInvalidRequestShowAllPoolNumber() throws Exception {
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
    public void testReissueDeferralLetterInvalidRequestJurorNumberPoolNumber() throws Exception {
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
    public void testDeleteDeferralLetterInvalidRequestEmpty() throws Exception {
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

}

