package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.DisqualifyJurorService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
class DisqualifyJurorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisqualifyJurorService disqualifyJurorService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @Test
    public void getDisqualifyReasonsBureauHappy() throws Exception {
        final ArgumentCaptor<BureauJWTPayload> bureauJwtPayloadCaptor = ArgumentCaptor.forClass(BureauJWTPayload.class);
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getDisqualifyReasons()).when(disqualifyJurorService).getDisqualifyReasons(any());

        mockMvc.perform(get("/api/v1/moj/disqualify/reasons")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(1)))
            .andExpect(jsonPath("$.disqualifyReasons.length()", is(6)))
            .andExpect(jsonPath("$.disqualifyReasons[*].code").value(containsInAnyOrder("A", "B", "C", "N", "O", "R")))
            .andExpect(jsonPath("$.disqualifyReasons[*].description").value(containsInAnyOrder(
                "Age",
                "Bail",
                "Conviction",
                "Mental Capacity Act",
                "Mental Health Act",
                "Residency")));

        verify(disqualifyJurorService, times(1)).getDisqualifyReasons(bureauJwtPayloadCaptor.capture());
        assertThat(bureauJwtPayloadCaptor.getValue().getLogin()).isEqualTo("BUREAU_USER");
        assertThat(bureauJwtPayloadCaptor.getValue().getStaff()).isNull();
        assertThat(bureauJwtPayloadCaptor.getValue().getDaysToExpire()).isEqualTo(89);
        assertThat(bureauJwtPayloadCaptor.getValue().getOwner()).isEqualTo("400");
        assertThat(bureauJwtPayloadCaptor.getValue().getPasswordWarning()).isEqualTo(Boolean.FALSE);
        assertThat(bureauJwtPayloadCaptor.getValue().getUserLevel()).isEqualTo("99");
    }

    @Test
    public void getDisqualifyReasonsCourtHappy() throws Exception {
        final ArgumentCaptor<BureauJWTPayload> bureauJwtPayloadCaptor = ArgumentCaptor.forClass(BureauJWTPayload.class);
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getDisqualifyReasons()).when(disqualifyJurorService).getDisqualifyReasons(any());

        mockMvc.perform(get("/api/v1/moj/disqualify/reasons")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(1)))
            .andExpect(jsonPath("$.disqualifyReasons.length()", is(6)))
            .andExpect(jsonPath("$.disqualifyReasons[*].code").value(containsInAnyOrder("A", "B", "C", "N", "O", "R")))
            .andExpect(jsonPath("$.disqualifyReasons[*].description").value(containsInAnyOrder(
                "Age",
                "Bail",
                "Conviction",
                "Mental Capacity Act",
                "Mental Health Act",
                "Residency")));

        verify(disqualifyJurorService, times(1)).getDisqualifyReasons(bureauJwtPayloadCaptor.capture());
        assertThat(bureauJwtPayloadCaptor.getValue().getLogin()).isEqualTo("COURT_USER");
        assertThat(bureauJwtPayloadCaptor.getValue().getStaff()).isNull();
        assertThat(bureauJwtPayloadCaptor.getValue().getDaysToExpire()).isEqualTo(89);
        assertThat(bureauJwtPayloadCaptor.getValue().getOwner()).isEqualTo("415");
        assertThat(bureauJwtPayloadCaptor.getValue().getPasswordWarning()).isEqualTo(Boolean.FALSE);
        assertThat(bureauJwtPayloadCaptor.getValue().getUserLevel()).isEqualTo("99");
    }

    @Test
    public void getDisqualifyReasonsInvalidPath() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void getDisqualifyReasonsIncorrectHttpOperation() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void disqualifyJurorBureauHappy() throws Exception {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<DisqualifyJurorDto> disqualifyJurorServiceCaptor =
            ArgumentCaptor.forClass(DisqualifyJurorDto.class);
        final ArgumentCaptor<BureauJWTPayload> bureauJwtPayloadCaptor = ArgumentCaptor.forClass(BureauJWTPayload.class);

        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(String.class), any(DisqualifyJurorDto.class),
            any(BureauJWTPayload.class));

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code C")));

        verify(disqualifyJurorService, times(1)).disqualifyJuror(
            jurorNumberCaptor.capture(),
            disqualifyJurorServiceCaptor.capture(),
            bureauJwtPayloadCaptor.capture());

        assertThat(jurorNumberCaptor.getValue()).isEqualTo("923456789");

        assertThat(disqualifyJurorServiceCaptor.getValue().getCode()).isEqualTo(DisqualifyCodeEnum.C);
        assertThat(disqualifyJurorServiceCaptor.getValue().getReplyMethod()).isEqualTo(ReplyMethod.PAPER);

        assertThat(bureauJwtPayloadCaptor.getValue().getLogin()).isEqualTo("BUREAU_USER");
        assertThat(bureauJwtPayloadCaptor.getValue().getStaff()).isNull();
        assertThat(bureauJwtPayloadCaptor.getValue().getDaysToExpire()).isEqualTo(89);
        assertThat(bureauJwtPayloadCaptor.getValue().getOwner()).isEqualTo("400");
        assertThat(bureauJwtPayloadCaptor.getValue().getPasswordWarning()).isEqualTo(Boolean.FALSE);
        assertThat(bureauJwtPayloadCaptor.getValue().getUserLevel()).isEqualTo("99");
    }

    @Test
    public void disqualifyJurorCourtHappy() throws Exception {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<DisqualifyJurorDto> disqualifyJurorServiceCaptor =
            ArgumentCaptor.forClass(DisqualifyJurorDto.class);
        final ArgumentCaptor<BureauJWTPayload> bureauJwtPayloadCaptor = ArgumentCaptor.forClass(BureauJWTPayload.class);

        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJuror(any(String.class), any(DisqualifyJurorDto.class),
            any(BureauJWTPayload.class));

        DisqualifyJurorDto disqualifyJurorDto = createRequestToDisqualifyJuror();
        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(disqualifyJurorDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code C")));

        verify(disqualifyJurorService, times(1)).disqualifyJuror(
            jurorNumberCaptor.capture(),
            disqualifyJurorServiceCaptor.capture(),
            bureauJwtPayloadCaptor.capture());

        assertThat(jurorNumberCaptor.getValue()).isEqualTo("923456789");

        assertThat(disqualifyJurorServiceCaptor.getValue().getCode()).isEqualTo(DisqualifyCodeEnum.C);
        assertThat(disqualifyJurorServiceCaptor.getValue().getReplyMethod()).isEqualTo(ReplyMethod.PAPER);

        assertThat(bureauJwtPayloadCaptor.getValue().getLogin()).isEqualTo("COURT_USER");
        assertThat(bureauJwtPayloadCaptor.getValue().getStaff()).isNull();
        assertThat(bureauJwtPayloadCaptor.getValue().getDaysToExpire()).isEqualTo(89);
        assertThat(bureauJwtPayloadCaptor.getValue().getOwner()).isEqualTo("415");
        assertThat(bureauJwtPayloadCaptor.getValue().getPasswordWarning()).isEqualTo(Boolean.FALSE);
        assertThat(bureauJwtPayloadCaptor.getValue().getUserLevel()).isEqualTo("99");
    }


    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678910"})
    void disqualifyJurorInvalidJurorNumbers(String jurorNumber) throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void disqualifyJurorAbsentJurorNumber() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void disqualifyJurorInvalidPath() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void disqualifyJurorMissingReplyMethod() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
            .andExpect(jsonPath("$.errors[0]", is("Reply method is missing")));

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }

    @Test
    public void disqualifyJurorMissingDisqualifyCode() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
            .andExpect(jsonPath("$.errors[0]", is("Disqualify code is missing")));

        verify(disqualifyJurorService, never()).disqualifyJuror(any(), any(), any());
    }

    @Test
    public void disqualifyJurorNoDisqualifyJurorDto() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void disqualifyJurorDueToAgeBureauHappyPath() throws Exception {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<BureauJWTPayload> bureauJwtPayloadCaptor = ArgumentCaptor.forClass(BureauJWTPayload.class);

        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJurorDueToAgeOutOfRange(any(String.class),
            any(BureauJWTPayload.class));

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789/age")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code A")));

        verify(disqualifyJurorService, times(1)).disqualifyJurorDueToAgeOutOfRange(
            jurorNumberCaptor.capture(),
            bureauJwtPayloadCaptor.capture());

        assertThat(jurorNumberCaptor.getValue()).isEqualTo("923456789");
        assertThat(bureauJwtPayloadCaptor.getValue().getLogin()).isEqualTo("BUREAU_USER");
        assertThat(bureauJwtPayloadCaptor.getValue().getStaff()).isNull();
        assertThat(bureauJwtPayloadCaptor.getValue().getDaysToExpire()).isEqualTo(89);
        assertThat(bureauJwtPayloadCaptor.getValue().getOwner()).isEqualTo("400");
        assertThat(bureauJwtPayloadCaptor.getValue().getPasswordWarning()).isEqualTo(Boolean.FALSE);
        assertThat(bureauJwtPayloadCaptor.getValue().getUserLevel()).isEqualTo("99");
    }


    @Test
    public void disqualifyJurorDueToAgeCourtsHappyPath() throws Exception {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<BureauJWTPayload> bureauJwtPayloadCaptor = ArgumentCaptor.forClass(BureauJWTPayload.class);

        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doNothing().when(disqualifyJurorService).disqualifyJurorDueToAgeOutOfRange(any(String.class),
            any(BureauJWTPayload.class));

        mockMvc.perform(patch("/api/v1/moj/disqualify/juror/923456789/age")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", CoreMatchers.is("Juror 923456789 disqualified with code A")));

        verify(disqualifyJurorService, times(1)).disqualifyJurorDueToAgeOutOfRange(
            jurorNumberCaptor.capture(),
            bureauJwtPayloadCaptor.capture());

        assertThat(jurorNumberCaptor.getValue()).isEqualTo("923456789");
        assertThat(bureauJwtPayloadCaptor.getValue().getLogin()).isEqualTo("COURT_USER");
        assertThat(bureauJwtPayloadCaptor.getValue().getStaff()).isNull();
        assertThat(bureauJwtPayloadCaptor.getValue().getDaysToExpire()).isEqualTo(89);
        assertThat(bureauJwtPayloadCaptor.getValue().getOwner()).isEqualTo("415");
        assertThat(bureauJwtPayloadCaptor.getValue().getPasswordWarning()).isEqualTo(Boolean.FALSE);
        assertThat(bureauJwtPayloadCaptor.getValue().getUserLevel()).isEqualTo("99");
    }


    @ParameterizedTest
    @ValueSource(strings = {"", "1234567891", "123", "12h£o986d"})
    void disqualifyJurorDueToAgeInvalidJurorNumberNoJurorNumber(String jurorNumber) throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    public void disqualifyJurorDueToAgeInvalidPath() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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