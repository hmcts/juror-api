package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.ExportContactDetailsRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.MessageSendRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.ViewMessageTemplateDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.messages.DataType;
import uk.gov.hmcts.juror.api.moj.domain.messages.Message;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessagePlaceholders;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageTemplate;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageTemplateRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.INVALID_SEND_TYPE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_MUST_HAVE_EMAIL;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_MUST_HAVE_PHONE_NUMBER;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_NOT_APART_OF_TRIAL;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.PLACEHOLDER_MUST_HAVE_VALUE;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports",
    "PMD.CouplingBetweenObjects",
    "PMD.TooManyMethods",
    "unchecked"
})
class MessagingServiceImplTest {

    private MessageTemplateRepository messageTemplateRepository;
    private TrialRepository trialRepository;
    private PanelRepository panelRepository;
    private MessageRepository messagesRepository;
    private WelshCourtLocationRepository welshCourtLocationRepository;
    private CourtLocationRepository courtLocationRepository;
    private JurorHistoryService historyService;
    private Clock clock;
    private JurorRepository jurorRepository;

    private MessagingServiceImpl messagingService;

    @BeforeEach
    void beforeEach() {
        messageTemplateRepository = mock(MessageTemplateRepository.class);
        trialRepository = mock(TrialRepository.class);
        panelRepository = mock(PanelRepository.class);
        messagesRepository = mock(MessageRepository.class);
        welshCourtLocationRepository = mock(WelshCourtLocationRepository.class);
        courtLocationRepository = mock(CourtLocationRepository.class);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        jurorRepository = mock(JurorRepository.class);
        historyService = mock(JurorHistoryService.class);
        this.messagingService = spy(new MessagingServiceImpl(
            messageTemplateRepository,
            trialRepository,
            panelRepository,
            messagesRepository,
            welshCourtLocationRepository,
            courtLocationRepository,
            clock,
            jurorRepository,
            historyService));
    }

    @Nested
    @DisplayName("MessageTemplate getMessageTemplate(MessageType messageType, boolean english)")
    class GetMessageTemplate {

        @ParameterizedTest
        @EnumSource(MessageType.class)
        void positiveTemplateFoundEnglish(MessageType messageType) {
            MessageTemplate messageTemplate = mock(MessageTemplate.class);

            doReturn(Optional.of(messageTemplate)).when(messageTemplateRepository)
                .findById(messageType.getEnglishMessageId());
            assertThat(messagingService.getMessageTemplate(messageType, true))
                .isEqualTo(messageTemplate);
        }

        @ParameterizedTest
        @EnumSource(MessageType.class)
        void positiveTemplateFoundWelsh(MessageType messageType) {
            MessageTemplate messageTemplate = mock(MessageTemplate.class);

            doReturn(Optional.of(messageTemplate)).when(messageTemplateRepository)
                .findById(messageType.getWelshMessageId());
            assertThat(messagingService.getMessageTemplate(messageType, false))
                .isEqualTo(messageTemplate);
        }

        @ParameterizedTest
        @EnumSource(MessageType.class)
        void negativeTemplateNotFoundEnglish(MessageType messageType) {
            doReturn(Optional.empty()).when(messageTemplateRepository)
                .findById(messageType.getEnglishMessageId());

            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> messagingService.getMessageTemplate(messageType, true),
                    "Exception should be thrown when message template is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("English message template not found for " + messageType.name());
            assertThat(exception.getCause()).isNull();
        }

        @ParameterizedTest
        @EnumSource(MessageType.class)
        void negativeTemplateNotFoundWelsh(MessageType messageType) {
            doReturn(Optional.empty()).when(messageTemplateRepository)
                .findById(messageType.getEnglishMessageId());

            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> messagingService.getMessageTemplate(messageType, false),
                    "Exception should be thrown when message template is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("Welsh message template not found for " + messageType.name());
            assertThat(exception.getCause()).isNull();
        }

    }

    @Nested
    @DisplayName("public ViewMessageTemplateDto getViewMessageTemplateDto(MessageType messageType, String locCode)")
    class GetViewMessageTemplateDto {
        @Test
        void positiveEnglishLocation() {
            final MessageType messageType = MessageType.FAILED_TO_ATTEND_COURT;
            final String englishText = "some english text";
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);

            doReturn(englishMessageTemplate).when(messagingService).getMessageTemplate(messageType, true);
            when(englishMessageTemplate.getText()).thenReturn(englishText);


            ViewMessageTemplateDto.Placeholder englishPlaceHolder1 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(englishPlaceHolder1.isEditable()).thenReturn(true);
            ViewMessageTemplateDto.Placeholder englishPlaceHolder2 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(englishPlaceHolder2.isEditable()).thenReturn(false);
            ViewMessageTemplateDto.Placeholder englishPlaceHolder3 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(englishPlaceHolder3.isEditable()).thenReturn(true);

            List<ViewMessageTemplateDto.Placeholder> englishPlaceholders = List.of(
                englishPlaceHolder1, englishPlaceHolder2, englishPlaceHolder3
            );
            doReturn(englishPlaceholders).when(messagingService)
                .getRequiredPlaceholders(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION);
            doReturn(false).when(messagingService)
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);

            ViewMessageTemplateDto response =
                messagingService.getViewMessageTemplateDto(messageType,
                    TestConstants.VALID_COURT_LOCATION);
            assertThat(response).isNotNull();
            assertThat(response.getMessageTemplateEnglish()).isEqualTo(englishText);
            assertThat(response.getMessageTemplateWelsh()).isNull();
            assertThat(response.getSendType()).isEqualTo(MessageType.SendType.EMAIL_AND_SMS);

            assertThat(response.getPlaceholders()).hasSize(2).containsAll(List.of(
                englishPlaceHolder1, englishPlaceHolder3
            ));

            verify(messagingService, times(1))
                .getRequiredPlaceholders(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void positiveWelshLocation() {
            final MessageType messageType = MessageType.SENTENCING_DATE_COURT;
            final String englishText = "some english text";
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);

            doReturn(englishMessageTemplate).when(messagingService).getMessageTemplate(messageType, true);
            when(englishMessageTemplate.getText()).thenReturn(englishText);


            ViewMessageTemplateDto.Placeholder englishPlaceHolder1 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(englishPlaceHolder1.isEditable()).thenReturn(true);
            ViewMessageTemplateDto.Placeholder englishPlaceHolder2 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(englishPlaceHolder2.isEditable()).thenReturn(false);
            ViewMessageTemplateDto.Placeholder englishPlaceHolder3 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(englishPlaceHolder3.isEditable()).thenReturn(true);

            List<ViewMessageTemplateDto.Placeholder> englishPlaceholders = List.of(
                englishPlaceHolder1, englishPlaceHolder2, englishPlaceHolder3
            );
            doReturn(englishPlaceholders).when(messagingService)
                .getRequiredPlaceholders(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION);


            final String welshText = "rhywfaint o destun cymraeg";
            MessageTemplate welshMessageTemplate = mock(MessageTemplate.class);

            doReturn(welshMessageTemplate).when(messagingService).getMessageTemplate(messageType, false);
            when(welshMessageTemplate.getText()).thenReturn(welshText);


            ViewMessageTemplateDto.Placeholder welshPlaceHolder1 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(welshPlaceHolder1.isEditable()).thenReturn(false);
            ViewMessageTemplateDto.Placeholder welshPlaceHolder2 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(welshPlaceHolder2.isEditable()).thenReturn(true);
            ViewMessageTemplateDto.Placeholder welshPlaceHolder3 = mock(ViewMessageTemplateDto.Placeholder.class);
            when(welshPlaceHolder3.isEditable()).thenReturn(true);

            List<ViewMessageTemplateDto.Placeholder> welshPlaceholders = List.of(
                welshPlaceHolder1, welshPlaceHolder2, welshPlaceHolder3
            );
            doReturn(welshPlaceholders).when(messagingService)
                .getRequiredPlaceholders(welshMessageTemplate, TestConstants.VALID_COURT_LOCATION);


            doReturn(true).when(messagingService)
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);

