package uk.gov.hmcts.juror.api.moj.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorStatusValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.CompleteServiceService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.api.moj.controller.CompleteServiceControllerTest.CompleteService.COMPLETE_SERVICE_URL;
import static uk.gov.hmcts.juror.api.moj.controller.CompleteServiceControllerTest.ValidateCompleteService.VALIDATE_COMPLETE_SERVICE_URL;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CompleteServiceController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        CompleteServiceController.class,
        RestResponseEntityExceptionHandler.class,
        BulkServiceImpl.class
    }
)
@DisplayName("Controller: " + CompleteServiceControllerTest.BASE_URL)
@SuppressWarnings({
    "PMD.ExcessiveImports"
})
class CompleteServiceControllerTest {
    public static final String BASE_URL = "/api/v1/moj/complete-service";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BulkService bulkService;

    @MockBean
    private CompleteServiceService completeServiceService;

    @Nested
    @DisplayName("PATCH " + COMPLETE_SERVICE_URL)
    class CompleteService {
        public static final String COMPLETE_SERVICE_URL = BASE_URL
            + "/{poolNumber}/complete";

        public static Stream<Arguments> invalidPayloadArgumentSource() {
            return Stream.of(
                arguments("Empty payload", ""),
                arguments("Missing completion date",
                    TestUtils.asJsonString(CompleteServiceJurorNumberListDto.builder()
                        .completionDate(null)
                        .jurorNumbers(List.of(TestConstants.VALID_JUROR_NUMBER))
                        .build())),
                arguments("Missing juror_numbers",
                    TestUtils.asJsonString(CompleteServiceJurorNumberListDto.builder()
                        .completionDate(LocalDate.now())
                        .jurorNumbers(null)
                        .build())),
                arguments("Empty juror_number",
                    TestUtils.asJsonString(CompleteServiceJurorNumberListDto.builder()
                        .completionDate(LocalDate.now())
                        .jurorNumbers(List.of())
                        .build())),
                arguments("Invalid juror_number",
                    TestUtils.asJsonString(CompleteServiceJurorNumberListDto.builder()
                        .completionDate(LocalDate.now())
                        .jurorNumbers(List.of("abc"))
                        .build()))
            );
        }


