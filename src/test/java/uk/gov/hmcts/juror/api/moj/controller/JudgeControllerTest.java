package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeListDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.trial.JudgeService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.api.TestUtils.createJwt;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = JudgeController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {JudgeController.class})
class JudgeControllerTest {
    private static final String BASE_URL = "/api/v1/moj/trial/judge";

    private BureauJWTPayload jwtPayload;
    private MockMvc mockMvc;

    @MockBean
    private JudgeService judgeService;
    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @Before
    @BeforeEach
    public void setupMocks() {
        jwtPayload = null;
        mockMvc = MockMvcBuilders
            .standaloneSetup(new JudgeController(judgeService))
            .setCustomArgumentResolvers(new PrincipalDetailsArgumentResolver())
            .build();
    }

    @Test
    void getJudgesForCourtLocationsHappy() throws Exception {
        final ArgumentCaptor<String> ownerArg = ArgumentCaptor.forClass(String.class);

        jwtPayload = createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(createJudgeListDto()).when(judgeService).getJudgeForCourtLocation("415");

        mockMvc.perform(get(BASE_URL + "/list").principal(mockPrincipal))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").value(IsNull.notNullValue()))
            .andExpect(jsonPath("$.judges.length()", is(2)))
            .andExpect(jsonPath("$.judges[*].code").value(containsInAnyOrder("DRED", "JUDD")))
            .andExpect(jsonPath("$.judges[*].description").value(containsInAnyOrder(
                "DREDD", "LAWSON")));

        verify(judgeService, times(1)).getJudgeForCourtLocation(ownerArg.capture());
        assertThat(ownerArg.getValue()).isEqualTo("415");
    }

    @Test
    void getJudgesForCourtLocationsForbiddenBureauUser() throws Exception {
        final ArgumentCaptor<String> ownerArg = ArgumentCaptor.forClass(String.class);

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("400", "BUREAU_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        when(judgeService.getJudgeForCourtLocation("415")).thenReturn(createJudgeListDto());

        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/list").principal(mockPrincipal))
            .andExpect(status().isForbidden())
            .andReturn();

        MojException.Forbidden ex = (MojException.Forbidden) mvcResult.getResolvedException();
        assertThat(ex.getMessage()).isEqualTo("Bureau users are not allowed to use this service");

        verify(judgeService, never()).getJudgeForCourtLocation(ownerArg.capture());
    }

    @Test
    public void getJudgesForCourtLocationsIncorrectUrl() throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("415", "COURT_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        when(judgeService.getJudgeForCourtLocation("415")).thenReturn(createJudgeListDto());

        mockMvc.perform(get(BASE_URL + "/lisst").principal(mockPrincipal))
            .andExpect(status().isNotFound());

        verify(judgeService, never()).getJudgeForCourtLocation(any());
    }

    @Test
    public void getJudgesForCourtLocationsIncorrectHttpMethod() throws Exception {
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        jwtPayload = createJwt("415", "COURT_USER");
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        when(judgeService.getJudgeForCourtLocation("415")).thenReturn(createJudgeListDto());

        mockMvc.perform(post(BASE_URL + "/list").principal(mockPrincipal))
            .andExpect(status().isMethodNotAllowed());

        verify(judgeService, never()).getJudgeForCourtLocation(any());
    }
    
    private class PrincipalDetailsArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(BureauJWTPayload.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return jwtPayload;
        }
    }

    private JudgeListDto createJudgeListDto() {
        List<JudgeDto> judgeDto = new ArrayList<>();

        JudgeDto judge1 = new JudgeDto();
        judge1.setCode("DRED");
        judge1.setDescription("DREDD");
        judgeDto.add(judge1);

        JudgeDto judge2 = new JudgeDto();
        judge2.setCode("JUDD");
        judge2.setDescription("LAWSON");
        judgeDto.add(judge2);

        return new JudgeListDto(judgeDto);
    }
}