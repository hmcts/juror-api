package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.DisqualifyJurorService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DisqualifyJurorController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {
    DisqualifyJurorController.class,
    RestResponseEntityExceptionHandler.class
})
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods",
    "PMD.LawOfDemeter"
})
class DisqualifyJurorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisqualifyJurorService disqualifyJurorService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @Test
    void positiveGetDisqualifyReasonsBureau() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getDisqualifyReasons()).when(disqualifyJurorService).getDisqualifyReasons(any());

        mockMvc.perform(get("/api/v1/moj/disqualify/reasons")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(1)))
            .andExpect(jsonPath("$.disqualifyReasons.length()", is(7)))
            .andExpect(jsonPath("$.disqualifyReasons[*].code").value(containsInAnyOrder("A", "B", "C", "M", "N", "O",
                "R")))
            .andExpect(jsonPath("$.disqualifyReasons[*].description").value(containsInAnyOrder(
                "Age",
                "Bail",
                "Conviction",
                "Suffering From a Mental Disorder",
                "Mental Capacity Act",
                "Mental Health Act",
                "Residency")));

        verify(disqualifyJurorService, times(1)).getDisqualifyReasons(jwtPayload);
    }

    @Test
    void positiveGetDisqualifyReasonsCourt() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getDisqualifyReasons()).when(disqualifyJurorService).getDisqualifyReasons(any());

        mockMvc.perform(get("/api/v1/moj/disqualify/reasons")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(1)))
            .andExpect(jsonPath("$.disqualifyReasons.length()", is(7)))
            .andExpect(jsonPath("$.disqualifyReasons[*].code").value(containsInAnyOrder("A", "B", "C", "M", "N", "O",
                "R")))
            .andExpect(jsonPath("$.disqualifyReasons[*].description").value(containsInAnyOrder(
                "Age",
                "Bail",
                "Conviction",
                "Suffering From a Mental Disorder",
                "Mental Capacity Act",
                "Mental Health Act",
                "Residency")));

        verify(disqualifyJurorService, times(1))
            .getDisqualifyReasons(jwtPayload);
    }

    @Test
    void negativeGetDisqualifyReasonsInvalidPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getDisqualifyReasons()).when(disqualifyJurorService).getDisqualifyReasons(any());

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(get("/api/v1/moj/disqualify/rea5ons")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isNotFound());

        verify(disqualifyJurorService, never()).getDisqualifyReasons(any());
    }

    @Test
    void negativeGetDisqualifyReasonsIncorrectHttpOperation() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getDisqualifyReasons()).when(disqualifyJurorService).getDisqualifyReasons(any());

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(post("/api/v1/moj/disqualify/reasons")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isMethodNotAllowed());

        verify(disqualifyJurorService, never()).getDisqualifyReasons(any());
    }

    @Test
    void positiveDisqualifyJurorBureauHappy() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(String.class), any(DisqualifyJurorDto.class),
            any(BureauJwtPayload.class));

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code C")));

        verify(disqualifyJurorService, times(1)).disqualifyJuror(
            "923456789",disqualifyJurorDto, jwtPayload);
    }

    @Test
    void positiveDisqualifyJurorCourt() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(String.class), any(DisqualifyJurorDto.class),
            any(BureauJwtPayload.class));

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code C")));

        verify(disqualifyJurorService, times(1)).disqualifyJuror(
            "923456789",disqualifyJurorDto, jwtPayload);
    }


    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678910"})
    void disqualifyJurorInvalidJurorNumbers(String jurorNumber) throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(), any(), any());

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/" + jurorNumber)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isBadRequest());

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }

    @Test
    void disqualifyJurorAbsentJurorNumber() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(), any(), any());

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isNotFound());

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }


    @Test
    void disqualifyJurorInvalidPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(), any(), any());

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/j")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isNotFound());

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }

    @Test
    void disqualifyJurorMissingReplyMethod() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(), any(), any());

        DisqualifyJurorDto disqualifyJurorDto = DisqualifyJurorDto.builder()
            .code(DisqualifyCodeEnum.C)
            .build();

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/123456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field", is("replyMethod")))
            .andExpect(jsonPath("$.errors[0].message", is("Reply method is missing")));

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }

    @Test
    void disqualifyJurorMissingDisqualifyCode() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(), any(), any());

        DisqualifyJurorDto disqualifyJurorDto = DisqualifyJurorDto.builder()
            .replyMethod(ReplyMethod.PAPER)
            .build();

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/123456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field", is("code")))
            .andExpect(jsonPath("$.errors[0].message", is("Disqualify code is missing")));

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }

    @Test
    void disqualifyJurorNoDisqualifyJurorDto() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(), any(), any());

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/123456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(null)))
            .andExpect(status().isBadRequest());

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }


    @Test
    void disqualifyJurorDueToAgeBureauHappyPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJurorDueToAgeOutOfRange(any(String.class),
            any(BureauJwtPayload.class));

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789/age")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code A")));

        verify(disqualifyJurorService, times(1)).disqualifyJurorDueToAgeOutOfRange(
            "923456789", jwtPayload);
    }


    @Test
    void disqualifyJurorDueToAgeCourtsHappyPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJurorDueToAgeOutOfRange(any(String.class),
            any(BureauJwtPayload.class));

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789/age")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code A")));

        verify(disqualifyJurorService, times(1)).disqualifyJurorDueToAgeOutOfRange(
            "923456789",jwtPayload);
    }


    @ParameterizedTest
    @ValueSource(strings = {"", "1234567891", "123", "12h£o986d"})
    void disqualifyJurorDueToAgeInvalidJurorNumberNoJurorNumber(String jurorNumber) throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJurorDueToAgeOutOfRange(any(), any());

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/" + jurorNumber + "/age")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }


    @Test
    void disqualifyJurorDueToAgeInvalidPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJurorDueToAgeOutOfRange(any(), any());

        mockMvc.perform(patch("/api/v1/moj/disqualfy/juror/123456789/age")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }


    private DisqualifyJurorDto createRequestToDisqualifyJuror() {
        return DisqualifyJurorDto.builder()
            .code(DisqualifyCodeEnum.C)
            .replyMethod(ReplyMethod.PAPER)
            .build();
    }

    private DisqualifyReasonsDto getDisqualifyReasons() {
        List<DisqualifyReasonsDto.DisqualifyReasons> disqualifyReasons = new ArrayList<>();

        for (DisqualifyCodeEnum disqualifyCodeEnum : DisqualifyCodeEnum.values()) {
            DisqualifyReasonsDto.DisqualifyReasons disqualifyReason = DisqualifyReasonsDto.DisqualifyReasons
                .builder()
                .code(disqualifyCodeEnum.getCode())
                .description(disqualifyCodeEnum.getDescription())
                .build();
            disqualifyReasons.add(disqualifyReason);
        }

        return DisqualifyReasonsDto.builder().disqualifyReasons(disqualifyReasons).build();
    }
}