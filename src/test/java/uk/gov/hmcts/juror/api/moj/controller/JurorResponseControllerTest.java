package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseRetrieveService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = JurorResponseController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ContextConfiguration(classes = {
    JurorResponseController.class,
    RestResponseEntityExceptionHandler.class,
    JurorResponseControllerTest.TestMvcConfiguration.class
})
class JurorResponseControllerTest {

    private static final String JUROR_NUMBER = "123456789";
    private static final String URI =
        "/api/v1/moj/juror-response/juror/{jurorNumber}/details/personal";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JurorResponseService jurorResponseService;

    @MockitoBean
    private SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;

    @MockitoBean
    private JurorResponseRetrieveService jurorResponseRetrieveService;

    @MockitoBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @EnumSource(value = ReplyMethod.class, names = {"PAPER", "DIGITAL"})
    void updatePersonalDetailsValidRequestReturnsNoContent(ReplyMethod replyMethod) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        JurorPersonalDetailsDto request = validRequest(replyMethod);

        mockMvc.perform(patch(URI, JUROR_NUMBER)
                .principal(principal(payload))
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isNoContent());

        ArgumentCaptor<JurorPersonalDetailsDto> captor =
            ArgumentCaptor.forClass(JurorPersonalDetailsDto.class);

        verify(jurorResponseService).updateJurorPersonalDetails(
            same(payload), captor.capture(), eq(JUROR_NUMBER));

        assertThat(captor.getValue())
            .usingRecursiveComparison()
            .isEqualTo(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void updatePersonalDetailsBlankAddressReturnsBadRequest(String address) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        JurorPersonalDetailsDto request = validRequest(ReplyMethod.PAPER);
        request.setAddressLineOne(address);

        mockMvc.perform(patch(URI, JUROR_NUMBER)
                .principal(principal(payload))
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    private JurorPersonalDetailsDto validRequest(ReplyMethod replyMethod) {
        JurorPersonalDetailsDto request = new JurorPersonalDetailsDto();
        request.setReplyMethod(replyMethod);
        request.setFirstName("Test");
        request.setLastName("Person");
        request.setAddressLineOne("1 First Street");
        request.setAddressPostcode("MK5 6LA");
        return request;
    }

    private BureauJwtAuthentication principal(BureauJwtPayload payload) {
        BureauJwtAuthentication authentication = mock(BureauJwtAuthentication.class);
        when(authentication.getPrincipal()).thenReturn(payload);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        return authentication;
    }

    @TestConfiguration
    static class TestMvcConfiguration implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }
}
