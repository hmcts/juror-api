package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;
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
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseRetrieveService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
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
    private static final String UPDATE_PERSONAL_DETAILS_URI =
        "/api/v1/moj/juror-response/juror/{jurorNumber}/details/personal";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JurorResponseService jurorResponseService;

    @MockitoBean
    private SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;

    @MockitoBean
    private JurorResponseRetrieveService jurorResponseRetrieveService;

    @MockitoBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @MockitoBean
    private SecurityContextHolderStrategy securityContextStrategy;

    @ParameterizedTest
    @MethodSource("validRequests")
    void updatePersonalDetailsValidRequestReturnsNoContent(JurorPersonalDetailsDto request) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isNoContent());

        ArgumentCaptor<JurorPersonalDetailsDto> personalDetailsCaptor =
            ArgumentCaptor.forClass(JurorPersonalDetailsDto.class);

        verify(jurorResponseService).updateJurorPersonalDetails(
            same(payload), personalDetailsCaptor.capture(), eq(JUROR_NUMBER));

        assertThat(personalDetailsCaptor.getValue())
            .usingRecursiveComparison()
            .isEqualTo(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void updatePersonalDetailsBlankAddressReturnsBadRequest(String address) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        JurorPersonalDetailsDto request = validRequest(ReplyMethod.PAPER);
        request.setAddressLineOne(address);
        BureauJwtAuthentication authentication = authenticate(payload);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "1234567890"})
    void updatePersonalDetailsInvalidJurorNumberReturnsBadRequest(String jurorNumber) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);
        JurorPersonalDetailsDto request = validRequest(ReplyMethod.PAPER);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, jurorNumber)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"replyMethod", "firstName", "lastName", "addressLineOne", "addressPostcode"})
    void updatePersonalDetailsMissingRequiredFieldReturnsBadRequest(String field) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);
        ObjectNode request = objectMapper.valueToTree(validRequest(ReplyMethod.PAPER));
        request.remove(field);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"replyMethod", "firstName", "lastName", "addressLineOne", "addressPostcode"})
    void updatePersonalDetailsNullRequiredFieldReturnsBadRequest(String field) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);
        ObjectNode request = objectMapper.valueToTree(validRequest(ReplyMethod.PAPER));
        request.putNull(field);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void updatePersonalDetailsInvalidFieldReturnsBadRequest(String field, String value) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);
        ObjectNode request = objectMapper.valueToTree(validRequest(ReplyMethod.PAPER));
        request.put(field, value);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{", "[]"})
    void updatePersonalDetailsInvalidBodyReturnsBadRequest(String body) throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(jurorResponseService);
    }

    @ParameterizedTest
    @MethodSource("serviceExceptions")
    void updatePersonalDetailsServiceExceptionReturnsExpectedStatus(MojException exception, int expectedStatus)
        throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication authentication = authenticate(payload);
        JurorPersonalDetailsDto request = validRequest(ReplyMethod.PAPER);
        doThrow(exception).when(jurorResponseService)
            .updateJurorPersonalDetails(same(payload), any(JurorPersonalDetailsDto.class), eq(JUROR_NUMBER));

        mockMvc.perform(patch(UPDATE_PERSONAL_DETAILS_URI, JUROR_NUMBER)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().is(expectedStatus));

        verify(jurorResponseService)
            .updateJurorPersonalDetails(same(payload), any(JurorPersonalDetailsDto.class), eq(JUROR_NUMBER));
    }

    private static List<Arguments> invalidFields() {
        return List.of(
            Arguments.of("replyMethod", "INVALID"),
            Arguments.of("firstName", ""),
            Arguments.of("lastName", ""),
            Arguments.of("addressPostcode", ""),
            Arguments.of("title", "A".repeat(11)),
            Arguments.of("firstName", "A".repeat(21)),
            Arguments.of("lastName", "A".repeat(26)),
            Arguments.of("addressLineOne", "A".repeat(36)),
            Arguments.of("addressLineTwo", "A".repeat(36)),
            Arguments.of("addressLineThree", "A".repeat(36)),
            Arguments.of("addressTown", "A".repeat(36)),
            Arguments.of("addressCounty", "A".repeat(36)),
            Arguments.of("firstName", "Test|Name"),
            Arguments.of("addressPostcode", "INVALID"),
            Arguments.of("primaryPhone", "INVALID"),
            Arguments.of("secondaryPhone", "INVALID"),
            Arguments.of("emailAddress", "INVALID"),
            Arguments.of("emailAddress", "a".repeat(243) + "@example.com"),
            Arguments.of("dateOfBirth", "2999-01-01"),
            Arguments.of("dateOfBirth", "1700-01-01"),
            Arguments.of("dateOfBirth", "INVALID")
        );
    }

    private static List<Arguments> serviceExceptions() {
        return List.of(
            Arguments.of(new MojException.Forbidden("Access denied", null), 403),
            Arguments.of(new MojException.NotFound("Response not found", null), 404),
            Arguments.of(new MojException.InternalServerError("Response lookup failed", null), 500)
        );
    }

    private static List<JurorPersonalDetailsDto> validRequests() {
        return List.of(
            validRequest(ReplyMethod.PAPER),
            validRequest(ReplyMethod.DIGITAL),
            requestWithOptionalDetails(ReplyMethod.PAPER),
            requestWithOptionalDetails(ReplyMethod.DIGITAL)
        );
    }

    private static JurorPersonalDetailsDto requestWithOptionalDetails(ReplyMethod replyMethod) {
        JurorPersonalDetailsDto request = validRequest(replyMethod);
        request.setTitle("Dr");
        request.setAddressLineTwo("Test Village");
        request.setAddressLineThree("Test District");
        request.setAddressTown("Test Town");
        request.setAddressCounty("Test County");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setPrimaryPhone("07700900123");
        request.setSecondaryPhone("07700900456");
        request.setEmailAddress("juror@example.com");
        request.setThirdParty(JurorPaperResponseDto.ThirdParty.builder()
            .thirdPartyFName("Another")
            .thirdPartyLName("Person")
            .mainPhone("07700900789")
            .otherPhone("07700900987")
            .emailAddress("third.party@example.com")
            .relationship("Friend")
            .thirdPartyReason("Other")
            .thirdPartyOtherReason("Needs assistance")
            .useJurorPhoneDetails(false)
            .useJurorEmailDetails(false)
            .build());
        return request;
    }

    private static JurorPersonalDetailsDto validRequest(ReplyMethod replyMethod) {
        JurorPersonalDetailsDto request = new JurorPersonalDetailsDto();
        request.setReplyMethod(replyMethod);
        request.setFirstName("Test");
        request.setLastName("Person");
        request.setAddressLineOne("1 First Street");
        request.setAddressPostcode("MK5 6LA");
        return request;
    }

    private BureauJwtAuthentication authenticate(BureauJwtPayload payload) {
        BureauJwtAuthentication authentication = mock(BureauJwtAuthentication.class);
        when(authentication.getPrincipal()).thenReturn(payload);

        SecurityContext context = new SecurityContextImpl(authentication);
        when(securityContextStrategy.getContext()).thenReturn(context);

        return authentication;
    }

    @TestConfiguration
    static class TestMvcConfiguration implements WebMvcConfigurer {
        private final SecurityContextHolderStrategy securityContextHolderStrategy;

        TestMvcConfiguration(SecurityContextHolderStrategy securityContextHolderStrategy) {
            this.securityContextHolderStrategy = securityContextHolderStrategy;
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            AuthenticationPrincipalArgumentResolver resolver = new AuthenticationPrincipalArgumentResolver();

            resolver.setSecurityContextHolderStrategy(securityContextHolderStrategy);
            resolvers.add(resolver);
        }
    }
}
