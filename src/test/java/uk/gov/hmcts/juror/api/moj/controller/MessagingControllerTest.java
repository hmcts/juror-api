package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.ExportContactDetailsRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.MessageSendRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.ViewMessageTemplateDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.MessagingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MessagingController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {MessagingController.class, RestResponseEntityExceptionHandler.class,
    BulkServiceImpl.class})
@DisplayName("Controller: " + MessagingControllerTest.BASE_URL)
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class MessagingControllerTest {
    protected static final String BASE_URL = "/api/v1/moj/messages";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessagingService messagingService;

    @Nested
    @DisplayName("GET " + GetMessageDetails.URL)
    class GetMessageDetails {
        protected static final String URL = BASE_URL + "/view/{message_type}/{loc_code}";

        @ParameterizedTest(name = "Typical - {0}")
        @EnumSource(MessageType.class)
        void positiveTypical(MessageType messageType) throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;
            ViewMessageTemplateDto viewMessageTemplateDto = ViewMessageTemplateDto.builder()
                .sendType(MessageType.SendType.EMAIL)
                .messageTemplateEnglish("Some English Placeholder")
                .build();
            when(messagingService.getViewMessageTemplateDto(messageType, locCode))
                .thenReturn(viewMessageTemplateDto);

            mockMvc.perform(get(URL, messageType.name(), locCode))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(viewMessageTemplateDto)));

            verify(messagingService, times(1))
                .getViewMessageTemplateDto(messageType, locCode);
        }


        @Test
        void negativeInvalidMessageType() throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;

            mockMvc.perform(get(URL, "INVALID", locCode))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "INVALID is the incorrect data type or is not in the expected format (message_type)")));
            verifyNoInteractions(messagingService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(get(URL, MessageType.BAD_WEATHER_COURT.name(), "INVALID"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "getMessageDetails.locCode: size must be between 3 and 3")));
            verifyNoInteractions(messagingService);
        }
    }

    @Nested
    @DisplayName("POST " + GetMessageDetailsPopulated.URL)
    class GetMessageDetailsPopulated {
        protected static final String URL = BASE_URL + "/view/{message_type}/{loc_code}/populated";

        @ParameterizedTest(name = "Typical - {0}")
        @EnumSource(MessageType.class)
        void positiveTypical(MessageType messageType) throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;
            ViewMessageTemplateDto viewMessageTemplateDto = ViewMessageTemplateDto.builder()
                .sendType(MessageType.SendType.EMAIL)
                .messageTemplateEnglish("Some English Placeholder")
                .build();
            Map<String, String> placeholders = Map.of("Key", "value", "Key2", "value2");
            when(messagingService.getViewMessageTemplateDtoPopulated(messageType, locCode, placeholders))
                .thenReturn(viewMessageTemplateDto);
            mockMvc.perform(post(URL, messageType.name(), locCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(placeholders)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(viewMessageTemplateDto)));

            verify(messagingService, times(1))
                .getViewMessageTemplateDtoPopulated(messageType, locCode, placeholders);
        }


        @Test
        void negativeInvalidMessageType() throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;

            mockMvc.perform(post(URL, "INVALID", locCode))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "INVALID is the incorrect data type or is not in the expected format (message_type)")));
            verifyNoInteractions(messagingService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(post(URL, MessageType.BAD_WEATHER_COURT.name(), "INVALID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(Map.of("key", "value"))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "getMessageDetailsPopulated.locCode: size must be between 3 and 3")));
            verifyNoInteractions(messagingService);
        }

        @Test
        @SuppressWarnings("PMD.UseConcurrentHashMap")
        void negativeInvalidPayload() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("key", null);
            mockMvc.perform(post(URL, MessageType.BAD_WEATHER_COURT.name(), TestConstants.VALID_COURT_LOCATION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "getMessageDetailsPopulated.placeholderValues[key].<map value>: must not be null")));
            verifyNoInteractions(messagingService);
        }
    }

    @Nested
    @DisplayName("POST " + PostSearch.URL)
    class PostSearch {
        protected static final String URL = BASE_URL + "/search/{loc_code}";

        protected MessageSearch getValidPayload() {
            return MessageSearch.builder()
                .trialNumber("T000000001")
                .pageNumber(1)
                .pageLimit(2)
                .build();
        }

        @Test
        void positiveTypicalSimpleResponseNull() throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;
            PaginatedList<? extends JurorToSendMessageBase> paginatedList = new PaginatedList<>();
            paginatedList.setTotalItems(10L);
            paginatedList.setCurrentPage(1L);

            MessageSearch messageSearch = getValidPayload();

            doReturn(paginatedList).when(messagingService).search(messageSearch, locCode, false);

            mockMvc.perform(post(URL, locCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(messageSearch)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(paginatedList)));

            verify(messagingService, times(1))
                .search(messageSearch, locCode, false);
        }

        @Test
        void positiveTypicalSimpleResponseTrue() throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;
            PaginatedList<? extends JurorToSendMessageBase> paginatedList = new PaginatedList<>();
            paginatedList.setTotalItems(10L);
            paginatedList.setCurrentPage(1L);

            MessageSearch messageSearch = getValidPayload();

            doReturn(paginatedList).when(messagingService).search(messageSearch, locCode, true);

            mockMvc.perform(post(URL, locCode)
                    .queryParam("simple_response", "true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(messageSearch)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(paginatedList)));

            verify(messagingService, times(1))
                .search(messageSearch, locCode, true);
        }

        @Test
        void positiveTypicalSimpleResponseFalse() throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;
            PaginatedList<? extends JurorToSendMessageBase> paginatedList = new PaginatedList<>();
            paginatedList.setTotalItems(10L);
            paginatedList.setCurrentPage(1L);

            MessageSearch messageSearch = getValidPayload();

            doReturn(paginatedList).when(messagingService).search(messageSearch, locCode, false);

            mockMvc.perform(post(URL, locCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .queryParam("simple_response", "false")
                    .content(TestUtils.asJsonString(messageSearch)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(paginatedList)));

            verify(messagingService, times(1))
                .search(messageSearch, locCode, false);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(post(URL, "INVALID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "postSearch.locCode: size must be between 3 and 3")));
            verifyNoInteractions(messagingService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            MessageSearch search = getValidPayload();
            search.setPageLimit(0);
            mockMvc.perform(post(URL, TestConstants.VALID_COURT_LOCATION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(search)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", CoreMatchers.is("pageLimit")))
                .andExpect(jsonPath("$.errors[0].message",
                    CoreMatchers.is("must be greater than or equal to 1")));
            verifyNoInteractions(messagingService);
        }
    }

    @Nested
    @DisplayName("POST " + SendMessage.URL)
    class SendMessage {
        protected static final String URL = BASE_URL + "/send/{message_type}/{loc_code}";

        protected MessageSendRequest getValidPayload() {
            return MessageSendRequest.builder()
                .jurors(
                    List.of(MessageSendRequest.JurorAndSendType.builder()
                        .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                        .poolNumber(TestConstants.VALID_POOL_NUMBER)
                        .type(MessageType.SendType.EMAIL_AND_SMS)
                        .build()
                    )
                )
                .build();
        }

        @ParameterizedTest(name = "Typical - {0}")
        @EnumSource(MessageType.class)
        void positiveTypical(MessageType messageType) throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;

            MessageSendRequest request = getValidPayload();
            mockMvc.perform(post(URL, messageType.name(), locCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

            verify(messagingService, times(1))
                .send(messageType, locCode, request);
        }

        @Test
        void negativeInvalidMessageType() throws Exception {
            mockMvc.perform(post(URL, "INVALID", TestConstants.VALID_COURT_LOCATION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "INVALID is the incorrect data type or is not in the expected format (message_type)")));
            verifyNoInteractions(messagingService);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(post(URL, MessageType.BAD_WEATHER_COURT.name(), "INVALID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "sendMessage.locCode: size must be between 3 and 3")));
            verifyNoInteractions(messagingService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            MessageSendRequest request = getValidPayload();
            request.getJurors().get(0).setJurorNumber(TestConstants.INVALID_JUROR_NUMBER);
            mockMvc.perform(post(URL, MessageType.BAD_WEATHER_COURT.name(),
                    TestConstants.VALID_COURT_LOCATION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", CoreMatchers.is("jurors[0].jurorNumber")))
                .andExpect(jsonPath("$.errors[0].message",
                    CoreMatchers.is("must match \"^\\d{9}$\"")));
            verifyNoInteractions(messagingService);
        }
    }


    @Nested
    @DisplayName("POST " + ToCsv.URL)
    class ToCsv {
        protected static final String URL = BASE_URL + "/csv/{loc_code}";

        protected ExportContactDetailsRequest getValidPayload() {
            return ExportContactDetailsRequest.builder()
                .exportItems(List.of(
                    ExportContactDetailsRequest.ExportItems.JUROR_NUMBER
                ))
                .jurors(List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                        .poolNumber(TestConstants.VALID_POOL_NUMBER)
                        .build()
                ))
                .build();
        }

        @Test
        void positiveTypical() throws Exception {
            final String locCode = TestConstants.VALID_COURT_LOCATION;

            ExportContactDetailsRequest request = getValidPayload();
            String response = "Some example csv response";
            doReturn(response).when(messagingService).exportContactDetails(locCode, request);

            mockMvc.perform(post(URL, locCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Content-Disposition",
                    "attachment; filename=juror_export_details.csv"))
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(content().string(response));

            verify(messagingService, times(1))
                .exportContactDetails(locCode, request);
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            mockMvc.perform(post(URL, "INVALID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(getValidPayload())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                    CoreMatchers.is(
                        "toCsv.locCode: size must be between 3 and 3")));
            verifyNoInteractions(messagingService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            ExportContactDetailsRequest request = getValidPayload();
            request.setJurors(null);
            mockMvc.perform(post(URL, TestConstants.VALID_COURT_LOCATION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", CoreMatchers.is("jurors")))
                .andExpect(jsonPath("$.errors[0].message",
                    CoreMatchers.is("must not be empty")));
            verifyNoInteractions(messagingService);
        }
    }
}