            ViewMessageTemplateDto response =
                messagingService.getViewMessageTemplateDto(messageType,
                    TestConstants.VALID_COURT_LOCATION);
            assertThat(response).isNotNull();
            assertThat(response.getMessageTemplateEnglish()).isEqualTo(englishText);
            assertThat(response.getMessageTemplateWelsh()).isEqualTo(welshText);
            assertThat(response.getSendType()).isEqualTo(MessageType.SendType.EMAIL);

            assertThat(response.getPlaceholders()).hasSize(4).containsAll(List.of(
                englishPlaceHolder1, englishPlaceHolder3, welshPlaceHolder2, welshPlaceHolder3
            ));

            verify(messagingService, times(1))
                .getRequiredPlaceholders(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .getRequiredPlaceholders(welshMessageTemplate, TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);
        }
    }

    @Nested
    @DisplayName(
        "public ViewMessageTemplateDto getViewMessageTemplateDtoPopulated(MessageType messageType, String locCode,\n"
            + "                                                                     Map<String, String> "
            + "overridePlaceholders)")
    class GetViewMessageTemplateDtoPopulated {
        @Test
        void positiveEnglishLocation() {
            final MessageType messageType = MessageType.FAILED_TO_ATTEND_COURT;
            final String englishText = "some english text";
            Map<String, String> overridePlaceholders = Map.of("key", "value");
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);

            doReturn(englishMessageTemplate).when(messagingService).getMessageTemplate(messageType, true);

            doReturn(englishText).when(messagingService)
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION,
                    false, overridePlaceholders);


            doReturn(false).when(messagingService)
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);

            ViewMessageTemplateDto response =
                messagingService.getViewMessageTemplateDtoPopulated(messageType,
                    TestConstants.VALID_COURT_LOCATION, overridePlaceholders);
            assertThat(response).isNotNull();
            assertThat(response.getMessageTemplateEnglish()).isEqualTo(englishText);
            assertThat(response.getMessageTemplateWelsh()).isNull();
            assertThat(response.getSendType()).isEqualTo(MessageType.SendType.EMAIL_AND_SMS);

            assertThat(response.getPlaceholders()).isNull();

            verify(messagingService, times(1))
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION,
                    false, overridePlaceholders);

            verify(messagingService, times(1))
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void positiveWelshLocation() {
            final MessageType messageType = MessageType.SENTENCING_INVITE_COURT;
            final String englishText = "some english text";
            Map<String, String> overridePlaceholders = Map.of("key", "value");
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);

            doReturn(englishMessageTemplate).when(messagingService).getMessageTemplate(messageType, true);

            doReturn(englishText).when(messagingService)
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION,
                    false, overridePlaceholders);

            MessageTemplate welshMessageTemplate = mock(MessageTemplate.class);

            doReturn(welshMessageTemplate).when(messagingService).getMessageTemplate(messageType, false);

            final String welshText = "rhywfaint o destun cymraeg";
            doReturn(welshText).when(messagingService)
                .getMessageTemplatePopulated(welshMessageTemplate, TestConstants.VALID_COURT_LOCATION,
                    true, overridePlaceholders);


            doReturn(true).when(messagingService)
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);

            ViewMessageTemplateDto response =
                messagingService.getViewMessageTemplateDtoPopulated(messageType,
                    TestConstants.VALID_COURT_LOCATION, overridePlaceholders);
            assertThat(response).isNotNull();
            assertThat(response.getMessageTemplateEnglish()).isEqualTo(englishText);
            assertThat(response.getMessageTemplateWelsh()).isEqualTo(welshText);
            assertThat(response.getSendType()).isEqualTo(MessageType.SendType.EMAIL);

            assertThat(response.getPlaceholders()).isNull();

            verify(messagingService, times(1))
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION,
                    false, overridePlaceholders);
            verify(messagingService, times(1))
                .getMessageTemplatePopulated(welshMessageTemplate, TestConstants.VALID_COURT_LOCATION,
                    true, overridePlaceholders);

            verify(messagingService, times(1))
                .isWelshLocation(TestConstants.VALID_COURT_LOCATION);
        }
    }

    @Nested
    @DisplayName("String getMessageTemplatePopulated(MessageTemplate messageTemplate,\n"
        + "                                       String locCode,\n"
        + "                                       boolean isWelsh,\n"
        + "                                       Map<String, String> overridePlaceholders)")
    class GetMessageTemplatePopulated {
        @Test
        void positiveTypical() {
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            Map<String, String> overridePlaceholders = Map.of("key", "value");
            List<ViewMessageTemplateDto.Placeholder> requiredPlaceholders = List.of(
                mock(ViewMessageTemplateDto.Placeholder.class),
                mock(ViewMessageTemplateDto.Placeholder.class),
                mock(ViewMessageTemplateDto.Placeholder.class)
            );

            Map<String, String> populatedPlaceholders = Map.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3",
                "key4", "value4"
            );
            final String response = "some response";

            doReturn(requiredPlaceholders).when(messagingService)
                .getRequiredPlaceholders(messageTemplate, TestConstants.VALID_COURT_LOCATION);
            doReturn(populatedPlaceholders).when(messagingService)
                .getPopulatedPlaceholders(requiredPlaceholders, overridePlaceholders, true);
            doReturn(response).when(messagingService)
                .getMessageTemplatePopulated(messageTemplate, populatedPlaceholders);

            assertThat(
                messagingService
                    .getMessageTemplatePopulated(
                        messageTemplate,
                        TestConstants.VALID_COURT_LOCATION,
                        true,
                        overridePlaceholders
                    )
            ).isEqualTo(response);


            verify(messagingService, times(1))
                .getRequiredPlaceholders(messageTemplate, TestConstants.VALID_COURT_LOCATION);
            verify(messagingService, times(1))
                .getPopulatedPlaceholders(requiredPlaceholders, overridePlaceholders, true);
            verify(messagingService, times(1))
                .getMessageTemplatePopulated(messageTemplate, populatedPlaceholders);
        }
    }

    @Nested
    @DisplayName("String getMessageTemplatePopulated(MessageTemplate messageTemplate,\n"
        + "                                       Map<String, String> placeholders)")
    class GetMessageTemplatePopulatedSimple {
        @Test
        void positiveTypical() {
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String templateText = "some template text <key_1> <key_2> some <key_3>";

            when(messageTemplate.getText()).thenReturn(templateText);
            Map<String, String> placeholders = Map.of(
                "<key_2>", "value 2",
                "<key_1>", "value 1",
                "<key_3>", "value 3"
            );

            assertThat(messagingService.getMessageTemplatePopulated(messageTemplate, placeholders))
                .isEqualTo("some template text value 1 value 2 some value 3");

            verify(messageTemplate, times(1)).getText();
            verifyNoMoreInteractions(messageTemplate);
        }

        @Test
        void positiveNoEmptyPlaceholders() {
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String templateText = "some template text <key_1> <key_2> some <key_3>";

            when(messageTemplate.getText()).thenReturn(templateText);
            Map<String, String> placeholders = Map.of();

            assertThat(messagingService.getMessageTemplatePopulated(messageTemplate, placeholders))
                .isEqualTo(templateText);

            verify(messageTemplate, times(1)).getText();
            verifyNoMoreInteractions(messageTemplate);
        }

        @Test
        void positiveNotFoundReplacement() {
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String templateText = "some template text <key_1> <key_2> some <key_3>";

            when(messageTemplate.getText()).thenReturn(templateText);
            Map<String, String> placeholders = Map.of(
                "<key_2>", "value 2",
                "<key_1>", "value 1",
                "<key_3>", "value 3",
                "<key_4>", "value 4"
            );

            assertThat(messagingService.getMessageTemplatePopulated(messageTemplate, placeholders))
                .isEqualTo("some template text value 1 value 2 some value 3");

            verify(messageTemplate, times(1)).getText();
            verifyNoMoreInteractions(messageTemplate);
        }
    }

    @Nested
    @DisplayName("public PaginatedList<JurorToSendMessage> search(MessageSearch messageSearch, String locCode,\n"
        + "                                                    boolean simpleResponse)")
    class Search {

        @Test
        void positiveHasResultsComplexSearch() {
            PaginatedList<JurorToSendMessageBase> response = mock(PaginatedList.class);
            doReturn(false).when(response).isEmpty();
            MessageSearch messageSearch = mock(MessageSearch.class);

            doReturn(response).when(messageTemplateRepository)
                .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, false, 500L);

            assertThat(messagingService.search(messageSearch, TestConstants.VALID_COURT_LOCATION, false))
                .isEqualTo(response);

            verify(messageTemplateRepository, times(1))
                .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, false, 500L);
        }

        @Test
        void positiveHasResultsSimpleSearch() {
            PaginatedList<JurorToSendMessageBase> response = mock(PaginatedList.class);
            doReturn(false).when(response).isEmpty();
            MessageSearch messageSearch = mock(MessageSearch.class);

            doReturn(response).when(messageTemplateRepository)
                .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, true, 500L);

            assertThat(messagingService.search(messageSearch, TestConstants.VALID_COURT_LOCATION, true))
                .isEqualTo(response);

            verify(messageTemplateRepository, times(1))
                .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, true, 500L);

        }

        @Test
        void negativeDoesNotHaveResults() {
            PaginatedList<JurorToSendMessageBase> response = mock(PaginatedList.class);
            doReturn(true).when(response).isEmpty();
            MessageSearch messageSearch = mock(MessageSearch.class);
            doReturn(response).when(messageTemplateRepository)
                .messageSearch(messageSearch, TestConstants.VALID_COURT_LOCATION, false, 500L);


            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> messagingService.search(
                        messageSearch, TestConstants.VALID_COURT_LOCATION, false),
                    "Exception should be thrown when result is empty");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("No jurors found that meet the search criteria");
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("void validateTrialNumber(String trialNumber, String locCode,\n"
        + "                             List<MessageSendRequest.JurorNumberAndSendType> jurors)")
    class ValidateTrialNumber {
        @Test
        void positiveTrialNumberNull() {
            assertDoesNotThrow(
                () -> messagingService
                    .validateTrialNumber(null, TestConstants.VALID_COURT_LOCATION,
                        List.of(mock(MessageSendRequest.JurorAndSendType.class))),
                "Should not throw exception"
            );
        }

        @Test
        void positiveJurorOnTrial() {
            MessageSendRequest.JurorAndSendType juror1 = MessageSendRequest.JurorAndSendType.builder()
                .jurorNumber("100000001")
                .poolNumber("200000001")
                .build();
            MessageSendRequest.JurorAndSendType juror2 = MessageSendRequest.JurorAndSendType.builder()
                .jurorNumber("100000002")
                .poolNumber("200000002")
                .build();
            MessageSendRequest.JurorAndSendType juror3 = MessageSendRequest.JurorAndSendType.builder()
                .jurorNumber("100000003")
                .poolNumber("200000003")
                .build();
            doReturn(true).when(messagingService).doesTrialExist(
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            );
            doReturn(true).when(messagingService).isJurorOnTrial(
                anyString(),
                anyString(),
                anyString()
            );

            assertDoesNotThrow(
                () -> messagingService
                    .validateTrialNumber(TestConstants.VALID_TRIAL_NUMBER, TestConstants.VALID_COURT_LOCATION,
                        List.of(juror1, juror2, juror3)),
                "Should not throw exception"
            );

            verify(messagingService, times(1))
                .doesTrialExist(
                    TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION
                );

            verify(messagingService, times(1))
                .isJurorOnTrial("100000001", TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .isJurorOnTrial("100000002", TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .isJurorOnTrial("100000003", TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void negativeTrialDoesNotExist() {
            MessageSendRequest.JurorAndSendType juror1 = MessageSendRequest.JurorAndSendType.builder()
                .jurorNumber("100000001")
                .poolNumber("200000001")
                .build();
            doReturn(false).when(messagingService).doesTrialExist(
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            );

            MojException.NotFound exception = assertThrows(
                MojException.NotFound.class,
                () -> messagingService
                    .validateTrialNumber(TestConstants.VALID_TRIAL_NUMBER, TestConstants.VALID_COURT_LOCATION,
                        List.of(juror1)),
                "Should throw exception where trial is not found"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(
                "Trial does not exist for trial number: " + TestConstants.VALID_TRIAL_NUMBER);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void negativeJurorNotOnTrial() {
            MessageSendRequest.JurorAndSendType juror1 = MessageSendRequest.JurorAndSendType.builder()
                .jurorNumber("100000001")
                .poolNumber("200000001")
                .build();
            doReturn(true).when(messagingService).doesTrialExist(
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            );
            doReturn(false).when(messagingService).isJurorOnTrial(
                anyString(),
                anyString(),
                anyString()
            );
            MojException.BusinessRuleViolation exception = assertThrows(
                MojException.BusinessRuleViolation.class,
                () -> messagingService
                    .validateTrialNumber(TestConstants.VALID_TRIAL_NUMBER, TestConstants.VALID_COURT_LOCATION,
                        List.of(juror1)),
                "Should throw exception where trial is not found"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(
                "Juror is not apart of trial");
            assertThat(exception.getErrorCode()).isEqualTo(JUROR_NOT_APART_OF_TRIAL);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("public void send(MessageType messageType, String locCode, MessageSendRequest messageSendRequest)")
    class SendTest {

        @Test
        void positiveTypicalNoTrialNumber() {
            final MessageType messageType = MessageType.FAILED_TO_ATTEND_COURT;
            doNothing().when(messagingService).validateTrialNumber(any(), any(), any());
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);
            doReturn(englishMessageTemplate).when(messagingService)
                .getMessageTemplate(messageType, true);
            when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.empty());

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository)
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);

            final String englishTemplateText = "Some english template text";


            MessageSendRequest.JurorAndSendType jurorAndSendType1 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message1 = mock(Message.class);
            doReturn(message1).when(messagingService)
                .createMessage(jurorAndSendType1, messageType, courtLocation, englishTemplateText, null);
            MessageSendRequest.JurorAndSendType jurorAndSendType2 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message2 = mock(Message.class);
            doReturn(message2).when(messagingService)
                .createMessage(jurorAndSendType2, messageType, courtLocation, englishTemplateText, null);
            MessageSendRequest.JurorAndSendType jurorAndSendType3 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message3 = mock(Message.class);
            doReturn(message3).when(messagingService)
                .createMessage(jurorAndSendType3, messageType, courtLocation, englishTemplateText, null);
            doReturn(false).when(messagingService).isWelshLocation(TestConstants.VALID_COURT_LOCATION);
            Map<String, String> placeholders = Map.of();
            List<MessageSendRequest.JurorAndSendType> jurorList = List.of(jurorAndSendType1,
                jurorAndSendType2, jurorAndSendType3);
            MessageSendRequest request = MessageSendRequest.builder()
                .placeholderValues(placeholders)
                .jurors(jurorList)
                .build();

            doReturn(englishTemplateText).when(messagingService)
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION, false,
                    placeholders);


            messagingService.send(messageType, TestConstants.VALID_COURT_LOCATION, request);


            verify(messagingService, times(1))
                .validateTrialNumber(null, TestConstants.VALID_COURT_LOCATION,
                    jurorList);

            verify(messagingService, times(1))
                .getMessageTemplate(messageType, true);

            verify(courtLocationRepository, times(1))
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION, false,
                    placeholders);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType1, messageType, courtLocation, englishTemplateText, null);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType2, messageType, courtLocation, englishTemplateText, null);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType3, messageType, courtLocation, englishTemplateText, null);

            verify(messagesRepository, times(1))
                .saveAll(List.of(message1, message2, message3));
        }

        @Test
        void positiveTypicalEnglishLocation() {
            final MessageType messageType = MessageType.FAILED_TO_ATTEND_COURT;
            doNothing().when(messagingService).validateTrialNumber(any(), any(), any());
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);
            doReturn(englishMessageTemplate).when(messagingService)
                .getMessageTemplate(messageType, true);
            when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.empty());

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository)
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);

            final String englishTemplateText = "Some english template text";


            MessageSendRequest.JurorAndSendType jurorAndSendType1 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message1 = mock(Message.class);
            doReturn(message1).when(messagingService)
                .createMessage(jurorAndSendType1, messageType, courtLocation, englishTemplateText, null);
            MessageSendRequest.JurorAndSendType jurorAndSendType2 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message2 = mock(Message.class);
            doReturn(message2).when(messagingService)
                .createMessage(jurorAndSendType2, messageType, courtLocation, englishTemplateText, null);
            MessageSendRequest.JurorAndSendType jurorAndSendType3 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message3 = mock(Message.class);
            doReturn(message3).when(messagingService)
                .createMessage(jurorAndSendType3, messageType, courtLocation, englishTemplateText, null);
            doReturn(false).when(messagingService).isWelshLocation(TestConstants.VALID_COURT_LOCATION);
            Map<String, String> placeholders = Map.of(
                "<trial_no>", TestConstants.VALID_TRIAL_NUMBER
            );
            List<MessageSendRequest.JurorAndSendType> jurorList = List.of(jurorAndSendType1,
                jurorAndSendType2, jurorAndSendType3);
            MessageSendRequest request = MessageSendRequest.builder()
                .placeholderValues(placeholders)
                .jurors(jurorList)
                .build();

            doReturn(englishTemplateText).when(messagingService)
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION, false,
                    placeholders);


            messagingService.send(messageType, TestConstants.VALID_COURT_LOCATION, request);


            verify(messagingService, times(1))
                .validateTrialNumber(TestConstants.VALID_TRIAL_NUMBER, TestConstants.VALID_COURT_LOCATION,
                    jurorList);

            verify(messagingService, times(1))
                .getMessageTemplate(messageType, true);

            verify(courtLocationRepository, times(1))
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION, false,
                    placeholders);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType1, messageType, courtLocation, englishTemplateText, null);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType2, messageType, courtLocation, englishTemplateText, null);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType3, messageType, courtLocation, englishTemplateText, null);

            verify(messagesRepository, times(1))
                .saveAll(List.of(message1, message2, message3));
        }

        @Test
        void positiveIsWelshLocation() {
            final MessageType messageType = MessageType.FAILED_TO_ATTEND_COURT;
            doNothing().when(messagingService).validateTrialNumber(any(), any(), any());
            MessageTemplate englishMessageTemplate = mock(MessageTemplate.class);
            doReturn(englishMessageTemplate).when(messagingService)
                .getMessageTemplate(messageType, true);
            when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.empty());

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository)
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);

            final String englishTemplateText = "Some english template text";

            final String welshTemplateText = "Some english template text";

            MessageSendRequest.JurorAndSendType jurorAndSendType1 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message1 = mock(Message.class);
            doReturn(message1).when(messagingService)
                .createMessage(jurorAndSendType1, messageType, courtLocation, englishTemplateText,
                    welshTemplateText);
            MessageSendRequest.JurorAndSendType jurorAndSendType2 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message2 = mock(Message.class);
            doReturn(message2).when(messagingService)
                .createMessage(jurorAndSendType2, messageType, courtLocation, englishTemplateText,
                    welshTemplateText);
            MessageSendRequest.JurorAndSendType jurorAndSendType3 =
                mock(MessageSendRequest.JurorAndSendType.class);
            Message message3 = mock(Message.class);
            doReturn(message3).when(messagingService)
                .createMessage(jurorAndSendType3, messageType, courtLocation, englishTemplateText,
                    welshTemplateText);

            MessageTemplate welshMessageTemplate = mock(MessageTemplate.class);
            doReturn(welshMessageTemplate).when(messagingService)
                .getMessageTemplate(messageType, false);


            doReturn(true).when(messagingService).isWelshLocation(TestConstants.VALID_COURT_LOCATION);
            Map<String, String> placeholders = Map.of(
                "<trial_no>", TestConstants.VALID_TRIAL_NUMBER
            );
            List<MessageSendRequest.JurorAndSendType> jurorList = List.of(jurorAndSendType1,
                jurorAndSendType2, jurorAndSendType3);
            MessageSendRequest request = MessageSendRequest.builder()
                .placeholderValues(placeholders)
                .jurors(jurorList)
                .build();

            doReturn(englishTemplateText).when(messagingService)
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION, false,
                    placeholders);

            doReturn(welshTemplateText).when(messagingService)
                .getMessageTemplatePopulated(welshMessageTemplate, TestConstants.VALID_COURT_LOCATION, true,
                    placeholders);

            messagingService.send(messageType, TestConstants.VALID_COURT_LOCATION, request);


            verify(messagingService, times(1))
                .validateTrialNumber(TestConstants.VALID_TRIAL_NUMBER, TestConstants.VALID_COURT_LOCATION,
                    jurorList);

            verify(messagingService, times(1))
                .getMessageTemplate(messageType, true);

            verify(courtLocationRepository, times(1))
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);

            verify(messagingService, times(1))
                .getMessageTemplatePopulated(englishMessageTemplate, TestConstants.VALID_COURT_LOCATION, false,
                    placeholders);
            verify(messagingService, times(1))
                .getMessageTemplatePopulated(welshMessageTemplate, TestConstants.VALID_COURT_LOCATION, true,
                    placeholders);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType1, messageType, courtLocation, englishTemplateText,
                    welshTemplateText);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType2, messageType, courtLocation, englishTemplateText,
                    welshTemplateText);
            verify(messagingService, times(1))
                .createMessage(jurorAndSendType3, messageType, courtLocation, englishTemplateText,
                    welshTemplateText);

            verify(messagesRepository, times(1))
                .saveAll(List.of(message1, message2, message3));
        }

        @Test
        void negativeCourtLocationNotFound() {
            final MessageType messageType = MessageType.FAILED_TO_ATTEND_COURT;
            doNothing().when(messagingService).validateTrialNumber(any(), any(), any());
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            doReturn(messageTemplate).when(messagingService)
                .getMessageTemplate(messageType, true);
            when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
                .thenReturn(Optional.empty());

            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> messagingService.send(messageType, TestConstants.VALID_COURT_LOCATION,
                        mock(MessageSendRequest.class)),
                    "Exception should be thrown when court location is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("Court location '" + TestConstants.VALID_COURT_LOCATION + "' not found");
            assertThat(exception.getCause()).isNull();


        }


    }

    @Nested
    @DisplayName("boolean doesTrialExist(String trialNumber, String locCode)")
    class DoesTrialExist {

        @Test
        void positiveTrue() {
            doReturn(true)
                .when(trialRepository)
                .existsByTrialNumberAndCourtLocationLocCode(
                    TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION);

            assertThat(messagingService.doesTrialExist(
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            )).isTrue();
        }

        @Test
        void positiveFalse() {
            doReturn(false)
                .when(trialRepository)
                .existsByTrialNumberAndCourtLocationLocCode(
                    TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION);

            assertThat(messagingService.doesTrialExist(
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            )).isFalse();
        }

    }

    @Nested
    @DisplayName("boolean isJurorOnTrial(String jurorNumber, String poolNumber, String trialNumber, String locCode)")
    class IsJurorOnTrial {
        @Test
        @SuppressWarnings("LineLength")
        void positiveTrue() {
            doReturn(true)
                .when(panelRepository)
                .existsByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumber(
                    TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            assertThat(messagingService.isJurorOnTrial(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            )).isTrue();
        }

        @Test
        @SuppressWarnings("LineLength")
        void positiveFalse() {
            doReturn(false)
                .when(panelRepository)
                .existsByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumber(
                    TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_TRIAL_NUMBER,
                    TestConstants.VALID_COURT_LOCATION);

            assertThat(messagingService.isJurorOnTrial(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_TRIAL_NUMBER,
                TestConstants.VALID_COURT_LOCATION
            )).isFalse();
        }
    }

    @Nested
    @DisplayName("Message createMessage(MessageSendRequest.JurorNumberAndSendType jurorNumberAndSendType,\n"
        + "                          MessageType messageType,\n"
        + "                          CourtLocation courtLocation,\n"
        + "                          String englishTemplate,\n"
        + "                          String welshTemplate)")
    class CreateMessage {

        private static final String ENGLISH_SUBJECT = "Your Jury Service";
        private static final String WELSH_SUBJECT = "Eich Gwasanaeth Rheithgor";
        private MockedStatic<SecurityUtil> securityUtilMockedStatic;

        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        void positiveTypicalSms() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.SMS)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(TestConstants.VALID_PHONE_NUMBER);
            when(juror.getEmail()).thenReturn(TestConstants.VALID_EMAIL);

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String otherInfo = "Some other info";
            doReturn(messageTemplate).when(messagingService)
                .getMessageTemplate(MessageType.FAILED_TO_ATTEND_COURT, true);
            doReturn(otherInfo).when(messageTemplate).getTitle();
            Message message = messagingService.createMessage(
                jurorAndSendType,
                MessageType.FAILED_TO_ATTEND_COURT,
                courtLocation,
                englishTemplate,
                null
            );

            assertThat(message).isNotNull();
            assertThat(message.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
            assertThat(message.getFileDatetime()).isEqualTo(LocalDateTime.now(clock));
            assertThat(message.getUserName()).isEqualTo(username);
            assertThat(message.getLocationCode()).isEqualTo(courtLocation);
            assertThat(message.getPoolNumber()).isEqualTo(TestConstants.VALID_POOL_NUMBER);

            assertThat(message.getEmail()).isNull();
            assertThat(message.getPhone()).isEqualTo(TestConstants.VALID_PHONE_NUMBER);
            assertThat(message.getSubject()).isEqualTo(ENGLISH_SUBJECT);
            assertThat(message.getMessageText()).isEqualTo(englishTemplate);
            assertThat(message.getMessageId()).isEqualTo(2);
            verify(historyService, times(1))
                .createSendMessageHistory(
                    TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER,
                    otherInfo
                );
        }


        @Test
        void positiveTypicalEmail() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.EMAIL)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String otherInfo = "Some other info";
            doReturn(messageTemplate).when(messagingService)
                .getMessageTemplate(MessageType.FAILED_TO_ATTEND_COURT, true);
            doReturn(otherInfo).when(messageTemplate).getTitle();

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(TestConstants.VALID_PHONE_NUMBER);
            when(juror.getEmail()).thenReturn(TestConstants.VALID_EMAIL);

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            Message message = messagingService.createMessage(
                jurorAndSendType,
                MessageType.FAILED_TO_ATTEND_COURT,
                courtLocation,
                englishTemplate,
                null
            );

            assertThat(message).isNotNull();
            assertThat(message.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
            assertThat(message.getFileDatetime()).isEqualTo(LocalDateTime.now(clock));
            assertThat(message.getUserName()).isEqualTo(username);
            assertThat(message.getLocationCode()).isEqualTo(courtLocation);
            assertThat(message.getPoolNumber()).isEqualTo(TestConstants.VALID_POOL_NUMBER);

            assertThat(message.getEmail()).isEqualTo(TestConstants.VALID_EMAIL);
            assertThat(message.getPhone()).isNull();
            assertThat(message.getSubject()).isEqualTo(ENGLISH_SUBJECT);
            assertThat(message.getMessageText()).isEqualTo(englishTemplate);
            assertThat(message.getMessageId()).isEqualTo(2);
            verify(historyService, times(1))
                .createSendMessageHistory(
                    TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER,
                    otherInfo
                );
        }

        @Test
        void positiveTypicalBoth() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.EMAIL_AND_SMS)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(TestConstants.VALID_PHONE_NUMBER);
            when(juror.getEmail()).thenReturn(TestConstants.VALID_EMAIL);

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);
            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String otherInfo = "Some other info";
            doReturn(messageTemplate).when(messagingService)
                .getMessageTemplate(MessageType.FAILED_TO_ATTEND_COURT, true);
            doReturn(otherInfo).when(messageTemplate).getTitle();
            Message message = messagingService.createMessage(
                jurorAndSendType,
                MessageType.FAILED_TO_ATTEND_COURT,
                courtLocation,
                englishTemplate,
                null
            );

            assertThat(message).isNotNull();
            assertThat(message.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
            assertThat(message.getFileDatetime()).isEqualTo(LocalDateTime.now(clock));
            assertThat(message.getUserName()).isEqualTo(username);
            assertThat(message.getLocationCode()).isEqualTo(courtLocation);
            assertThat(message.getPoolNumber()).isEqualTo(TestConstants.VALID_POOL_NUMBER);

            assertThat(message.getEmail()).isEqualTo(TestConstants.VALID_EMAIL);
            assertThat(message.getPhone()).isEqualTo(TestConstants.VALID_PHONE_NUMBER);
            assertThat(message.getSubject()).isEqualTo(ENGLISH_SUBJECT);
            assertThat(message.getMessageText()).isEqualTo(englishTemplate);
            assertThat(message.getMessageId()).isEqualTo(2);
            verify(historyService, times(1))
                .createSendMessageHistory(
                    TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER,
                    otherInfo
                );
        }

        @Test
        void positiveTypicalWelshCourtNoneWelshUser() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.EMAIL)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            final String welshTemplate = "welsh template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(TestConstants.VALID_PHONE_NUMBER);
            when(juror.getEmail()).thenReturn(TestConstants.VALID_EMAIL);
            when(juror.getWelsh()).thenReturn(false);

            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String otherInfo = "Some other info";
            doReturn(messageTemplate).when(messagingService)
                .getMessageTemplate(MessageType.FAILED_TO_ATTEND_COURT, true);
            doReturn(otherInfo).when(messageTemplate).getTitle();

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            Message message = messagingService.createMessage(
                jurorAndSendType,
                MessageType.FAILED_TO_ATTEND_COURT,
                courtLocation,
                englishTemplate,
                welshTemplate
            );

            assertThat(message).isNotNull();
            assertThat(message.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
            assertThat(message.getFileDatetime()).isEqualTo(LocalDateTime.now(clock));
            assertThat(message.getUserName()).isEqualTo(username);
            assertThat(message.getLocationCode()).isEqualTo(courtLocation);
            assertThat(message.getPoolNumber()).isEqualTo(TestConstants.VALID_POOL_NUMBER);

            assertThat(message.getEmail()).isEqualTo(TestConstants.VALID_EMAIL);
            assertThat(message.getPhone()).isNull();
            assertThat(message.getSubject()).isEqualTo(ENGLISH_SUBJECT);
            assertThat(message.getMessageText()).isEqualTo(englishTemplate);
            assertThat(message.getMessageId()).isEqualTo(2);

            verify(historyService, times(1))
                .createSendMessageHistory(
                    TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER,
                    otherInfo
                );
        }

        @Test
        void positiveTypicalWelshCourtWelshUser() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.EMAIL)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            final String welshTemplate = "welsh template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(TestConstants.VALID_PHONE_NUMBER);
            when(juror.getEmail()).thenReturn(TestConstants.VALID_EMAIL);
            when(juror.getWelsh()).thenReturn(true);

            MessageTemplate messageTemplate = mock(MessageTemplate.class);
            final String otherInfo = "Some other info";
            doReturn(messageTemplate).when(messagingService)
                .getMessageTemplate(MessageType.FAILED_TO_ATTEND_COURT, false);
            doReturn(otherInfo).when(messageTemplate).getTitle();

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            Message message = messagingService.createMessage(
                jurorAndSendType,
                MessageType.FAILED_TO_ATTEND_COURT,
                courtLocation,
                englishTemplate,
                welshTemplate
            );

            assertThat(message).isNotNull();
            assertThat(message.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
            assertThat(message.getFileDatetime()).isEqualTo(LocalDateTime.now(clock));
            assertThat(message.getUserName()).isEqualTo(username);
            assertThat(message.getLocationCode()).isEqualTo(courtLocation);
            assertThat(message.getPoolNumber()).isEqualTo(TestConstants.VALID_POOL_NUMBER);

            assertThat(message.getEmail()).isEqualTo(TestConstants.VALID_EMAIL);
            assertThat(message.getPhone()).isNull();
            assertThat(message.getSubject()).isEqualTo(WELSH_SUBJECT);
            assertThat(message.getMessageText()).isEqualTo(welshTemplate);
            assertThat(message.getMessageId()).isEqualTo(19);
            verify(historyService, times(1))
                .createSendMessageHistory(
                    TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER,
                    otherInfo
                );
        }


        @Test
        void negativeDoesNotSuportSms() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .type(MessageType.SendType.SMS)
                    .build();

            MojException.BusinessRuleViolation exception = assertThrows(
                MojException.BusinessRuleViolation.class,
                () -> messagingService
                    .createMessage(jurorAndSendType, MessageType.SENTENCING_INVITE_COURT, null, null, null),
                "Should throw exception where message type does not support SMS"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(
                "This message type only supports: EMAIL");
            assertThat(exception.getErrorCode()).isEqualTo(INVALID_SEND_TYPE);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void negativeDoesNotSuportEmail() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .type(MessageType.SendType.EMAIL)
                    .build();

            MojException.BusinessRuleViolation exception = assertThrows(
                MojException.BusinessRuleViolation.class,
                () -> messagingService
                    .createMessage(jurorAndSendType, MessageType.CHECK_INBOX_COURT, null, null, null),
                "Should throw exception where message type does not support EMAIL"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(
                "This message type only supports: SMS");
            assertThat(exception.getErrorCode()).isEqualTo(INVALID_SEND_TYPE);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void negativePhoneNotFound() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.SMS)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            final String welshTemplate = "welsh template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(null);
            when(juror.getEmail()).thenReturn(TestConstants.VALID_EMAIL);
            when(juror.getWelsh()).thenReturn(true);

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            MojException.BusinessRuleViolation exception = assertThrows(
                MojException.BusinessRuleViolation.class,
                () -> messagingService.createMessage(
                    jurorAndSendType,
                    MessageType.FAILED_TO_ATTEND_COURT,
                    courtLocation,
                    englishTemplate,
                    welshTemplate
                ),
                "Should throw exception where message type does not support EMAIL"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(
                "Phone number is required for juror: " + TestConstants.VALID_JUROR_NUMBER);
            assertThat(exception.getErrorCode()).isEqualTo(JUROR_MUST_HAVE_PHONE_NUMBER);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void negativeEmailNotFound() {
            MessageSendRequest.JurorAndSendType jurorAndSendType =
                MessageSendRequest.JurorAndSendType.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .type(MessageType.SendType.EMAIL)
                    .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            final String username = "username123";

            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn(username);
            final String englishTemplate = "english template";
            final String welshTemplate = "welsh template";
            Juror juror = mock(Juror.class);
            when(juror.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(juror.getPollNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(juror.getPhoneNumber()).thenReturn(TestConstants.VALID_PHONE_NUMBER);
            when(juror.getEmail()).thenReturn(null);
            when(juror.getWelsh()).thenReturn(true);

            doReturn(juror).when(messagingService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            MojException.BusinessRuleViolation exception = assertThrows(
                MojException.BusinessRuleViolation.class,
                () -> messagingService.createMessage(
                    jurorAndSendType,
                    MessageType.FAILED_TO_ATTEND_COURT,
                    courtLocation,
                    englishTemplate,
                    welshTemplate
                ),
                "Should throw exception where message type does not support EMAIL"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(
                "Email is required for juror: " + TestConstants.VALID_JUROR_NUMBER);
            assertThat(exception.getErrorCode()).isEqualTo(JUROR_MUST_HAVE_EMAIL);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName(
        "Map<String, String> getPopulatedPlaceholders(List<ViewMessageTemplateDto.Placeholder> requiredPlaceholders,\n"
            + "                                                 Map<String, String> overridePlaceholderValues,\n"
            + "                                                 boolean isWelsh)")
    class GetPopulatedPlaceholders {

        @Test
        void positiveNoPlaceholders() {
            assertThat(messagingService.getPopulatedPlaceholders(
                List.of(),
                Map.of(),
                false
            )).isEqualTo(new ConcurrentHashMap<>());
        }

        @Test
        void positivePlaceholdersWelsh() {
            ViewMessageTemplateDto.Placeholder placeholder1 = ViewMessageTemplateDto.Placeholder.builder()
                .placeholderName("placeholder1").build();
            ViewMessageTemplateDto.Placeholder placeholder2 = ViewMessageTemplateDto.Placeholder.builder()
                .placeholderName("placeholder2").build();
            ViewMessageTemplateDto.Placeholder placeholder3 = ViewMessageTemplateDto.Placeholder.builder()
                .placeholderName("placeholder3").build();


            Map<String, String> overrides = Map.of(
                "Key1", "Value1",
                "Key2", "Value2",
                "Key3", "Value3"
            );

            doReturn("Place1").when(messagingService)
                .getPlaceholderValue(placeholder1, overrides, true);
            doReturn("Place2").when(messagingService)
                .getPlaceholderValue(placeholder2, overrides, true);
            doReturn("Place3").when(messagingService)
                .getPlaceholderValue(placeholder3, overrides, true);
            Map<String, String> response = messagingService.getPopulatedPlaceholders(
                List.of(placeholder1, placeholder2, placeholder3),
                overrides,
                true
            );
            assertThat(response).isNotNull().hasSize(3).contains(
                Map.entry("placeholder1", "Place1"),
                Map.entry("placeholder2", "Place2"),
                Map.entry("placeholder3", "Place3")
            );

            verify(messagingService, times(1))
                .getPlaceholderValue(placeholder1, overrides, true);
            verify(messagingService, times(1))
                .getPlaceholderValue(placeholder2, overrides, true);
            verify(messagingService, times(1))
                .getPlaceholderValue(placeholder3, overrides, true);
        }

        @Test
        void positivePlaceholdersEnglish() {
            ViewMessageTemplateDto.Placeholder placeholder1 = ViewMessageTemplateDto.Placeholder.builder()
                .placeholderName("placeholder1").build();
            ViewMessageTemplateDto.Placeholder placeholder2 = ViewMessageTemplateDto.Placeholder.builder()
                .placeholderName("placeholder2").build();
            ViewMessageTemplateDto.Placeholder placeholder3 = ViewMessageTemplateDto.Placeholder.builder()
                .placeholderName("placeholder3").build();


            Map<String, String> overrides = Map.of(
                "Key1", "Value1",
                "Key2", "Value2",
                "Key3", "Value3"
            );

            doReturn("Place1").when(messagingService)
                .getPlaceholderValue(placeholder1, overrides, false);
            doReturn("Place2").when(messagingService)
                .getPlaceholderValue(placeholder2, overrides, false);
            doReturn("Place3").when(messagingService)
                .getPlaceholderValue(placeholder3, overrides, false);
            Map<String, String> response = messagingService.getPopulatedPlaceholders(
                List.of(placeholder1, placeholder2, placeholder3),
                overrides,
                false
            );
            assertThat(response).isNotNull().hasSize(3).contains(
                Map.entry("placeholder1", "Place1"),
                Map.entry("placeholder2", "Place2"),
                Map.entry("placeholder3", "Place3")
            );

            verify(messagingService, times(1))
                .getPlaceholderValue(placeholder1, overrides, false);
            verify(messagingService, times(1))
                .getPlaceholderValue(placeholder2, overrides, false);
            verify(messagingService, times(1))
                .getPlaceholderValue(placeholder3, overrides, false);
        }
    }

    @Nested
    @DisplayName("String getPlaceholderValue(ViewMessageTemplateDto.Placeholder placeholder,\n"
        + "                               Map<String, String> overridePlaceholderValues,\n"
        + "                               boolean isWelsh)")
    class GetPlaceholderValue {

        @Test
        void positiveIsOverridableHasValue() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<trial_no>")
                    .dataType(DataType.STRING)
                    .editable(false)
                    .defaultValue("abc")
                    .build();
            doReturn(true).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of(
                "<trial_no>", "override value 123"
            );
            assertThat(messagingService.getPlaceholderValue(
                placeholder, overridePlaceholderValues, false))
                .isEqualTo("override value 123");
        }

        @Test
        void positiveIsOverridableWithoutValue() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<trial_no>")
                    .dataType(DataType.STRING)
                    .editable(false)
                    .defaultValue("abc")
                    .build();
            doReturn(true).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of(
                "<trial_no_no>", "override value 123"
            );
            assertThat(messagingService.getPlaceholderValue(
                placeholder, overridePlaceholderValues, false))
                .isEqualTo("abc");
        }

        @Test
        void positiveIsEditableHasValue() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<edit_trial_no>")
                    .dataType(DataType.STRING)
                    .editable(true)
                    .defaultValue("abc")
                    .build();
            doReturn(false).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of(
                "<edit_trial_no>", "override value 123"
            );
            assertThat(messagingService.getPlaceholderValue(
                placeholder, overridePlaceholderValues, false))
                .isEqualTo("override value 123");
        }

        @Test
        void negativeIsEditableWithoutValue() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<edit_trial_no>")
                    .dataType(DataType.STRING)
                    .editable(true)
                    .defaultValue(null)
                    .build();
            doReturn(false).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of();

            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> messagingService.getPlaceholderValue(
                        placeholder, overridePlaceholderValues, false),
                    "Exception should be thrown when value is null");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("Placeholder <edit_trial_no> must have a value");
            assertThat(exception.getErrorCode())
                .isEqualTo(PLACEHOLDER_MUST_HAVE_VALUE);
            assertThat(exception.getCause()).isNull();

        }

        @Test
        void positiveIsNotEditable() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<no_edit_trial_no>")
                    .dataType(DataType.STRING)
                    .editable(false)
                    .defaultValue("abcd")
                    .build();
            doReturn(false).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of(
                "no_edit_trial_no", "ignored_value"
            );

            assertThat(messagingService.getPlaceholderValue(
                placeholder, overridePlaceholderValues, false))
                .isEqualTo("abcd");
        }

        @Test
        void positiveTypicalIsWelsh() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<some_time>")
                    .dataType(DataType.TIME)
                    .editable(false)
                    .defaultValue("16:30")
                    .build();
            doReturn(false).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of();

            assertThat(messagingService.getPlaceholderValue(
                placeholder, overridePlaceholderValues, true))
                .isEqualTo("16:30");
        }

        @Test
        void positiveTypicalIsEnglish() {
            ViewMessageTemplateDto.Placeholder placeholder =
                ViewMessageTemplateDto.Placeholder.builder()
                    .placeholderName("<some_time>")
                    .dataType(DataType.TIME)
                    .editable(false)
                    .defaultValue("16:30")
                    .build();
            doReturn(false).when(messagingService).isOverridable(placeholder.getPlaceholderName());
            Map<String, String> overridePlaceholderValues = Map.of();

            assertThat(messagingService.getPlaceholderValue(
                placeholder, overridePlaceholderValues, false))
                .isEqualTo("16:30");
        }
    }

    @Nested
    @DisplayName("boolean isOverridable(String placeholderName)")
    class IsOverridable {

        @Test
        void positiveTrialTrue() {
            assertThat(messagingService.isOverridable("<trial_no>")).isTrue();
        }

        @Test
        void positiveTrialFalse() {
            assertThat(messagingService.isOverridable("trial_no>")).isFalse();
        }
    }

    @Nested
    @DisplayName("Juror getJuror(String jurorNumber)")
    class GetJuror {

        @Test
        void positiveJurorFound() {
            Juror juror = mock(Juror.class);
            doReturn(juror).when(jurorRepository)
                .findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);

            assertThat(messagingService.getJuror(TestConstants.VALID_JUROR_NUMBER))
                .isEqualTo(juror);
            verify(jurorRepository, times(1))
                .findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);
        }

        @Test
        void negativeJurorNotFound() {
            doReturn(null).when(jurorRepository)
                .findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);


            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> messagingService.getJuror(TestConstants.VALID_JUROR_NUMBER),
                    "Exception should be thrown when juror is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("Juror not found: JurorNumber: " + TestConstants.VALID_JUROR_NUMBER);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("List<ViewMessageTemplateDto.Placeholder> getRequiredPlaceholders(MessageTemplate messageTemplate,\n"
        + "                                                                     String locCode)")
    class GetRequiredPlaceholders {
        @Test
        void positiveTypical() {

            MessagePlaceholders messagePlaceholders1 = mock(MessagePlaceholders.class);
            ViewMessageTemplateDto.Placeholder placeholder1 = mock(ViewMessageTemplateDto.Placeholder.class);
            doReturn(placeholder1).when(messagingService).mapPlaceholder(messagePlaceholders1,
                TestConstants.VALID_COURT_LOCATION);

            MessagePlaceholders messagePlaceholders2 = mock(MessagePlaceholders.class);
            ViewMessageTemplateDto.Placeholder placeholder2 = mock(ViewMessageTemplateDto.Placeholder.class);
            doReturn(placeholder2).when(messagingService).mapPlaceholder(messagePlaceholders2,
                TestConstants.VALID_COURT_LOCATION);

            MessagePlaceholders messagePlaceholders3 = mock(MessagePlaceholders.class);
            ViewMessageTemplateDto.Placeholder placeholder3 = mock(ViewMessageTemplateDto.Placeholder.class);
            doReturn(placeholder3).when(messagingService).mapPlaceholder(messagePlaceholders3,
                TestConstants.VALID_COURT_LOCATION);
            MessageTemplate messageTemplate = mock(MessageTemplate.class);

            when(messageTemplate.getPlaceholders()).thenReturn(Set.of(
                messagePlaceholders1, messagePlaceholders2, messagePlaceholders3)
            );
            List<ViewMessageTemplateDto.Placeholder> result = messagingService
                .getRequiredPlaceholders(messageTemplate, TestConstants.VALID_COURT_LOCATION);

            assertThat(result).isNotNull().hasSize(3).containsAll(List.of(
                placeholder1, placeholder2, placeholder3
            ));

            verifyNoInteractions(messagePlaceholders1, messagePlaceholders2, messagePlaceholders3);
            verify(messagingService, times(1)).mapPlaceholder(messagePlaceholders1, TestConstants.VALID_COURT_LOCATION);
            verify(messagingService, times(1)).mapPlaceholder(messagePlaceholders2, TestConstants.VALID_COURT_LOCATION);
            verify(messagingService, times(1)).mapPlaceholder(messagePlaceholders3, TestConstants.VALID_COURT_LOCATION);
        }

    }

    @Nested
    @DisplayName("ViewMessageTemplateDto.Placeholder mapPlaceholder(MessagePlaceholders messagePlaceholder, String "
        + "locCode)")
    class MapPlaceholder {


        void assertAndTrigger(MessagePlaceholders messagePlaceholders, String defaultValue) {
            ViewMessageTemplateDto.Placeholder placeholder = messagingService
                .mapPlaceholder(messagePlaceholders, TestConstants.VALID_COURT_LOCATION);

            assertThat(placeholder).isNotNull();
            assertThat(placeholder.getDisplayName())
                .isEqualTo(messagePlaceholders.getDisplayName());
            assertThat(placeholder.isEditable())
                .isEqualTo(messagePlaceholders.isEditable());
            assertThat(placeholder.getPlaceholderName())
                .isEqualTo(messagePlaceholders.getPlaceholderName());
            assertThat(placeholder.getDataType())
                .isEqualTo(messagePlaceholders.getType());
            assertThat(placeholder.getDefaultValue())
                .isEqualTo(defaultValue);
        }

        @Test
        void positiveHasSourceTableAndSourceColumnName() {
            final String defaultValue = "Some default value";
            MessagePlaceholders messagePlaceholders =
                new MessagePlaceholders(
                    "PLACEHOLDERNAME1",
                    "SOURCETABLENAME1",
                    "COLUMNNAME1",
                    "Display name",
                    DataType.NONE,
                    "Desc",
                    true,
                    "Some Regex",
                    "Validation message"
                );

            doReturn(defaultValue).when(messageTemplateRepository)
                .getDefaultValue(messagePlaceholders, TestConstants.VALID_COURT_LOCATION);
            assertAndTrigger(messagePlaceholders, defaultValue);
        }


        @Test
        void positiveHasSourceTableAndNoSourceColumnName() {
            MessagePlaceholders messagePlaceholders =
                new MessagePlaceholders(
                    "PLACEHOLDERNAME1",
                    "SOURCETABLENAME1",
                    null,
                    "Display name",
                    DataType.NONE,
                    "Desc",
                    true,
                    "Some Regex",
                    "Validation message"
                );
            assertAndTrigger(messagePlaceholders, null);
        }

        @Test
        void positiveNoHasSourceTableAndSourceColumnName() {
            MessagePlaceholders messagePlaceholders =
                new MessagePlaceholders(
                    "PLACEHOLDERNAME1",
                    null,
                    "COLUMNNAME1",
                    "Display name",
                    DataType.NONE,
                    "Desc",
                    true,
                    "Some Regex",
                    "Validation message"
                );
            assertAndTrigger(messagePlaceholders, null);
        }

        @Test
        void positiveHasNoSourceTableAndNoSourceColumnName() {
            MessagePlaceholders messagePlaceholders =
                new MessagePlaceholders(
                    "PLACEHOLDERNAME1",
                    null,
                    null,
                    "Display name",
                    DataType.NONE,
                    "Desc",
                    true,
                    "Some Regex",
                    "Validation message"
                );
            assertAndTrigger(messagePlaceholders, null);
        }

    }

    @Nested
    @DisplayName("boolean isWelshLocation(String locCode)")
    class IsWelshLocation {
        @Test
        void positiveTrue() {
            doReturn(Optional.of(mock(WelshCourtLocation.class)))
                .when(welshCourtLocationRepository).findById(TestConstants.VALID_COURT_LOCATION);

            assertThat(messagingService.isWelshLocation(TestConstants.VALID_COURT_LOCATION))
                .isTrue();
        }

        @Test
        void positiveFalse() {
            doReturn(Optional.empty())
                .when(welshCourtLocationRepository).findById(TestConstants.VALID_COURT_LOCATION);

            assertThat(messagingService.isWelshLocation(TestConstants.VALID_COURT_LOCATION))
                .isFalse();

        }
    }

    @Nested
    @DisplayName("public String exportContactDetails(String locCode, ExportContactDetailsRequest "
        + "exportContactDetailsRequest)")
    class ExportContactDetails {
        protected ExportContactDetailsRequest getRequest() {
            return ExportContactDetailsRequest.builder()
                .exportItems(List.of(
                    ExportContactDetailsRequest.ExportItems.JUROR_NUMBER,
                    ExportContactDetailsRequest.ExportItems.POOL_NUMBER,
                    ExportContactDetailsRequest.ExportItems.STATUS
                ))
                .jurors(List.of(
                    JurorAndPoolRequest.builder()
                        .jurorNumber(TestConstants.VALID_JUROR_NUMBER + "0")
                        .poolNumber(TestConstants.VALID_POOL_NUMBER + "0")
                        .build(),

                    JurorAndPoolRequest.builder()
                        .jurorNumber(TestConstants.VALID_JUROR_NUMBER + "1")
                        .poolNumber(TestConstants.VALID_POOL_NUMBER + "1")
                        .build()
                ))
                .build();
        }

        @Test
        void positiveTypical() {
            ExportContactDetailsRequest exportContactDetailsRequest = getRequest();

            List<List<String>> data = List.of(
                List.of(TestConstants.VALID_JUROR_NUMBER + "0", TestConstants.VALID_POOL_NUMBER + "0", "1"),
                List.of(TestConstants.VALID_JUROR_NUMBER + "1", TestConstants.VALID_POOL_NUMBER + "1", "2")
            );

            doReturn(data).when(messageTemplateRepository)
                .exportDetails(exportContactDetailsRequest, TestConstants.VALID_COURT_LOCATION);

            assertThat(messagingService.exportContactDetails(
                TestConstants.VALID_COURT_LOCATION, exportContactDetailsRequest))
                .isEqualTo("Juror Number,Pool Number,Status\n"
                    + TestConstants.VALID_JUROR_NUMBER + "0," + TestConstants.VALID_POOL_NUMBER + "0,1\n"
                    + TestConstants.VALID_JUROR_NUMBER + "1," + TestConstants.VALID_POOL_NUMBER + "1,2");

            verify(messageTemplateRepository, times(1))
                .exportDetails(exportContactDetailsRequest, TestConstants.VALID_COURT_LOCATION);
        }
    }

}