        @Test
        void positiveAllValidResponse() throws Exception {
            CompleteServiceJurorNumberListDto payload = new CompleteServiceJurorNumberListDto();
            payload.setJurorNumbers(List.of("123456789", "123456788"));
            payload.setCompletionDate(LocalDate.now());


            mockMvc.perform(patch(COMPLETE_SERVICE_URL,
                    TestConstants.VALID_POOL_NUMBER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

            verify(completeServiceService, times(1)).completeService(TestConstants.VALID_POOL_NUMBER, payload);
        }

        @ParameterizedTest(name = "Invalid Payload: {0}")
        @MethodSource("invalidPayloadArgumentSource")
        @DisplayName("Invalid Payload")
        void negativeInvalidPayload(String title, String payload) throws Exception {
            mockMvc.perform(patch(COMPLETE_SERVICE_URL,
                    TestConstants.VALID_POOL_NUMBER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(completeServiceService);
        }
    }

    @Nested
    @DisplayName("POST " + VALIDATE_COMPLETE_SERVICE_URL)
    class ValidateCompleteService {
        public static final String VALIDATE_COMPLETE_SERVICE_URL = BASE_URL
            + "/{poolNumber}/validate";

        @Test
        void positiveAllValidResponse() throws Exception {
            JurorNumberListDto payload = new JurorNumberListDto();
            payload.setJurorNumbers(List.of("123456789", "123456788"));

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto =
                new CompleteServiceValidationResponseDto();

            JurorStatusValidationResponseDto jurorStatusValidationResponseDto1 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456789")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(IJurorStatus.RESPONDED)
                    .build();
            JurorStatusValidationResponseDto jurorStatusValidationResponseDto2 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456788")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(IJurorStatus.RESPONDED)
                    .build();

            completeServiceValidationResponseDto.addValid(jurorStatusValidationResponseDto1);
            completeServiceValidationResponseDto.addValid(jurorStatusValidationResponseDto2);


            when(completeServiceService.validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, payload))
                .thenReturn(completeServiceValidationResponseDto);

            ResultActions resultActions = mockMvc.perform(post(VALIDATE_COMPLETE_SERVICE_URL,
                    TestConstants.VALID_POOL_NUMBER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$.valid.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$.invalid_not_responded.size()", CoreMatchers.is(0)));

            assertJurorStatusValidationResponseDto(resultActions, "$.valid[0]", jurorStatusValidationResponseDto1);
            assertJurorStatusValidationResponseDto(resultActions, "$.valid[1]", jurorStatusValidationResponseDto2);
        }

        @Test
        void positiveAllInValidResponse() throws Exception {
            JurorNumberListDto payload = new JurorNumberListDto();
            payload.setJurorNumbers(List.of("123456789", "123456788", "123456787"));

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto =
                new CompleteServiceValidationResponseDto();

            JurorStatusValidationResponseDto jurorStatusValidationResponseDto1 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456789")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(1)
                    .build();
            JurorStatusValidationResponseDto jurorStatusValidationResponseDto2 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456788")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(RandomUtils.nextInt(2, 12))
                    .build();
            JurorStatusValidationResponseDto jurorStatusValidationResponseDto3 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456787")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(RandomUtils.nextInt(2, 12))
                    .build();

            completeServiceValidationResponseDto.addInvalidNotResponded(jurorStatusValidationResponseDto1);
            completeServiceValidationResponseDto.addInvalidNotResponded(jurorStatusValidationResponseDto2);
            completeServiceValidationResponseDto.addInvalidNotResponded(jurorStatusValidationResponseDto3);


            when(completeServiceService.validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, payload))
                .thenReturn(completeServiceValidationResponseDto);

            ResultActions resultActions = mockMvc.perform(post(VALIDATE_COMPLETE_SERVICE_URL,
                    TestConstants.VALID_POOL_NUMBER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$.valid.size()", CoreMatchers.is(0)))
                .andExpect(jsonPath("$.invalid_not_responded.size()", CoreMatchers.is(3)));

            assertJurorStatusValidationResponseDto(resultActions, "$.invalid_not_responded[0]",
                jurorStatusValidationResponseDto1);
            assertJurorStatusValidationResponseDto(resultActions, "$.invalid_not_responded[1]",
                jurorStatusValidationResponseDto2);
            assertJurorStatusValidationResponseDto(resultActions, "$.invalid_not_responded[2]",
                jurorStatusValidationResponseDto3);
        }


        @Test
        void positiveAllMixValidAndInvalidResponse() throws Exception {
            JurorNumberListDto payload = new JurorNumberListDto();
            payload.setJurorNumbers(List.of("123456789", "123456788", "123456787"));

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto =
                new CompleteServiceValidationResponseDto();

            JurorStatusValidationResponseDto jurorStatusValidationResponseDto1 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456789")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(1)
                    .build();
            JurorStatusValidationResponseDto jurorStatusValidationResponseDto2 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456788")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(IJurorStatus.RESPONDED)
                    .build();
            JurorStatusValidationResponseDto jurorStatusValidationResponseDto3 =
                JurorStatusValidationResponseDto.builder()
                    .jurorNumber("123456787")
                    .firstName(RandomStringUtils.randomAlphabetic(10))
                    .lastName(RandomStringUtils.randomAlphabetic(10))
                    .status(RandomUtils.nextInt(2, 12))
                    .build();

            completeServiceValidationResponseDto.addInvalidNotResponded(jurorStatusValidationResponseDto1);
            completeServiceValidationResponseDto.addValid(jurorStatusValidationResponseDto2);
            completeServiceValidationResponseDto.addInvalidNotResponded(jurorStatusValidationResponseDto3);


            when(completeServiceService.validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, payload))
                .thenReturn(completeServiceValidationResponseDto);

            ResultActions resultActions = mockMvc.perform(post(VALIDATE_COMPLETE_SERVICE_URL,
                    TestConstants.VALID_POOL_NUMBER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(jsonPath("$.valid.size()", CoreMatchers.is(1)))
                .andExpect(jsonPath("$.invalid_not_responded.size()", CoreMatchers.is(2)));

            assertJurorStatusValidationResponseDto(resultActions, "$.invalid_not_responded[0]",
                jurorStatusValidationResponseDto1);
            assertJurorStatusValidationResponseDto(resultActions, "$.valid[0]", jurorStatusValidationResponseDto2);
            assertJurorStatusValidationResponseDto(resultActions, "$.invalid_not_responded[1]",
                jurorStatusValidationResponseDto3);
        }

        public static Stream<Arguments> invalidPayloadArgumentSource() {
            return Stream.of(
                arguments("Empty payload", ""),
                arguments("Missing juror_numbers",
                    TestUtils.asJsonString(JurorNumberListDto.builder()
                        .jurorNumbers(null)
                        .build())),
                arguments("Empty juror_number",
                    TestUtils.asJsonString(JurorNumberListDto.builder()
                        .jurorNumbers(List.of())
                        .build())),
                arguments("Invalid juror_number",
                    TestUtils.asJsonString(JurorNumberListDto.builder()
                        .jurorNumbers(List.of("abc"))
                        .build()))
            );
        }

        @ParameterizedTest(name = "Invalid Payload: {0}")
        @MethodSource("invalidPayloadArgumentSource")
        @DisplayName("Invalid Payload")
        void negativeInvalidPayload(String title, String payload) throws Exception {
            mockMvc.perform(post(VALIDATE_COMPLETE_SERVICE_URL,
                    TestConstants.VALID_POOL_NUMBER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(completeServiceService);
        }
    }


    @Nested
    @DisplayName("PATCH " + UncompleteService.URL)
    class UncompleteService {
        public static final String URL = BASE_URL + "/uncomplete";

        @Test
        void positiveTypical() throws Exception {
            JurorAndPoolRequest jurorAndPoolRequest = JurorAndPoolRequest.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();

            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(List.of(jurorAndPoolRequest))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));


            verify(completeServiceService, times(1))
                .uncompleteJurorsService(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER);
        }

        @Test
        void positiveMultiple() throws Exception {
            JurorAndPoolRequest jurorAndPoolRequest1 = JurorAndPoolRequest.builder()
                .jurorNumber("111111111")
                .poolNumber("111111112")
                .build();


            JurorAndPoolRequest jurorAndPoolRequest2 = JurorAndPoolRequest.builder()
                .jurorNumber("111111113")
                .poolNumber("111111114")
                .build();


            JurorAndPoolRequest jurorAndPoolRequest3 = JurorAndPoolRequest.builder()
                .jurorNumber("111111115")
                .poolNumber("111111116")
                .build();

            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(List.of(jurorAndPoolRequest1,
                        jurorAndPoolRequest2, jurorAndPoolRequest3))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));


            verify(completeServiceService, times(1))
                .uncompleteJurorsService(
                    "111111111", "111111112");
            verify(completeServiceService, times(1))
                .uncompleteJurorsService(
                    "111111113", "111111114");
            verify(completeServiceService, times(1))
                .uncompleteJurorsService(
                    "111111115", "111111116");
        }

        @Test
        void negativeBadPayload() throws Exception {
            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(List.of())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(completeServiceService);
        }
    }




    @Test
    @SuppressWarnings({
        "PMD.JUnitTestsShouldIncludeAssert" //False positive
    })
    void negativeNotFound() throws Exception {
        JurorNumberListDto payload = new JurorNumberListDto();
        payload.setJurorNumbers(List.of("123456789", "123456788"));


        MojException.NotFound notFoundException = new MojException.NotFound("Juror number 123456788 not found in "
            + "pool " + TestConstants.VALID_POOL_NUMBER, null);

        when(completeServiceService.validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, payload))
            .thenThrow(notFoundException);

        mockMvc.perform(post(VALIDATE_COMPLETE_SERVICE_URL,
                TestConstants.VALID_POOL_NUMBER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(payload)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound());

    }

    private void assertJurorStatusValidationResponseDto(
        ResultActions resultActions, String prefix,
        JurorStatusValidationResponseDto jurorStatusValidationResponseDto) throws Exception {

        resultActions
            .andExpect(jsonPath(prefix + ".size()", CoreMatchers.is(4)))
            .andExpect(jsonPath(prefix + ".juror_number",
                CoreMatchers.is(jurorStatusValidationResponseDto.getJurorNumber())))
            .andExpect(jsonPath(prefix + ".first_name",
                CoreMatchers.is(jurorStatusValidationResponseDto.getFirstName())))
            .andExpect(
                jsonPath(prefix + ".last_name", CoreMatchers.is(jurorStatusValidationResponseDto.getLastName())))
            .andExpect(jsonPath(prefix + ".status", CoreMatchers.is(jurorStatusValidationResponseDto.getStatus())));
    }
}
