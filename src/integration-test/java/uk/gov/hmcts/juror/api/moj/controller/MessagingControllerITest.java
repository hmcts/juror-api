package uk.gov.hmcts.juror.api.moj.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.MessageSendRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBureau;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageCourt;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.ViewMessageTemplateDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorSearch;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.messages.DataType;
import uk.gov.hmcts.juror.api.moj.domain.messages.Message;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_MUST_HAVE_EMAIL;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_MUST_HAVE_PHONE_NUMBER;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_NOT_APART_OF_TRIAL;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("MessagingControllerI: /api/v1/moj/messages")
@SuppressWarnings({
    "LineLength",
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class MessagingControllerITest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/moj/messages";

    @Autowired
    private TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @Autowired
    private MessageRepository messagesRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    protected static class TestData {
        public static final String ENGLISH_SUBJECT = "Your Jury Service";
        public static final String WELSH_SUBJECT = "Eich Gwasanaeth Rheithgor";
        protected static final String ENGLISH_LOC_CODE = "462";
        protected static final String WELSH_LOC_CODE = "756";

        protected enum Placeholder {
            ENGLISH_COURT_NAME("N/A", "<court_name>", DataType.STRING, false, "WARRINGTON"),
            ENGLISH_COURT_PHONE("N/A", "<court_phone>", DataType.STRING, false, "01244 356726"),

            WELSH_COURT_PHONE("N/A", "<court_phone>", DataType.STRING, false, "01792 637000"),
            WELSH_ENGLISH_COURT_NAME("N/A", "<court_name>", DataType.STRING, false, "CARMARTHEN"),
            WELSH_COURTNAME("N/A", "<welsh_courtname>", DataType.STRING, false, "CAERFYRDDIN"),

            SENTENCE_DATE("Sentence date", "<sentence_date>", DataType.DATE, true, null),

            ATTEND_DATE("Attendance Date", "<attend_date>", DataType.DATE, true, null),
            ENGLISH_ATTEND_TIME("Attendance time", "<attend_time>", DataType.TIME, true, "09:30:00"),
            WELSH_ATTEND_TIME("Attendance time", "<attend_time>", DataType.TIME, true, "09:15:00"),
            TRIAL_NUMBER("N/A", "<trial_no>", DataType.STRING, false, null);


            private final ViewMessageTemplateDto.Placeholder placeholder;

            Placeholder(String displayName, String placeholderName, DataType dataType, boolean editable,
                        String defaultValue) {
                this.placeholder = ViewMessageTemplateDto.Placeholder.builder()
                    .displayName(displayName)
                    .placeholderName(placeholderName)
                    .dataType(dataType)
                    .editable(editable)
                    .defaultValue(defaultValue)
                    .build();
            }

            public ViewMessageTemplateDto.Placeholder toPlaceholder() {
                return this.placeholder;
            }
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000001() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000001")
                .firstName("FNAME01")
                .lastName("LNAME01")
                .email("FNAME01.LNAME01@email.com")
                .phone("07777000001")
                .poolNumber("200000015")
                .status("Responded")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 1))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000002() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000002")
                .firstName("FNAME02")
                .lastName("LNAME02")
                .email("FNAME02.LNAME02@email.com")
                .phone("07777000002")
                .poolNumber("200000002")
                .status("FailedToAttend")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 2))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000003() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000003")
                .firstName("FNAME03")
                .lastName("LNAME03")
                .email("FNAME03.LNAME03@email.com")
                .phone("07777000003")
                .poolNumber("200000003")
                .status("Deferred")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 1))
                .dateDeferredTo(LocalDate.of(2022, 3, 3))
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000004() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000004")
                .firstName("FNAME04")
                .lastName("LNAME04")
                .email("FNAME04.LNAME04@email.com")
                .phone("07777000004")
                .poolNumber("200000014")
                .status("Juror")
                .trialNumber("T100000000")
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 4))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000005() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000005")
                .firstName("FNAME05")
                .lastName("LNAME05")
                .email("FNAME05.LNAME05@email.com")
                .phone("07777000005")
                .poolNumber("200000015")
                .status("Panel")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 5))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000006() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000006")
                .firstName("FNAME06")
                .lastName("LNAME06")
                .email("FNAME06.LNAME06@email.com")
                .phone("07777000006")
                .poolNumber("200000015")
                .status("Completed")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 6))
                .dateDeferredTo(null)
                .completionDate(LocalDate.of(2023, 1, 6))
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000007() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000007")
                .firstName("FNAME07")
                .lastName("LNAME07")
                .email("FNAME07.LNAME07@email.com")
                .phone("07777000007")
                .poolNumber("200000015")
                .status("Transferred")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 7))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000008() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000008")
                .firstName("FNAME08")
                .lastName("LNAME08")
                .email("FNAME08.LNAME08@email.com")
                .phone("07777000008")
                .poolNumber("200000015")
                .status("Disqualified")
                .trialNumber(null)
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 8))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000009() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000009")
                .firstName("FNAME09")
                .lastName("LNAME09")
                .email("FNAME09.LNAME09@email.com")
                .phone("07777000009")
                .poolNumber("200000001")
                .status("Excused")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 1))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000010() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000010")
                .firstName("FNAME10")
                .lastName("LNAME10")
                .email("FNAME10.LNAME10@email.com")
                .phone("07777000010")
                .poolNumber("200000015")
                .status("Responded")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 10))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000011() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000011")
                .firstName("FNAME11")
                .lastName("LNAME11")
                .email("FNAME11.LNAME11@email.com")
                .phone("07777000011")
                .poolNumber("200000002")
                .status("FailedToAttend")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 11))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000012() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000012")
                .firstName("FNAME12")
                .lastName("LNAME12")
                .email("FNAME12.LNAME12@email.com")
                .phone("07777000012")
                .poolNumber("200000003")
                .status("Deferred")
                .trialNumber(null)
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 12))
                .dateDeferredTo(LocalDate.of(2022, 3, 12))
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000013() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000013")
                .firstName("FNAME13")
                .lastName("LNAME13")
                .email("FNAME13.LNAME13@email.com")
                .phone("07777000013")
                .poolNumber("200000014")
                .status("Juror")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 13))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000014() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000014")
                .firstName("FNAME14")
                .lastName("LNAME14")
                .email("FNAME14.LNAME14@email.com")
                .phone("07777000014")
                .poolNumber("200000015")
                .status("Panel")
                .trialNumber("T100000001")
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 14))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000015() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000015")
                .firstName("FNAME15")
                .lastName("LNAME15")
                .email("FNAME15.LNAME15@email.com")
                .phone("07777000015")
                .poolNumber("200000015")
                .status("Completed")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 1))
                .dateDeferredTo(null)
                .completionDate(LocalDate.of(2023, 1, 15))
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000016() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000016")
                .firstName("FNAME16")
                .lastName("LNAME16")
                .email("FNAME16.LNAME16@email.com")
                .phone("07777000016")
                .poolNumber("200000015")
                .status("Transferred")
                .trialNumber(null)
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 16))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000017() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000017")
                .firstName("FNAME17")
                .lastName("LNAME17")
                .email("FNAME17.LNAME17@email.com")
                .phone("07777000017")
                .poolNumber("200000015")
                .status("Disqualified")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 17))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000018() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000018")
                .firstName("FNAME18")
                .lastName("LNAME18")
                .email("FNAME18.LNAME18@email.com")
                .phone("07777000018")
                .poolNumber("200000001")
                .status("Excused")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 18))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000019() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000019")
                .firstName("FNAME19")
                .lastName("LNAME19")
                .email("FNAME19.LNAME19@email.com")
                .phone("07777000019")
                .poolNumber("200000015")
                .status("Responded")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 19))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000020() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000020")
                .firstName("FNAME20")
                .lastName("LNAME20")
                .email("FNAME20.LNAME20@email.com")
                .phone("07777000020")
                .poolNumber("200000002")
                .status("FailedToAttend")
                .trialNumber(null)
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 20))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000021() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000021")
                .firstName("FNAME21")
                .lastName("LNAME21")
                .email("FNAME21.LNAME21@email.com")
                .phone("07777000021")
                .poolNumber("200000003")
                .status("Deferred")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 21))
                .dateDeferredTo(LocalDate.of(2022, 3, 21))
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000022() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000022")
                .firstName("FNAME22")
                .lastName("LNAME22")
                .email("FNAME22.LNAME22@email.com")
                .phone("07777000022")
                .poolNumber("200000014")
                .status("Juror")
                .trialNumber("T100000000")
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 22))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000023() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000023")
                .firstName("FNAME23")
                .lastName("LNAME23")
                .email("FNAME23.LNAME23@email.com")
                .phone("07777000023")
                .poolNumber("200000015")
                .status("Panel")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 23))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000024() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000024")
                .firstName("FNAME24")
                .lastName("LNAME24")
                .email("FNAME24.LNAME24@email.com")
                .phone("07777000024")
                .poolNumber("200000015")
                .status("Completed")
                .trialNumber(null)
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 24))
                .dateDeferredTo(null)
                .completionDate(LocalDate.of(2023, 1, 24))
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000025() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000025")
                .firstName("FNAME25")
                .lastName("LNAME25")
                .email("FNAME25.LNAME25@email.com")
                .phone("07777000025")
                .poolNumber("200000015")
                .status("Transferred")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 25))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000026() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000026")
                .firstName("FNAME26")
                .lastName("LNAME26")
                .email("FNAME26.LNAME26@email.com")
                .phone("07777000026")
                .poolNumber("200000015")
                .status("Disqualified")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 26))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000027() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000027")
                .firstName("FNAME27")
                .lastName("LNAME27")
                .email("FNAME27.LNAME27@email.com")
                .phone("07777000027")
                .poolNumber("200000001")
                .status("Excused")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 27))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000028() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000028")
                .firstName("FNAME28")
                .lastName("LNAME28")
                .email("FNAME28.LNAME28@email.com")
                .phone("07777000028")
                .poolNumber("200000015")
                .status("Responded")
                .trialNumber(null)
                .onCall(true)
                .nextDueAtCourt(LocalDate.of(2022, 1, 28))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000029() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000029")
                .firstName("FNAME29")
                .lastName("LNAME29")
                .email("FNAME29.LNAME29@email.com")
                .phone("07777000029")
                .poolNumber("200000002")
                .status("FailedToAttend")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 29))
                .dateDeferredTo(null)
                .completionDate(null)
                .welshLanguage(false)
                .build();
        }

        protected static JurorToSendMessageCourt getExpectedJurorToSendMessageFor100000030() {
            return JurorToSendMessageCourt.builder()
                .jurorNumber("100000030")
                .firstName("FNAME30")
                .lastName("LNAME30")
                .email("FNAME30.LNAME30@email.com")
                .phone("07777000030")
                .poolNumber("200000003")
                .status("Deferred")
                .trialNumber(null)
                .onCall(false)
                .nextDueAtCourt(LocalDate.of(2022, 1, 30))
                .dateDeferredTo(LocalDate.of(2022, 3, 12))
                .completionDate(null)
                .welshLanguage(true)
                .build();
        }
    }

    @Nested
    @DisplayName("GET " + GetMessageDetails.URL)
    class GetMessageDetails {

        public static final String URL = BASE_URL + "/view/{message_type}/{loc_code}";


        protected URI toUri(MessageType messageType, String locCode) {
            return toUri(messageType.name(), locCode);
        }

        protected URI toUri(String messageType, String locCode) {
            return URI.create(toUrl(messageType, locCode));
        }

        protected String toUrl(String messageType, String locCode) {
            return URL.replace("{message_type}", messageType)
                .replace("{loc_code}", locCode);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected final List<DynamicContainer> tests;


            protected Positive() {
                this.tests = new ArrayList<>();

                addTest(MessageType.REMIND_TO_ATTEND,
                    "Reminder: Please attend for your Jury Service at <court_name> Court on <attend_date> at "
                        + "<attend_time>. If you have any questions, please contact the jury office on <court_phone>.",
                    "Nodyn Atgoffa: Cofiwch fynychu eich Gwasanaeth Rheithgor yn Llys<welsh_courtname> ar "
                        + "<attend_date> am <attend_time>. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa "
                        + "rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURTNAME.toPlaceholder(),
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()),
                    MessageType.SendType.EMAIL_AND_SMS
                );


                addTest(MessageType.FAILED_TO_ATTEND_COURT,
                    "You failed to attend for Jury Service at <court_name> Court on <attend_date>. Please contact the"
                        + " jury office on <court_phone>.",
                    "Bu ichi fethu â mynychu eich Gwasanaeth Rheithgor yn Llys<welsh_courtname> ar <attend_date>. "
                        + "Cysylltwch â'r Swyddfa Rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURTNAME.toPlaceholder(),
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.ATTENDANCE_DATE_AND_TIME_CHANGED_COURT,
                    "The date of your attendance for Jury Service has changed to <attend_date> at <attend_time> at "
                        + "<court_name> Court. The days you are not required at court still form part of your jury "
                        + "service and will not be added on at the end. If you have any questions, please contact the"
                        + " jury office on <court_phone>.",
                    "Mae dyddiad mynychu eich Gwasanaeth Rheithgor wedi newid i <attend_date> am <attend_time> yn "
                        + "Llys<welsh_courtname>. Mae'r dyddiau pan na fydd eich angen yn y llys dal yn ffurfio rhan "
                        + "o'ch gwasanaeth rheithgor ac ni fyddant yn cael eu hychwanegu ar y diwedd. Os oes gennych "
                        + "unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURTNAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.ATTENDANCE_TIME_CHANGED_COURT,
                    "The time of your attendance for Jury Service has changed to <attend_time>. The date remains the "
                        + "same. If you have any questions, please contact the jury office on <court_phone>.",
                    "Mae amser mynychu eich Gwasanaeth Rheithgor wedi newid i <attend_time>. Nid yw'r dyddiad wedi "
                        + "newid. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "<court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.COMPLETE_ATTENDED_COURT,
                    "You are not required to attend court any further and are formally discharged from jury service. "
                        + "If you have any questions, please contact the jury office on <court_phone>.",
                    "Nid oes angen ichi fynychu'r Llys eto ac rydych nawr wedi cael eich rhyddhau'n ffurfiol o'ch "
                        + "Gwasanaeth Rheithgor. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor "
                        + "drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.COMPLETE_NOT_NEEDED_COURT,
                    "You are no longer required to attend court and are formally discharged from jury service. If you"
                        + " have any questions, please contact the jury office on <court_phone>.",
                    "Nid oes angen ichi fynychu'r Llys mwyach ac rydych nawr wedi cael eich rhyddhau'n ffurfiol o'ch "
                        + "Gwasanaeth Rheithgor. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor "
                        + "drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.NEXT_ATTENDANCE_DATE_COURT,
                    "You are next required to attend for Jury Service on <attend_date> at <attend_time> at "
                        + "<court_name> Court. If you have any questions, please contact the jury office on "
                        + "<court_phone>.",
                    "Mae angen i chi fynychu'r llys eto ar gyfer eich Gwasanaeth Rheithgor ar <attend_date> am "
                        + "<attend_time> yn Llys<welsh_courtname>. Os oes gennych unrhyw gwestiynau, cysylltwch â'r "
                        + "swyddfa rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURTNAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.ON_CALL_COURT,
                    "You are not required to attend for Jury Service on <attend_date>. We will be in touch by "
                        + "text/email with regards to your next attendance. If you have any questions, please contact"
                        + " the jury office on <court_phone>.",
                    "Nid oes angen ichi fynychu'r Llys ar gyfer eich Gwasanaeth Rheithgor ar <attend_date>. Byddwn yn"
                        + " cysylltu â chi drwy neges testun/e-bost ynghylch pryd fydd angen i chi fynychu'r Llys eto"
                        + ". Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "<court_phone>.",
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.PLEASE_CONTACT_COURT,
                    "Please contact <court_name> Jury Office on <court_phone> with regard to your jury service.",
                    "Cysylltwch â Swyddfa Rheithgor<welsh_courtname> drwy ffonio <court_phone>  ynghylch eich "
                        + "gwasanaeth rheithgor.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURTNAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.DELAYED_START_COURT,
                    "You are due for Jury Service on <attend_date>, but please do not attend the court until "
                        + "contacted by the Jury Team. Please attend work if you are able, you will receive "
                        + "sufficient notice when required to attend. If you have any questions, please contact the "
                        + "jury office on <court_phone>.",
                    "Rydych i fod i ddechrau eich Gwasanaeth Rheithgor ar <attend_date>, ond peidiwch â mynd i'r llys"
                        + " hyd nes bydd y Tîm Rheithgor yn cysylltu â chi. Ewch i'ch gwaith os allwch chi oherwydd "
                        + "byddwch yn cael digon o rybudd pan fydd angen ichi fynychu'r llys. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.SELECTION_COURT,
                    "You have been selected to form a panel from which a jury will be chosen. Please attend "
                        + "<court_name> Court on <attend_date> at <attend_time>. If you have any questions, please "
                        + "contact the jury office on <court_phone>.",
                    "Rydych wedi cael eich dewis i fod yn rhan o banel a bydd Rheithgor yn cael ei ddewis o blith y "
                        + "panel hwnnw. Ewch i Lys<welsh_courtname> ar <attend_date> am <attend_time>. Os oes gennych"
                        + " unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_ENGLISH_COURT_NAME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURTNAME.toPlaceholder(),
                        TestData.Placeholder.ATTEND_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_ATTEND_TIME.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.BAD_WEATHER_COURT,
                    "Due to local adverse weather conditions, you are not required to attend the court until further "
                        + "notice. Please await further information. If you have any questions, please contact the "
                        + "jury office on <court_phone>.",
                    "Oherwydd amodau tywydd gwael yn lleol, nid oes angen ichi fynychu'r llys hyd nes y dywedir "
                        + "wrthych yn wahanol. Arhoswch am ragor o wybodaeth. Os oes gennych unrhyw gwestiynau, "
                        + "cysylltwch â'r swyddfa rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.CHECK_INBOX_COURT,
                    "If you've provided us with an email address, we'll use this to keep in touch. Please check your "
                        + "inbox regularly including your junk and spam mailbox.",
                    "Eich gwasanaeth rheithgor. Os ydych wedi rhoi eich cyfeiriad e-bost i ni, byddwn yn defnyddio "
                        + "hwn i gysylltu â chi. Gwiriwch eich mewnflwch yn rheolaidd gan gynnwys eich blwch junk a "
                        + "spam.",
                    List.of(),
                    List.of(),
                    MessageType.SendType.SMS);

                addTest(MessageType.BRING_LUNCH_COURT,
                    "Your jury panel is likely to go into deliberation tomorrow. Please bring a packed lunch to court"
                        + " as you will not be able to leave the deliberation room to get refreshments. Please do not"
                        + " bring metal cutlery or glass. If you have any questions, please contact the jury office "
                        + "on <court_phone>.",
                    "Mae eich panel rheithgor yn debygol o gychwyn trafod yn yr ystafell ymneilltuo yfory. Dewch â "
                        + "phecyn bwyd gyda chi i'r llys oherwydd ni chaniateir i chi adael yr ystafell ymneilltuo i "
                        + "nôl bwyd. Peidiwch â dod â chyllell a fforc metel nac unrhyw eitemau gwydr gyda chi. Os "
                        + "oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio <court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.EXCUSED_COURT,
                    "You have been excused from attending jury service on this occasion and are no longer required to"
                        + " attend court. If you have any questions, please contact the jury office on <court_phone>.",
                    "Rydych wedi cael eich esgusodi o wasanaethu ar reithgor ac nid oes rhaid i chi fynd i'r llys "
                        + "mwyach. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "<court_phone>.",
                    List.of(
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL_AND_SMS);

                addTest(MessageType.SENTENCING_INVITE_COURT,
                    "The defendant in the trial in which you were a juror is being sentenced on <sentence_date>.\n\n"
                        + "Please contact the court if you wish to attend on <court_phone>.\n\n"
                        + "Alternatively contact the court after the hearing date quoting reference number <trial_no>"
                        + " if you would like to know the sentence.\n\n"
                        + "Please note that jurors do not have a role in sentencing, and as such your attendance is "
                        + "entirely voluntary and travel and subsistence payments cannot be claimed.\n\n"
                        + "Please do not reply to this email as this mailbox is unmonitored.",
                    "Bydd y diffynnydd yn y treial yr oeddech yn rheithiwr ynddo yn cael ei ddedfrydu ar "
                        + "<sentence_date>.\n\n"
                        + "Cysylltwch â'r llys os ydych yn dymuno bod yn bresennol drwy ffonio <court_phone>.\n\n"
                        + "Fel arall, cysylltwch â'r llys ar ôl dyddiad y gwrandawiad, gan ddyfynnu'r cyfeirnod "
                        + "<trial_no> os hoffech wybod beth oedd y ddedfryd.\n\n"
                        + "Noder, nid oes gan reithwyr rôl i'w chwarae wrth ddedfrydu diffynyddion ac felly mae eich "
                        + "presenoldeb yn gyfan gwbl wirfoddol ac ni allwch hawlio costau teithio a chynhaliaeth.\n\n"
                        + "Peidiwch ag ymateb i'r neges e-bost hon oherwydd nid yw'r mewnflwch hwn yn cael ei fonitro.",
                    List.of(
                        TestData.Placeholder.SENTENCE_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder(),
                        TestData.Placeholder.TRIAL_NUMBER.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.SENTENCE_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder(),
                        TestData.Placeholder.TRIAL_NUMBER.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL);

                addTest(MessageType.SENTENCING_DATE_COURT,
                    "The defendant in the trial in which you were a juror is being sentenced on <sentence_date>.\n\n"
                        + "Please contact the court after the hearing date on <court_phone> quoting reference number "
                        + "<trial_no> if you would like to know the sentence.\n\n"
                        + "Please do not reply to this email as this mailbox is unmonitored.",
                    "Bydd y diffynnydd yn y treial yr oeddech yn rheithiwr ynddo yn cael ei ddedfrydu ar "
                        + "<sentence_date>.\n\n"
                        + "Cysylltwch â'r Llys ar ôl dyddiad y gwrandawiad drwy ffonio <court_phone> a dyfynnu'r "
                        + "cyfeirnod <trial_no> os hoffech wybod beth oedd y ddedfryd.\n\n"
                        + "Peidiwch ag ymateb i'r neges e-bost hon oherwydd nid yw'r mewnflwch hwn yn cael ei fonitro.",
                    List.of(
                        TestData.Placeholder.SENTENCE_DATE.toPlaceholder(),
                        TestData.Placeholder.ENGLISH_COURT_PHONE.toPlaceholder(),
                        TestData.Placeholder.TRIAL_NUMBER.toPlaceholder()
                    ),
                    List.of(
                        TestData.Placeholder.SENTENCE_DATE.toPlaceholder(),
                        TestData.Placeholder.WELSH_COURT_PHONE.toPlaceholder(),
                        TestData.Placeholder.TRIAL_NUMBER.toPlaceholder()
                    ),
                    MessageType.SendType.EMAIL);
            }

            @TestFactory
            @DisplayName("Generated")
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert"//False positive
            })
            Stream<DynamicContainer> tests() {
                return tests.stream();
            }


            private ResponseEntity<ViewMessageTemplateDto> triggerValid(
                MessageType messageType, String locCode) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<ViewMessageTemplateDto> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET, toUri(messageType, locCode)),
                    ViewMessageTemplateDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }

            private void addTest(MessageType messageType, String englishTemplate, String welshTemplate,
                                 List<ViewMessageTemplateDto.Placeholder> englishPlaceholders,
                                 List<ViewMessageTemplateDto.Placeholder> welshPlaceholders,
                                 MessageType.SendType sendType) {
                this.tests.add(DynamicContainer.dynamicContainer(messageType.name(),
                    List.of(
                        DynamicTest.dynamicTest("English",
                            () -> triggerAndValidate(messageType, englishTemplate, null, englishPlaceholders,
                                sendType)),
                        DynamicTest.dynamicTest("Welsh",
                            () -> triggerAndValidate(messageType, englishTemplate, welshTemplate, welshPlaceholders,
                                sendType))
                    )
                ));
            }

            private void triggerAndValidate(MessageType messageType, String englishTemplate, String welshTemplate,
                                            List<ViewMessageTemplateDto.Placeholder> placeholders,
                                            MessageType.SendType sendType) throws Exception {
                boolean isEnglishOnly = welshTemplate == null;
                ResponseEntity<ViewMessageTemplateDto> response = triggerValid(messageType,
                    isEnglishOnly ? TestData.ENGLISH_LOC_CODE : TestData.WELSH_LOC_CODE);

                ViewMessageTemplateDto responseBody = response.getBody();
                assertThat(responseBody).isNotNull();
                assertThat(responseBody.getSendType()).isNotNull().isEqualTo(sendType);
                assertThat(responseBody.getMessageTemplateEnglish()).isNotNull().isEqualTo(englishTemplate);

                if (isEnglishOnly) {
                    assertThat(responseBody.getMessageTemplateWelsh()).isNull();
                } else {
                    assertThat(responseBody.getMessageTemplateWelsh()).isNotNull().isEqualTo(welshTemplate);
                }
                List<ViewMessageTemplateDto.Placeholder> editablePlaceholders = placeholders
                    .stream().filter(ViewMessageTemplateDto.Placeholder::isEditable)
                    .toList();

                assertThat(responseBody.getPlaceholders()).hasSize(editablePlaceholders.size());
                assertThat(responseBody.getPlaceholders()).containsAll(editablePlaceholders);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(
                String messageType, String locCode, String court) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", court);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET, toUri(messageType, locCode)),
                    String.class);
            }

            @Test
            void unauthorisedNotMemberOfCourt() throws Exception {
                assertForbiddenResponse(triggerInvalid(
                    MessageType.SELECTION_COURT.name(),
                    "415",
                    "416"
                ), toUrl(MessageType.SELECTION_COURT.name(), "415"));

            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid(MessageType.SELECTION_COURT.name(),
                        "INVALID",
                        "INVALID"
                    ),
                    "getMessageDetails.locCode: size must be between 3 and 3"
                );
            }

            @Test
            void invalidMessageType() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid("INVALID",
                        "415",
                        "415"
                    ),
                    "INVALID is the incorrect data type or is not in the expected format (message_type)"
                );

            }
        }
    }

    @Nested
    @DisplayName("GET " + PostSearch.URL)
    @Sql({"/db/mod/truncate.sql", "/db/MessagingControllerITest_postSearchSetup.sql"
    })
    class PostSearch {
        private static final String URL = BASE_URL + "/search/{loc_code}";


        private URI toUri(String locCode, boolean isSimple) {
            return URI.create(toUrl(locCode, isSimple));
        }

        private URI toUri(String locCode) {
            return URI.create(toUrl(locCode));
        }

        protected String toUrl(String locCode) {
            return toUrl(locCode, false);
        }

        protected String toUrl(String locCode, boolean isSimple) {
            String url = URL.replace("{loc_code}", locCode);
            if (isSimple) {
                url += "?simple_response=true";
            }
            return url;
        }

        @Nested
        @DisplayName("Positive - Simple")
        class PositiveSimple extends Positive {
            PositiveSimple() {
                super(true);
            }
        }

        @Nested
        @DisplayName("Positive - full")
        class PositiveFull extends Positive {
            PositiveFull() {
                super(false);
            }

        }


        abstract class Positive {

            private final boolean isSimple;

            Positive(boolean isSimple) {
                this.isSimple = isSimple;
            }


            private ResponseEntity<PaginatedList<JurorToSendMessageCourt>> triggerValidCourt(
                String locCode, MessageSearch search) throws Exception {
                return triggerValidCourt("415", locCode, search);
            }

            private ResponseEntity<PaginatedList<JurorToSendMessageCourt>> triggerValidCourt(
                String owner, String locCode, MessageSearch search) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, Set.of(), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = template.exchange(
                    new RequestEntity<>(search, httpHeaders, POST, toUri(locCode, isSimple)),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }

            private ResponseEntity<PaginatedList<JurorToSendMessageBureau>> triggerValidBureau(
                String owner, String locCode, MessageSearch search) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner, locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<PaginatedList<JurorToSendMessageBureau>> response = template.exchange(
                    new RequestEntity<>(search, httpHeaders, POST, toUri(locCode, isSimple)),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }


            <T extends JurorToSendMessageBase> void assertJurorToSendMessage(PaginatedList<T> actualResponseBody,
                                                                             long expectedTotalItems,
                                                                             long expectedCurrentPage,
                                                                             long expectedTotalPages,
                                                                             boolean isCourt,
                                                                             JurorToSendMessageCourt... expectedList) {
                assertThat(actualResponseBody).isNotNull();
                assertThat(actualResponseBody.getCurrentPage()).as("Current page")
                    .isEqualTo(expectedCurrentPage);
                assertThat(actualResponseBody.getTotalItems()).as("Total Items")
                    .isEqualTo(expectedTotalItems);
                assertThat(actualResponseBody.getTotalPages()).as("Total pages")
                    .isEqualTo(expectedTotalPages);


                List<T> actualDataList = actualResponseBody.getData();
                assertThat(actualDataList).isNotNull().hasSize(expectedList.length);

                for (int index = 0; index < actualDataList.size(); index++) {
                    T actual = actualDataList.get(index);
                    JurorToSendMessageCourt expected = expectedList[index];
                    assertThat(actual.getJurorNumber()).isEqualTo(expected.getJurorNumber());
                    assertThat(actual.getPoolNumber()).isEqualTo(expected.getPoolNumber());
                    assertThat(actual.isWelshLanguage()).isEqualTo(expected.isWelshLanguage());
                    assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
                    assertThat(actual.getPhone()).isEqualTo(expected.getPhone());

                    if (isSimple) {
                        assertThat(actual.getFirstName()).isNull();
                        assertThat(actual.getLastName()).isNull();
                        assertThat(actual.getStatus()).isNull();
                        assertThat(actual.getDateDeferredTo()).isNull();
                        if (isCourt) {
                            assertThat(actual).isInstanceOf(JurorToSendMessageCourt.class);
                            JurorToSendMessageCourt court = (JurorToSendMessageCourt) actual;
                            assertThat(court.getCompletionDate()).isNull();
                            assertThat(court.getTrialNumber()).isNull();
                            assertThat(court.getOnCall()).isNull();
                            assertThat(court.getNextDueAtCourt()).isNull();
                        } else {
                            assertThat(actual).isInstanceOf(JurorToSendMessageBureau.class);
                        }
                    } else {
                        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
                        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
                        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
                        assertThat(actual.getDateDeferredTo()).isEqualTo(expected.getDateDeferredTo());
                        if (isCourt) {
                            assertThat(actual).isInstanceOf(JurorToSendMessageCourt.class);
                            JurorToSendMessageCourt court = (JurorToSendMessageCourt) actual;
                            assertThat(court.getCompletionDate()).isEqualTo(expected.getCompletionDate());
                            assertThat(court.getTrialNumber()).isEqualTo(expected.getTrialNumber());
                            assertThat(court.getOnCall()).isEqualTo(expected.getOnCall());
                            assertThat(court.getNextDueAtCourt()).isEqualTo(expected.getNextDueAtCourt());
                        } else {
                            assertThat(actual).isInstanceOf(JurorToSendMessageBureau.class);
                        }
                    }
                }
            }

            @Test
            void jurorNameSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME0")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());
                assertJurorToSendMessage(response.getBody(),
                    9, 1, 2, true,
                    TestData.getExpectedJurorToSendMessageFor100000001(),
                    TestData.getExpectedJurorToSendMessageFor100000002(),
                    TestData.getExpectedJurorToSendMessageFor100000003(),
                    TestData.getExpectedJurorToSendMessageFor100000004(),
                    TestData.getExpectedJurorToSendMessageFor100000005()
                );
            }

            @Test
            void jurorNumberSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .jurorSearch(JurorSearch.builder()
                            .jurorNumber("10000001")
                            .build())
                        .sortMethod(SortMethod.DESC)
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    10, 1, 2, true,
                    TestData.getExpectedJurorToSendMessageFor100000019(),
                    TestData.getExpectedJurorToSendMessageFor100000018(),
                    TestData.getExpectedJurorToSendMessageFor100000017(),
                    TestData.getExpectedJurorToSendMessageFor100000016(),
                    TestData.getExpectedJurorToSendMessageFor100000015()
                );
            }

            @Test
            void jurorPostcodeSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .jurorSearch(JurorSearch.builder()
                            .postcode("CH02 1AN")
                            .build())
                        .sortMethod(SortMethod.ASC)
                        .pageLimit(3)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    1, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000002()
                );
            }

            @Test
            void poolNumberSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .poolNumber("20000001")
                        .sortField(MessageSearch.SortField.POOL_NUMBER)
                        .sortMethod(SortMethod.ASC)
                        .pageLimit(3)
                        .pageNumber(2)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    19, 2, 7, true,
                    TestData.getExpectedJurorToSendMessageFor100000001(),
                    TestData.getExpectedJurorToSendMessageFor100000005(),
                    TestData.getExpectedJurorToSendMessageFor100000006()
                );
            }

            @Test
            void trialNumberSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .trialNumber("T100000000")
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    2, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000004(),
                    TestData.getExpectedJurorToSendMessageFor100000022()
                );
            }

            @Test
            void nextDueAtCourtDateSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .nextDueAtCourt(LocalDate.of(2022, 1, 1))
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000001(),
                    TestData.getExpectedJurorToSendMessageFor100000003(),
                    TestData.getExpectedJurorToSendMessageFor100000009(),
                    TestData.getExpectedJurorToSendMessageFor100000015()
                );
            }

            @Test
            void dateDeferredToSearch() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .dateDeferredTo(LocalDate.of(2022, 3, 12))
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    2, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000012(),
                    TestData.getExpectedJurorToSendMessageFor100000030()
                );
            }

            @Test
            void includeOnCall() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_ON_CALL))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME0")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    2, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000004(),
                    TestData.getExpectedJurorToSendMessageFor100000008()
                );
            }

            @Test
            void includeFailedToAttend() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_FAILED_TO_ATTEND))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000002(),
                    TestData.getExpectedJurorToSendMessageFor100000011(),
                    TestData.getExpectedJurorToSendMessageFor100000020(),
                    TestData.getExpectedJurorToSendMessageFor100000029()
                );
            }

            @Test
            void includeDeferred() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_DEFERRED))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000003(),
                    TestData.getExpectedJurorToSendMessageFor100000012(),
                    TestData.getExpectedJurorToSendMessageFor100000021(),
                    TestData.getExpectedJurorToSendMessageFor100000030()
                );
            }

            @Test
            void includeJurorsAndPanelled() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_JURORS_AND_PANELLED))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    6, 1, 2, true,
                    TestData.getExpectedJurorToSendMessageFor100000004(),
                    TestData.getExpectedJurorToSendMessageFor100000005(),
                    TestData.getExpectedJurorToSendMessageFor100000013(),
                    TestData.getExpectedJurorToSendMessageFor100000014(),
                    TestData.getExpectedJurorToSendMessageFor100000022()
                );
            }

            @Test
            void includeCompleted() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_COMPLETED))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    3, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000006(),
                    TestData.getExpectedJurorToSendMessageFor100000015(),
                    TestData.getExpectedJurorToSendMessageFor100000024()
                );
            }

            @Test
            void includeTransferred() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_TRANSFERRED))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    3, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000007(),
                    TestData.getExpectedJurorToSendMessageFor100000016(),
                    TestData.getExpectedJurorToSendMessageFor100000025()
                );
            }

            @Test
            void includeDisqualifiedAndExcused() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(MessageSearch.Filter.INCLUDE_DISQUALIFIED_AND_EXCUSED))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    6, 1, 2, true,
                    TestData.getExpectedJurorToSendMessageFor100000008(),
                    TestData.getExpectedJurorToSendMessageFor100000009(),
                    TestData.getExpectedJurorToSendMessageFor100000017(),
                    TestData.getExpectedJurorToSendMessageFor100000018(),
                    TestData.getExpectedJurorToSendMessageFor100000026()
                );
            }

            @Test
            void includeMultiple() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.INCLUDE_COMPLETED,
                            MessageSearch.Filter.INCLUDE_TRANSFERRED,
                            MessageSearch.Filter.INCLUDE_DISQUALIFIED_AND_EXCUSED
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(7)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    12, 1, 2, true,
                    TestData.getExpectedJurorToSendMessageFor100000006(),
                    TestData.getExpectedJurorToSendMessageFor100000007(),
                    TestData.getExpectedJurorToSendMessageFor100000008(),
                    TestData.getExpectedJurorToSendMessageFor100000009(),
                    TestData.getExpectedJurorToSendMessageFor100000015(),
                    TestData.getExpectedJurorToSendMessageFor100000016(),
                    TestData.getExpectedJurorToSendMessageFor100000017()
                );
            }

            @Test
            void showOnlyOnCall() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.SHOW_ONLY_ON_CALL
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    7, 1, 2, true,
                    TestData.getExpectedJurorToSendMessageFor100000004(),
                    TestData.getExpectedJurorToSendMessageFor100000008(),
                    TestData.getExpectedJurorToSendMessageFor100000012(),
                    TestData.getExpectedJurorToSendMessageFor100000016(),
                    TestData.getExpectedJurorToSendMessageFor100000020()
                );
            }

            @Test
            void showOnlyFailedToAttend() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.SHOW_ONLY_FAILED_TO_ATTEND
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000002(),
                    TestData.getExpectedJurorToSendMessageFor100000011(),
                    TestData.getExpectedJurorToSendMessageFor100000020(),
                    TestData.getExpectedJurorToSendMessageFor100000029()
                );
            }

            @Test
            void showOnlyDeferred() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.SHOW_ONLY_DEFERRED
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000003(),
                    TestData.getExpectedJurorToSendMessageFor100000012(),
                    TestData.getExpectedJurorToSendMessageFor100000021(),
                    TestData.getExpectedJurorToSendMessageFor100000030()
                );
            }

            @Test
            void showOnlyDeferredAsBureau() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageBureau>> response = triggerValidBureau("400", "415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.SHOW_ONLY_DEFERRED
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, false,
                    TestData.getExpectedJurorToSendMessageFor100000003(),
                    TestData.getExpectedJurorToSendMessageFor100000012(),
                    TestData.getExpectedJurorToSendMessageFor100000021(),
                    TestData.getExpectedJurorToSendMessageFor100000030()
                );
            }

            @Test
            void showOnlyResponded() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.SHOW_ONLY_RESPONDED
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    4, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000001(),
                    TestData.getExpectedJurorToSendMessageFor100000010(),
                    TestData.getExpectedJurorToSendMessageFor100000019(),
                    TestData.getExpectedJurorToSendMessageFor100000028()
                );
            }

            @Test
            void showOnlyMultiple() throws Exception {
                ResponseEntity<PaginatedList<JurorToSendMessageCourt>> response = triggerValidCourt("415",
                    MessageSearch.builder()
                        .filters(List.of(
                            MessageSearch.Filter.SHOW_ONLY_RESPONDED,
                            MessageSearch.Filter.SHOW_ONLY_ON_CALL
                        ))
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("FNAME")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build());

                assertJurorToSendMessage(response.getBody(),
                    1, 1, 1, true,
                    TestData.getExpectedJurorToSendMessageFor100000028()
                );
            }

        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(
                String locCode, String court, MessageSearch search) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", court);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                return template.exchange(
                    new RequestEntity<>(search, httpHeaders, POST, toUri(locCode)),
                    String.class);
            }

            @Test
            void notFound() throws Exception {
                assertNotFound(triggerInvalid("415", "415",
                        MessageSearch.builder()
                            .jurorSearch(JurorSearch.builder()
                                .jurorName("UNKNOWN")
                                .build())
                            .pageLimit(5)
                            .pageNumber(1)
                            .build()
                    ), toUrl("415"),
                    "No jurors found that meet the search criteria");
            }

            @Test
            void unauthorisedNotMemberOfCourt() throws Exception {
                assertForbiddenResponse(triggerInvalid(
                    "415",
                    "416",
                    MessageSearch.builder()
                        .jurorSearch(JurorSearch.builder()
                            .jurorName("UNKNOWN")
                            .build())
                        .pageLimit(5)
                        .pageNumber(1)
                        .build()
                ), toUrl("415"));

            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid("INVALID", "INVALID",
                        MessageSearch.builder()
                            .jurorSearch(JurorSearch.builder()
                                .jurorName("FNAME")
                                .build())
                            .pageLimit(5)
                            .pageNumber(1)
                            .build()
                    ),
                    "postSearch.locCode: size must be between 3 and 3"
                );
            }

            @Test
            void badPayload() throws Exception {
                assertInvalidPayload(
                    triggerInvalid("415", "415",
                        MessageSearch.builder()
                            .jurorSearch(JurorSearch.builder()
                                .jurorName("FNAME")
                                .jurorNumber("123")
                                .build())
                            .pageLimit(5)
                            .pageNumber(1)
                            .build()
                    ),
                    new RestResponseEntityExceptionHandler.FieldError("jurorSearch.jurorName",
                        "Field jurorName should be excluded if any of the following fields are present: [jurorNumber,"
                            + " postcode]")
                );
            }
        }
    }

    @Nested
    @DisplayName("POST " + SendMessage.URL)
    @Sql({"/db/mod/truncate.sql", "/db/MessagingControllerITest_sendMessageSetup.sql"
    })
    class SendMessage {

        private static final String URL = BASE_URL + "/send/{message_type}/{loc_code}";
        private static final String ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE = "100000002";
        private static final String ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE = "200000002";
        private static final String ENGLISH_JUROR_NUMBER_WELSH_FLAG_TRUE = "100000004";
        private static final String ENGLISH_POOL_NUMBER_WELSH_FLAG_TRUE = "200000014";
        private static final String WELSH_JUROR_NUMBER_WELSH_FLAG_FALSE = "100000014";
        private static final String WELSH_POOL_NUMBER_WELSH_FLAG_FALSE = "200000015";
        private static final String WELSH_JUROR_NUMBER_WELSH_FLAG_TRUE = "100000001";
        private static final String WELSH_POOL_NUMBER_WELSH_FLAG_TRUE = "200000015";

        protected URI toUri(MessageType messageType, String locCode) {
            return toUri(messageType.name(), locCode);
        }

        protected URI toUri(String messageType, String locCode) {
            return URI.create(toUrl(messageType, locCode));
        }

        protected String toUrl(String messageType, String locCode) {
            return URL.replace("{message_type}", messageType)
                .replace("{loc_code}", locCode);
        }


        @Nested
        @DisplayName("Positive")
        class Positive {

            protected final List<DynamicContainer> tests;


            protected Positive() {
                this.tests = new ArrayList<>();
                addTest(
                    MessageType.REMIND_TO_ATTEND,
                    "Reminder: Please attend for your Jury Service at WARRINGTON Court on 01/01/2023 at "
                        + "05:00. If you have any questions, please contact the jury office on 01244 356726.",
                    "Reminder: Please attend for your Jury Service at CARMARTHEN Court on 01/01/2023 at "
                        + "05:00. If you have any questions, please contact the jury office on 01792 637000.",
                    "Nodyn Atgoffa: Cofiwch fynychu eich Gwasanaeth Rheithgor yn LlysCAERFYRDDIN ar 01/01/2023"
                        + " am 05:00. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy"
                        + " ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01",
                        "<attend_time>", "05:00"
                    ),
                    "Reminder to attend",
                    "Welsh Reminder to attend",
                    true,
                    true
                );
                addTest(
                    MessageType.FAILED_TO_ATTEND_COURT,
                    "You failed to attend for Jury Service at WARRINGTON Court on 01/01/2023. Please contact "
                        + "the jury office on 01244 356726.",
                    "You failed to attend for Jury Service at CARMARTHEN Court on 01/01/2023. Please contact "
                        + "the jury office on 01792 637000.",
                    "Bu ichi fethu â mynychu eich Gwasanaeth Rheithgor yn LlysCAERFYRDDIN ar 01/01/2023. "
                        + "Cysylltwch â'r Swyddfa Rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01"
                    ),
                    "Failed to attend",
                    "Welsh Failed to attend",
                    true,
                    true
                );
                addTest(
                    MessageType.ATTENDANCE_DATE_AND_TIME_CHANGED_COURT,
                    "The date of your attendance for Jury Service has changed to 01/01/2023 at 05:00 at "
                        + "WARRINGTON Court. The days you are not required at court still form part of your jury "
                        + "service and will not be added on at the end. If you have any questions, please contact the"
                        + " jury office on 01244 356726.",
                    "The date of your attendance for Jury Service has changed to 01/01/2023 at 05:00 at "
                        + "CARMARTHEN Court. The days you are not required at court still form part of your jury "
                        + "service and will not be added on at the end. If you have any questions, please contact the"
                        + " jury office on 01792 637000.",
                    "Mae dyddiad mynychu eich Gwasanaeth Rheithgor wedi newid i 01/01/2023 am 05:00 yn "
                        + "LlysCAERFYRDDIN. Mae'r dyddiau pan na fydd eich angen yn y llys dal yn ffurfio rhan o'ch "
                        + "gwasanaeth rheithgor ac ni fyddant yn cael eu hychwanegu ar y diwedd. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01",
                        "<attend_time>", "05:00"
                    ),
                    "Attend date & time changed",
                    "Welsh Attend date changed",
                    true,
                    true
                );
                addTest(
                    MessageType.ATTENDANCE_TIME_CHANGED_COURT,
                    "The time of your attendance for Jury Service has changed to 05:00. The date remains the same. "
                        + "If you have any questions, please contact the jury office on 01244 356726.",
                    "The time of your attendance for Jury Service has changed to 05:00. The date remains the same. "
                        + "If you have any questions, please contact the jury office on 01792 637000.",
                    "Mae amser mynychu eich Gwasanaeth Rheithgor wedi newid i 05:00. Nid yw'r dyddiad wedi newid. Os"
                        + " oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_time>", "05:00"
                    ),
                    "Attend time changed",
                    "Welsh Attend time changed",
                    true,
                    true
                );
                addTest(
                    MessageType.COMPLETE_ATTENDED_COURT,
                    "You are not required to attend court any further and are formally discharged from jury service. "
                        + "If you have any questions, please contact the jury office on 01244 356726.",
                    "You are not required to attend court any further and are formally discharged from jury service. "
                        + "If you have any questions, please contact the jury office on 01792 637000.",
                    "Nid oes angen ichi fynychu'r Llys eto ac rydych nawr wedi cael eich rhyddhau'n ffurfiol o'ch "
                        + "Gwasanaeth Rheithgor. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor "
                        + "drwy "
                        + "ffonio 01792 637000.",
                    Map.of(),
                    "Complete (attended)",
                    "Welsh Complete (attended)",
                    true,
                    true
                );
                addTest(
                    MessageType.COMPLETE_NOT_NEEDED_COURT,
                    "You are no longer required to attend court and are formally discharged from jury service. If you"
                        + " have any questions, please contact the jury office on 01244 356726.",
                    "You are no longer required to attend court and are formally discharged from jury service. If you"
                        + " have any questions, please contact the jury office on 01792 637000.",
                    "Nid oes angen ichi fynychu'r Llys mwyach ac rydych nawr wedi cael eich rhyddhau'n ffurfiol o'ch "
                        + "Gwasanaeth Rheithgor. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor "
                        + "drwy "
                        + "ffonio 01792 637000.",
                    Map.of(),
                    "Complete (not needed)",
                    "Welsh Complete (not needed)",
                    true,
                    true
                );
                addTest(
                    MessageType.NEXT_ATTENDANCE_DATE_COURT,
                    "You are next required to attend for Jury Service on 03/01/2023 at 08:00 at WARRINGTON "
                        + "Court. If you have any questions, please contact the jury office on 01244 356726.",
                    "You are next required to attend for Jury Service on 03/01/2023 at 08:00 at CARMARTHEN "
                        + "Court. If you have any questions, please contact the jury office on 01792 637000.",
                    "Mae angen i chi fynychu'r llys eto ar gyfer eich Gwasanaeth Rheithgor ar 03/01/2023 "
                        + "am 08:00 yn LlysCAERFYRDDIN. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa "
                        + "rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-03",
                        "<attend_time>", "08:00"
                    ),
                    "Next date",
                    "Welsh Next date",
                    true,
                    true
                );
                addTest(
                    MessageType.ON_CALL_COURT,
                    "You are not required to attend for Jury Service on 01/01/2023. We will be in touch by "
                        + "text/email with regards to your next attendance. If you have any questions, please contact"
                        + " the jury office on 01244 356726.",
                    "You are not required to attend for Jury Service on 01/01/2023. We will be in touch by "
                        + "text/email with regards to your next attendance. If you have any questions, please contact"
                        + " the jury office on 01792 637000.",
                    "Nid oes angen ichi fynychu'r Llys ar gyfer eich Gwasanaeth Rheithgor ar 01/01/2023. "
                        + "Byddwn yn cysylltu â chi drwy neges testun/e-bost ynghylch pryd fydd angen i chi fynychu'r "
                        + "Llys eto. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01"
                    ),
                    "On call",
                    "Welsh On call",
                    true,
                    true
                );
                addTest(
                    MessageType.PLEASE_CONTACT_COURT,
                    "Please contact WARRINGTON Jury Office on 01244 356726 with regard to your jury service.",
                    "Please contact CARMARTHEN Jury Office on 01792 637000 with regard to your jury service.",
                    "Cysylltwch â Swyddfa RheithgorCAERFYRDDIN drwy ffonio 01792 637000  ynghylch eich gwasanaeth "
                        + "rheithgor.",
                    Map.of(),
                    "Please contact",
                    "Welsh Please contact",
                    true,
                    true
                );
                addTest(
                    MessageType.DELAYED_START_COURT,
                    "You are due for Jury Service on 01/01/2023, but please do not attend the court until "
                        + "contacted by the Jury Team. Please attend work if you are able, you will receive sufficient "
                        + "notice when required to attend. If you have any questions, please contact the jury office "
                        + "on 01244 356726.",
                    "You are due for Jury Service on 01/01/2023, but please do not attend the court until "
                        + "contacted by the Jury Team. Please attend work if you are able, you will receive sufficient "
                        + "notice when required to attend. If you have any questions, please contact the jury office "
                        + "on 01792 637000.",
                    "Rydych i fod i ddechrau eich Gwasanaeth Rheithgor ar 01/01/2023, ond peidiwch â mynd i'r"
                        + " llys hyd nes bydd y Tîm Rheithgor yn cysylltu â chi. Ewch i'ch gwaith os allwch chi "
                        + "oherwydd "
                        + "byddwch yn cael digon o rybudd pan fydd angen ichi fynychu'r llys. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01"
                    ),
                    "Delayed start",
                    "Welsh Delayed start",
                    true,
                    true
                );
                addTest(
                    MessageType.SELECTION_COURT,
                    "You have been selected to form a panel from which a jury will be chosen. Please attend "
                        + "WARRINGTON Court on 02/01/2023 at 15:30. If you have any questions, please "
                        + "contact the jury office on 01244 356726.",
                    "You have been selected to form a panel from which a jury will be chosen. Please attend "
                        + "CARMARTHEN Court on 02/01/2023 at 15:30. If you have any questions, please "
                        + "contact the jury office on 01792 637000.",
                    "Rydych wedi cael eich dewis i fod yn rhan o banel a bydd Rheithgor yn cael ei ddewis o blith y "
                        + "panel hwnnw. Ewch i LysCAERFYRDDIN ar 02/01/2023 am 15:30. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-02",
                        "<attend_time>", "15:30"
                    ),
                    "Selection",
                    "Welsh Selection",
                    true,
                    true
                );
                addTest(
                    MessageType.BAD_WEATHER_COURT,
                    "Due to local adverse weather conditions, you are not required to attend the court until further "
                        + "notice. Please await further information. If you have any questions, please contact the "
                        + "jury office on 01244 356726.",
                    "Due to local adverse weather conditions, you are not required to attend the court until further "
                        + "notice. Please await further information. If you have any questions, please contact the "
                        + "jury office on 01792 637000.",
                    "Oherwydd amodau tywydd gwael yn lleol, nid oes angen ichi fynychu'r llys hyd nes y dywedir "
                        + "wrthych yn wahanol. Arhoswch am ragor o wybodaeth. Os oes gennych unrhyw gwestiynau, "
                        + "cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(),
                    "Bad weather",
                    "Welsh Bad weather",
                    true,
                    true
                );
                addTest(
                    MessageType.CHECK_INBOX_COURT,
                    "If you've provided us with an email address, we'll use this to keep in touch. Please check your "
                        + "inbox regularly including your junk and spam mailbox.",
                    "If you've provided us with an email address, we'll use this to keep in touch. Please check your "
                        + "inbox regularly including your junk and spam mailbox.",
                    "Eich gwasanaeth rheithgor. Os ydych wedi rhoi eich cyfeiriad e-bost i ni, byddwn yn defnyddio "
                        + "hwn i gysylltu â chi. Gwiriwch eich mewnflwch yn rheolaidd gan gynnwys eich blwch junk a "
                        + "spam.",
                    Map.of(),
                    "Check Junk/Spam (TXT only)",
                    "Welsh Check Junk/Spam (TXT)",
                    true,
                    false
                );
                addTest(
                    MessageType.BRING_LUNCH_COURT,
                    "Your jury panel is likely to go into deliberation tomorrow. Please bring a packed lunch to court"
                        + " as you will not be able to leave the deliberation room to get refreshments. Please do not "
                        + "bring metal cutlery or glass. If you have any questions, please contact the jury office on "
                        + "01244 356726.",
                    "Your jury panel is likely to go into deliberation tomorrow. Please bring a packed lunch to court"
                        + " as you will not be able to leave the deliberation room to get refreshments. Please do not "
                        + "bring metal cutlery or glass. If you have any questions, please contact the jury office on "
                        + "01792 637000.",
                    "Mae eich panel rheithgor yn debygol o gychwyn trafod yn yr ystafell ymneilltuo yfory. Dewch â "
                        + "phecyn bwyd gyda chi i'r llys oherwydd ni chaniateir i chi adael yr ystafell ymneilltuo i "
                        + "nôl bwyd. Peidiwch â dod â chyllell a fforc metel nac unrhyw eitemau gwydr gyda chi. Os oes "
                        + "gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(),
                    "Bring lunch",
                    "Welsh Bring Lunch",
                    true,
                    true
                );
                addTest(
                    MessageType.EXCUSED_COURT,
                    "You have been excused from attending jury service on this occasion and are no longer required to"
                        + " attend court. If you have any questions, please contact the jury office on 01244 356726.",
                    "You have been excused from attending jury service on this occasion and are no longer required to"
                        + " attend court. If you have any questions, please contact the jury office on 01792 637000.",
                    "Rydych wedi cael eich esgusodi o wasanaethu ar reithgor ac nid oes rhaid i chi fynd i'r llys "
                        + "mwyach. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "01792 "
                        + "637000.",
                    Map.of(),
                    "Excused",
                    "Welsh Excused",
                    true,
                    true
                );
                this.tests.add(DynamicContainer.dynamicContainer(MessageType.SENTENCING_INVITE_COURT.name(),
                    List.of(
                        DynamicTest.dynamicTest(MessageType.SENTENCING_INVITE_COURT + " English Court - Email",
                            () -> addTest(MessageType.SENTENCING_INVITE_COURT,
                                TestData.ENGLISH_LOC_CODE,
                                Map.of(
                                    "<sentence_date>", "2023-01-02",
                                    "<trial_no>", "T100000002"
                                ),
                                JurorData.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .jurorPoolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishOtherInfo("Sentence invite(email only)")
                                    .englishMessage(
                                        """
                                            The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.

                                            Please contact the court if you wish to attend on 01244 356726.

                                            Alternatively contact the court after the hearing date quoting reference number T100000002 if you would like to know the sentence.

                                            Please note that jurors do not have a role in sentencing, and as such your attendance is entirely voluntary and travel and subsistence payments cannot be claimed.

                                            Please do not reply to this email as this mailbox is unmonitored.""")
                                    .welshMessage(null)
                                    .build(),
                                JurorData.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_TRUE)
                                    .jurorPoolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_TRUE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishOtherInfo("Sentence invite(email only)")
                                    .englishMessage("""
                                        The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.
                                                                                
                                        Please contact the court if you wish to attend on 01244 356726.
                                                                                
                                        Alternatively contact the court after the hearing date quoting reference number T100000002 if you would like to know the sentence.
                                                                                
                                        Please note that jurors do not have a role in sentencing, and as such your attendance is entirely voluntary and travel and subsistence payments cannot be claimed.
                                                                                
                                        Please do not reply to this email as this mailbox is unmonitored.""")
                                    .welshMessage(null)
                                    .build()
                            )
                        ),
                        DynamicTest.dynamicTest(MessageType.SENTENCING_INVITE_COURT + " Welsh Court - Email",
                            () -> addTest(MessageType.SENTENCING_INVITE_COURT,
                                TestData.WELSH_LOC_CODE,
                                Map.of(
                                    "<sentence_date>", "2023-01-02",
                                    "<trial_no>", "T100000003"
                                ),
                                JurorData.builder()
                                    .jurorNumber(WELSH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .jurorPoolNumber(WELSH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishOtherInfo("Sentence invite(email only)")
                                    .englishMessage(
                                        """
                                            The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.

                                            Please contact the court if you wish to attend on 01792 637000.

                                            Alternatively contact the court after the hearing date quoting reference number T100000003 if you would like to know the sentence.

                                            Please note that jurors do not have a role in sentencing, and as such your attendance is entirely voluntary and travel and subsistence payments cannot be claimed.

                                            Please do not reply to this email as this mailbox is unmonitored.""")
                                    .welshMessage(null)
                                    .build(),
                                JurorData.builder()
                                    .jurorNumber(WELSH_JUROR_NUMBER_WELSH_FLAG_TRUE)
                                    .jurorPoolNumber(WELSH_POOL_NUMBER_WELSH_FLAG_TRUE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishMessage(null)
                                    .welshOtherInfo("Welsh invite (email only)")
                                    .welshMessage("""
                                        Bydd y diffynnydd yn y treial yr oeddech yn rheithiwr ynddo yn cael ei ddedfrydu ar 02/01/2023.
                                                                                
                                        Cysylltwch â'r llys os ydych yn dymuno bod yn bresennol drwy ffonio 01792 637000.
                                                                                
                                        Fel arall, cysylltwch â'r llys ar ôl dyddiad y gwrandawiad, gan ddyfynnu'r cyfeirnod T100000003 os hoffech wybod beth oedd y ddedfryd.
                                                                                
                                        Noder, nid oes gan reithwyr rôl i'w chwarae wrth ddedfrydu diffynyddion ac felly mae eich presenoldeb yn gyfan gwbl wirfoddol ac ni allwch hawlio costau teithio a chynhaliaeth.
                                                                                
                                        Peidiwch ag ymateb i'r neges e-bost hon oherwydd nid yw'r mewnflwch hwn yn cael ei fonitro.""")
                                    .build())
                        )
                    )
                ));

                this.tests.add(DynamicContainer.dynamicContainer(MessageType.SENTENCING_DATE_COURT.name(),
                    List.of(
                        DynamicTest.dynamicTest(MessageType.SENTENCING_DATE_COURT + " English Court - Email",
                            () -> addTest(MessageType.SENTENCING_DATE_COURT,
                                TestData.ENGLISH_LOC_CODE,
                                Map.of(
                                    "<sentence_date>", "2023-01-02",
                                    "<trial_no>", "T100000002"
                                ),
                                JurorData.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .jurorPoolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishOtherInfo("Sentence date (email only)")
                                    .englishMessage("""
                                        The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.
                                                                                
                                        Please contact the court after the hearing date on 01244 356726 quoting reference number T100000002 if you would like to know the sentence.
                                                                                
                                        Please do not reply to this email as this mailbox is unmonitored.""")
                                    .welshMessage(null)
                                    .build(),
                                JurorData.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_TRUE)
                                    .jurorPoolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_TRUE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishOtherInfo("Sentence date (email only)")
                                    .englishMessage("""
                                        The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.
                                                                                
                                        Please contact the court after the hearing date on 01244 356726 quoting reference number T100000002 if you would like to know the sentence.
                                                                                
                                        Please do not reply to this email as this mailbox is unmonitored.""")
                                    .welshMessage(null)
                                    .build())
                        ),
                        DynamicTest.dynamicTest(MessageType.SENTENCING_DATE_COURT + " Welsh Court - Email",
                            () -> addTest(MessageType.SENTENCING_DATE_COURT,
                                TestData.WELSH_LOC_CODE,
                                Map.of(
                                    "<sentence_date>", "2023-01-02",
                                    "<trial_no>", "T100000003"
                                ),
                                JurorData.builder()
                                    .jurorNumber(WELSH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .jurorPoolNumber(WELSH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishOtherInfo("Sentence date (email only)")
                                    .englishMessage(
                                        """
                                            The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.
                                                                                        
                                            Please contact the court after the hearing date on 01792 637000 quoting reference number T100000003 if you would like to know the sentence.
                                                                                        
                                            Please do not reply to this email as this mailbox is unmonitored.""")
                                    .welshMessage(null)
                                    .build(),
                                JurorData.builder()
                                    .jurorNumber(WELSH_JUROR_NUMBER_WELSH_FLAG_TRUE)
                                    .jurorPoolNumber(WELSH_POOL_NUMBER_WELSH_FLAG_TRUE)
                                    .sendType(MessageType.SendType.EMAIL)
                                    .englishMessage(null)
                                    .welshOtherInfo("Welsh Sentence (email only)")
                                    .welshMessage("""
                                        Bydd y diffynnydd yn y treial yr oeddech yn rheithiwr ynddo yn cael ei ddedfrydu ar 02/01/2023.
                                                                                
                                        Cysylltwch â'r Llys ar ôl dyddiad y gwrandawiad drwy ffonio 01792 637000 a dyfynnu'r cyfeirnod T100000003 os hoffech wybod beth oedd y ddedfryd.
                                                                                
                                        Peidiwch ag ymateb i'r neges e-bost hon oherwydd nid yw'r mewnflwch hwn yn cael ei fonitro.""")
                                    .build())
                        )
                    )
                ));
            }

            private void triggerValid(
                MessageType messageType, String locCode, MessageSendRequest request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<String> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, toUri(messageType, locCode)),
                    String.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.NO_CONTENT);

                assertThat(response.getBody())
                    .isNull();
            }

            @TestFactory
            @DisplayName("Generated")
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert"//False positive
            })
            Stream<DynamicContainer> tests() {
                return tests.stream();
            }


            private void validateEnglishJurorNumberWelshFlagFalse(Message message,
                                                                  String messageText,
                                                                  MessageType messageType,
                                                                  MessageType.SendType sendType) {
                validateMessage(message,
                    ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE,
                    TestData.ENGLISH_LOC_CODE,
                    MessageType.SendType.SMS.equals(sendType) ? "07777000002" : null,
                    MessageType.SendType.EMAIL.equals(sendType) ? "FNAME02.LNAME02@email.com" : null,
                    ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE,
                    TestData.ENGLISH_SUBJECT,
                    messageText,
                    messageType.getEnglishMessageId()
                );
            }

            private void validateEnglishJurorNumberWelshFlagTrue(Message message,
                                                                 String messageText,
                                                                 MessageType messageType,
                                                                 MessageType.SendType sendType) {
                validateMessage(message,
                    ENGLISH_JUROR_NUMBER_WELSH_FLAG_TRUE,
                    TestData.ENGLISH_LOC_CODE,
                    MessageType.SendType.SMS.equals(sendType) ? "07777000004" : null,
                    MessageType.SendType.EMAIL.equals(sendType) ? "FNAME04.LNAME04@email.com" : null,
                    ENGLISH_POOL_NUMBER_WELSH_FLAG_TRUE,
                    TestData.ENGLISH_SUBJECT,
                    messageText,
                    messageType.getEnglishMessageId()
                );
            }

            private void validateWelshJurorNumberWelshFlagFalse(Message message,
                                                                String messageText,
                                                                MessageType messageType,
                                                                MessageType.SendType sendType) {
                validateMessage(message,
                    WELSH_JUROR_NUMBER_WELSH_FLAG_FALSE,
                    TestData.WELSH_LOC_CODE,
                    MessageType.SendType.SMS.equals(sendType) ? "07777000014" : null,
                    MessageType.SendType.EMAIL.equals(sendType) ? "FNAME14.LNAME14@email.com" : null,
                    WELSH_POOL_NUMBER_WELSH_FLAG_FALSE,
                    TestData.ENGLISH_SUBJECT,
                    messageText,
                    messageType.getEnglishMessageId()
                );
            }

            private void validateWelshJurorNumberWelshFlagTrue(Message message,
                                                               String messageText,
                                                               MessageType messageType,
                                                               MessageType.SendType sendType) {
                validateMessage(message,
                    WELSH_JUROR_NUMBER_WELSH_FLAG_TRUE,
                    TestData.WELSH_LOC_CODE,
                    MessageType.SendType.SMS.equals(sendType) ? "07777000001" : null,
                    MessageType.SendType.EMAIL.equals(sendType) ? "FNAME01.LNAME01@email.com" : null,
                    WELSH_POOL_NUMBER_WELSH_FLAG_TRUE,
                    TestData.WELSH_SUBJECT,
                    messageText,
                    messageType.getWelshMessageId()
                );
            }


            private void validateMessage(Message message,
                                         String jurorNumber,
                                         String locCode,
                                         String phone,
                                         String email,
                                         String poolNumber,
                                         String subject,
                                         String messageText,
                                         int messageId) {
                assertThat(message).isNotNull();
                assertThat(message.getJurorNumber()).isEqualTo(jurorNumber);
                assertThat(message.getFileDatetime()).isNotNull();
                assertThat(message.getUserName()).isEqualTo("COURT_USER");
                assertThat(message.getLocationCode()).isNotNull();
                assertThat(message.getLocationCode().getLocCode()).isEqualTo(locCode);
                assertThat(message.getPhone()).isEqualTo(phone);
                assertThat(message.getEmail()).isEqualTo(email);
                assertThat(message.getPoolNumber()).isEqualTo(poolNumber);
                assertThat(message.getSubject()).isEqualTo(subject);
                assertThat(message.getMessageText()).isEqualTo(messageText);
                assertThat(message.getMessageId()).isEqualTo(messageId);
                assertThat(message.getMessageRead()).isEqualTo("NR");
            }

            private void addTest(MessageType messageType, String englishMessage,
                                 String welshEnglishMessage, String welshMessage,
                                 Map<String, String> placeholders,
                                 String englishOtherInfo,
                                 String welshOtherInfo,
                                 boolean supportsSms,
                                 boolean supportsEmail) {
                List<DynamicTest> dynamicTests = new ArrayList<>();

                if (supportsEmail) {
                    dynamicTests.add(
                        DynamicTest.dynamicTest(messageType + " English Court - Email",
                            () -> addTest(messageType,
                                TestData.ENGLISH_LOC_CODE,
                                englishMessage,
                                null,
                                englishOtherInfo,
                                null,
                                placeholders,
                                MessageType.SendType.EMAIL,
                                MessageType.SendType.EMAIL)
                        )
                    );
                    dynamicTests.add(
                        DynamicTest.dynamicTest(messageType + " Welsh Court - Email",
                            () -> addTest(messageType,
                                TestData.WELSH_LOC_CODE,
                                welshEnglishMessage,
                                welshMessage,
                                englishOtherInfo,
                                welshOtherInfo,
                                placeholders,
                                MessageType.SendType.EMAIL,
                                MessageType.SendType.EMAIL)
                        )
                    );
                }
                if (supportsSms) {
                    dynamicTests.add(
                        DynamicTest.dynamicTest(messageType + " English Court - SMS",
                            () -> addTest(messageType,
                                TestData.ENGLISH_LOC_CODE,
                                englishMessage,
                                null,
                                englishOtherInfo,
                                null,
                                placeholders,
                                MessageType.SendType.SMS,
                                MessageType.SendType.SMS)
                        )
                    );
                    dynamicTests.add(
                        DynamicTest.dynamicTest(messageType + " Welsh Court - SMS",
                            () -> addTest(messageType,
                                TestData.WELSH_LOC_CODE,
                                welshEnglishMessage,
                                welshMessage,
                                englishOtherInfo,
                                welshOtherInfo,
                                placeholders,
                                MessageType.SendType.SMS,
                                MessageType.SendType.SMS)
                        )
                    );
                }
                if (supportsEmail && supportsSms) {
                    dynamicTests.add(
                        DynamicTest.dynamicTest(messageType + " English Court - Email & SMS",
                            () -> addTest(messageType,
                                TestData.ENGLISH_LOC_CODE,
                                englishMessage,
                                null,
                                englishOtherInfo,
                                null,
                                placeholders,
                                MessageType.SendType.EMAIL,
                                MessageType.SendType.SMS)
                        )
                    );
                    dynamicTests.add(
                        DynamicTest.dynamicTest(messageType + " Welsh Court - Email & SMS",
                            () -> addTest(messageType,
                                TestData.WELSH_LOC_CODE,
                                welshEnglishMessage,
                                welshMessage,
                                englishOtherInfo,
                                welshOtherInfo,
                                placeholders,
                                MessageType.SendType.EMAIL,
                                MessageType.SendType.SMS)
                        )
                    );
                }
                this.tests.add(DynamicContainer.dynamicContainer(messageType.name(), dynamicTests));
            }

            private void addTest(
                MessageType messageType,
                String locCode,
                String englishMessage,
                String welshMessage,
                String englishOtherInfo,
                String welshOtherInfo,
                Map<String, String> placeholders,
                MessageType.SendType user1SendType,
                MessageType.SendType user2SendType) throws Exception {
                if (TestData.ENGLISH_LOC_CODE.equals(locCode)) {
                    addTest(messageType,
                        locCode,
                        placeholders,
                        JurorData.builder()
                            .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                            .jurorPoolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                            .sendType(user1SendType)
                            .englishMessage(englishMessage)
                            .welshMessage(welshMessage)
                            .englishOtherInfo(englishOtherInfo)
                            .welshOtherInfo(welshOtherInfo)
                            .build(),
                        JurorData.builder()
                            .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_TRUE)
                            .jurorPoolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_TRUE)
                            .sendType(user2SendType)
                            .englishMessage(englishMessage)
                            .welshMessage(welshMessage)
                            .englishOtherInfo(englishOtherInfo)
                            .welshOtherInfo(welshOtherInfo)
                            .build());
                } else if (TestData.WELSH_LOC_CODE.equals(locCode)) {
                    addTest(messageType,
                        locCode,
                        placeholders,
                        JurorData.builder()
                            .jurorNumber(WELSH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                            .jurorPoolNumber(WELSH_POOL_NUMBER_WELSH_FLAG_FALSE)
                            .sendType(user1SendType)
                            .englishMessage(englishMessage)
                            .welshMessage(welshMessage)
                            .englishOtherInfo(englishOtherInfo)
                            .welshOtherInfo(welshOtherInfo)
                            .build(),
                        JurorData.builder()
                            .jurorNumber(WELSH_JUROR_NUMBER_WELSH_FLAG_TRUE)
                            .jurorPoolNumber(WELSH_POOL_NUMBER_WELSH_FLAG_TRUE)
                            .sendType(user2SendType)
                            .englishMessage(englishMessage)
                            .welshMessage(welshMessage)
                            .englishOtherInfo(englishOtherInfo)
                            .welshOtherInfo(welshOtherInfo)
                            .build());
                } else {
                    fail("Bad test");
                }
            }

            @Getter
            @Setter
            @Builder
            private static class JurorData {
                private String jurorPoolNumber;
                private String jurorNumber;
                private MessageType.SendType sendType;
                private String englishMessage;
                private String welshMessage;
                private String englishOtherInfo;
                private String welshOtherInfo;
            }

            private void addTest(
                MessageType messageType,
                String locCode,
                Map<String, String> placeholders,
                JurorData... data) throws Exception {

                List<MessageSendRequest.JurorAndSendType> jurors = Arrays.stream(data)
                    .map(jurorData -> MessageSendRequest.JurorAndSendType.builder()
                        .jurorNumber(jurorData.jurorNumber)
                        .poolNumber(jurorData.jurorPoolNumber)
                        .type(jurorData.sendType)
                        .build()).toList();

                triggerValid(messageType,
                    locCode,
                    MessageSendRequest.builder()
                        .jurors(jurors)
                        .placeholderValues(placeholders)
                        .build());

                Arrays.stream(data).forEach(jurorData -> validate(messageType, jurorData));
            }

            private void validate(MessageType messageType, JurorData data) {
                validate(messageType, data.getEnglishMessage(), data.getWelshMessage(),
                    data.getEnglishOtherInfo(), data.getWelshOtherInfo(),
                    data.getSendType(), data.getJurorNumber(), data.getJurorPoolNumber());
            }

            private void validate(MessageType messageType, String englishMessage,
                                  String welshMessage,
                                  String englishOtherInfo,
                                  String welshOtherInfo,
                                  MessageType.SendType sendType,
                                  String jurorNumber,
                                  String poolNumber) {
                List<Message> messages = messagesRepository.findAllByJurorNumber(jurorNumber);
                assertThat(messages).isNotNull().hasSize(1);
                Message message = messages.get(0);
                final String otherInfo;
                if (ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE.equals(jurorNumber)) {
                    validateEnglishJurorNumberWelshFlagFalse(
                        message,
                        englishMessage,
                        messageType,
                        sendType
                    );
                    otherInfo = englishOtherInfo;
                } else if (ENGLISH_JUROR_NUMBER_WELSH_FLAG_TRUE.equals(jurorNumber)) {
                    validateEnglishJurorNumberWelshFlagTrue(
                        message,
                        englishMessage,
                        messageType,
                        sendType
                    );
                    otherInfo = englishOtherInfo;
                } else if (WELSH_JUROR_NUMBER_WELSH_FLAG_FALSE.equals(jurorNumber)) {
                    validateWelshJurorNumberWelshFlagFalse(
                        message,
                        englishMessage,
                        messageType,
                        sendType
                    );
                    otherInfo = englishOtherInfo;
                } else if (WELSH_JUROR_NUMBER_WELSH_FLAG_TRUE.equals(jurorNumber)) {
                    validateWelshJurorNumberWelshFlagTrue(
                        message,
                        welshMessage != null ? welshMessage : englishMessage,
                        messageType,
                        sendType
                    );
                    otherInfo = welshOtherInfo;
                } else {
                    fail("Not validated");
                    otherInfo = null;
                }


                List<JurorHistory> history = jurorHistoryRepository.findByJurorNumber(jurorNumber);
                JurorHistory historyItem = history.get(history.size() - 1);
                assertThat(historyItem).isNotNull();
                assertThat(historyItem.getHistoryCode()).isEqualTo(HistoryCodeMod.NOTIFY_MESSAGE_REQUESTED);
                assertThat(historyItem.getJurorNumber()).isEqualTo(jurorNumber);
                assertThat(historyItem.getPoolNumber()).isEqualTo(poolNumber);
                assertThat(historyItem.getOtherInformation()).isEqualTo(otherInfo);
                assertThat(historyItem.getDateCreated()).isNotNull();
                assertThat(historyItem.getCreatedBy()).isEqualTo(COURT_USER);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(
                String messageType, String locCode, String court, MessageSendRequest request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", court);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, toUri(messageType, locCode)), String.class);
            }

            @Test
            void messageDoesNotSupportSms() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.SMS)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<sentence_date>", "2023-01-02",
                                "<trial_no>", "T100000002"
                            ))
                            .build()),
                    "This message type only supports: EMAIL",
                    MojException.BusinessRuleViolation.ErrorCode.INVALID_SEND_TYPE
                );
            }

            @Test
            void messageDoesNotSupportEmail() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.CHECK_INBOX_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()),
                    "This message type only supports: SMS",
                    MojException.BusinessRuleViolation.ErrorCode.INVALID_SEND_TYPE
                );
            }

            @Test
            void jurorNotOnTrial() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber("100000015")
                                    .poolNumber("200000014")
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<sentence_date>", "2023-01-02",
                                "<trial_no>", "T100000004"
                            ))
                            .build()),
                    "Juror is not apart of trial",
                    JUROR_NOT_APART_OF_TRIAL
                );
            }

            @Test
            void trialDoesNotExist() throws Exception {
                assertNotFound(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber("100000015")
                                    .poolNumber("200000014")
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<sentence_date>", "2023-01-02",
                                "<trial_no>", "T1234"
                            ))
                            .build()),
                    toUrl(MessageType.SENTENCING_DATE_COURT.name(), "462"),
                    "Trial does not exist for trial number: T1234"
                );
            }

            @Test
            void notFoundJuror() throws Exception {
                assertNotFound(
                    triggerInvalid(MessageType.CHECK_INBOX_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber("200000022")
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.SMS)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()),
                    toUrl(MessageType.CHECK_INBOX_COURT.name(), "462"),
                    "Juror not found: JurorNumber: 200000022"
                );
            }

            @Test
            void missingPlaceholderValue() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<trial_no>", "T100000002"
                            ))
                            .build()),
                    "Placeholder <sentence_date> must have a value",
                    MojException.BusinessRuleViolation.ErrorCode.PLACEHOLDER_MUST_HAVE_VALUE
                );
            }

            @Test
            void missingTrialPlaceholderValue() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<sentence_date>", "2023-01-01"
                            ))
                            .build()),
                    "Placeholder <trial_no> must have a value",
                    MojException.BusinessRuleViolation.ErrorCode.PLACEHOLDER_MUST_HAVE_VALUE
                );
            }

            @Test
            void badDataFormatDate() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<sentence_date>", "202301-02",
                                "<trial_no>", "T100000002"
                            ))
                            .build()),
                    "Invalid must be in format 'yyyy-MM-dd'",
                    MojException.BusinessRuleViolation.ErrorCode.INVALID_FORMAT
                );
            }

            @Test
            void badDataFormatTime() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.ATTENDANCE_TIME_CHANGED_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of(
                                "<attend_time>", "25:63a"
                            ))
                            .build()),
                    "Invalid must be in format 'HH:mm'",
                    MojException.BusinessRuleViolation.ErrorCode.INVALID_FORMAT
                );
            }

            @Test
            void jurorSendBySmsButNoPhone() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.EXCUSED_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber("100000016")
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.SMS)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()),
                    "Phone number is required for juror: 100000016",
                    JUROR_MUST_HAVE_PHONE_NUMBER
                );
            }

            @Test
            void jurorSendByEmailButNoEmail() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.EXCUSED_COURT.name(), "462", "462",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber("100000017")
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()),
                    "Email is required for juror: 100000017",
                    JUROR_MUST_HAVE_EMAIL
                );
            }

            @Test
            void unauthorisedNotMemberOfCourt() throws Exception {
                assertForbiddenResponse(triggerInvalid(
                    MessageType.SELECTION_COURT.name(),
                    "415",
                    "416",
                    MessageSendRequest.builder()
                        .jurors(List.of(
                            MessageSendRequest.JurorAndSendType.builder()
                                .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                .type(MessageType.SendType.EMAIL)
                                .build()
                        ))
                        .placeholderValues(Map.of())
                        .build()
                ), toUrl(MessageType.SELECTION_COURT.name(), "415"));
            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid(MessageType.SELECTION_COURT.name(),
                        "INVALID",
                        "INVALID",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()
                    ),
                    "sendMessage.locCode: size must be between 3 and 3"
                );
            }

            @Test
            void invalidMessageType() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid("INVALID",
                        "415",
                        "415",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber(ENGLISH_JUROR_NUMBER_WELSH_FLAG_FALSE)
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()
                    ),
                    "INVALID is the incorrect data type or is not in the expected format (message_type)"
                );
            }

            @Test
            void badPayload() throws Exception {
                assertInvalidPayload(
                    triggerInvalid(MessageType.SELECTION_COURT.name(), "415", "415",
                        MessageSendRequest.builder()
                            .jurors(List.of(
                                MessageSendRequest.JurorAndSendType.builder()
                                    .jurorNumber("Invalid")
                                    .poolNumber(ENGLISH_POOL_NUMBER_WELSH_FLAG_FALSE)
                                    .type(MessageType.SendType.EMAIL)
                                    .build()
                            ))
                            .placeholderValues(Map.of())
                            .build()
                    ),
                    new RestResponseEntityExceptionHandler.FieldError("jurors[0].jurorNumber",
                        "must match \"^\\d{9}$\"")
                );
            }

        }
    }


    @Nested
    @DisplayName("POST " + GetMessageDetailsPopulated.URL)
    @Sql({"/db/mod/truncate.sql", "/db/MessagingControllerITest_sendMessageSetup.sql"
    })
    class GetMessageDetailsPopulated {

        private static final String URL = BASE_URL + "/view/{message_type}/{loc_code}/populated";

        protected URI toUri(MessageType messageType, String locCode) {
            return toUri(messageType.name(), locCode);
        }

        protected URI toUri(String messageType, String locCode) {
            return URI.create(toUrl(messageType, locCode));
        }

        protected String toUrl(String messageType, String locCode) {
            return URL.replace("{message_type}", messageType)
                .replace("{loc_code}", locCode);
        }


        @Nested
        @DisplayName("Positive")
        class Positive {

            protected final List<DynamicContainer> tests;

            protected Positive() {
                this.tests = new ArrayList<>();
                addTests(
                    MessageType.REMIND_TO_ATTEND,
                    "Reminder: Please attend for your Jury Service at WARRINGTON Court on 01/01/2023 at "
                        + "05:00. If you have any questions, please contact the jury office on 01244 356726.",
                    "Reminder: Please attend for your Jury Service at CARMARTHEN Court on 01/01/2023 at "
                        + "05:00. If you have any questions, please contact the jury office on 01792 637000.",
                    "Nodyn Atgoffa: Cofiwch fynychu eich Gwasanaeth Rheithgor yn LlysCAERFYRDDIN ar 01/01/2023"
                        + " am 05:00. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy"
                        + " ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01",
                        "<attend_time>", "05:00"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.FAILED_TO_ATTEND_COURT,
                    "You failed to attend for Jury Service at WARRINGTON Court on 01/01/2023. Please contact "
                        + "the jury office on 01244 356726.",
                    "You failed to attend for Jury Service at CARMARTHEN Court on 01/01/2023. Please contact "
                        + "the jury office on 01792 637000.",
                    "Bu ichi fethu â mynychu eich Gwasanaeth Rheithgor yn LlysCAERFYRDDIN ar 01/01/2023. "
                        + "Cysylltwch â'r Swyddfa Rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.ATTENDANCE_DATE_AND_TIME_CHANGED_COURT,
                    "The date of your attendance for Jury Service has changed to 01/01/2023 at 05:00 at "
                        + "WARRINGTON Court. The days you are not required at court still form part of your jury "
                        + "service and will not be added on at the end. If you have any questions, please contact the"
                        + " jury office on 01244 356726.",
                    "The date of your attendance for Jury Service has changed to 01/01/2023 at 05:00 at "
                        + "CARMARTHEN Court. The days you are not required at court still form part of your jury "
                        + "service and will not be added on at the end. If you have any questions, please contact the"
                        + " jury office on 01792 637000.",
                    "Mae dyddiad mynychu eich Gwasanaeth Rheithgor wedi newid i 01/01/2023 am 05:00 yn "
                        + "LlysCAERFYRDDIN. Mae'r dyddiau pan na fydd eich angen yn y llys dal yn ffurfio rhan o'ch "
                        + "gwasanaeth rheithgor ac ni fyddant yn cael eu hychwanegu ar y diwedd. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01",
                        "<attend_time>", "05:00"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.ATTENDANCE_TIME_CHANGED_COURT,
                    "The time of your attendance for Jury Service has changed to 05:00. The date remains the same. "
                        + "If you have any questions, please contact the jury office on 01244 356726.",
                    "The time of your attendance for Jury Service has changed to 05:00. The date remains the same. "
                        + "If you have any questions, please contact the jury office on 01792 637000.",
                    "Mae amser mynychu eich Gwasanaeth Rheithgor wedi newid i 05:00. Nid yw'r dyddiad wedi newid. Os"
                        + " oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_time>", "05:00"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.COMPLETE_ATTENDED_COURT,
                    "You are not required to attend court any further and are formally discharged from jury service. "
                        + "If you have any questions, please contact the jury office on 01244 356726.",
                    "You are not required to attend court any further and are formally discharged from jury service. "
                        + "If you have any questions, please contact the jury office on 01792 637000.",
                    "Nid oes angen ichi fynychu'r Llys eto ac rydych nawr wedi cael eich rhyddhau'n ffurfiol o'ch "
                        + "Gwasanaeth Rheithgor. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor "
                        + "drwy "
                        + "ffonio 01792 637000.",
                    Map.of(),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.COMPLETE_NOT_NEEDED_COURT,
                    "You are no longer required to attend court and are formally discharged from jury service. If you"
                        + " have any questions, please contact the jury office on 01244 356726.",
                    "You are no longer required to attend court and are formally discharged from jury service. If you"
                        + " have any questions, please contact the jury office on 01792 637000.",
                    "Nid oes angen ichi fynychu'r Llys mwyach ac rydych nawr wedi cael eich rhyddhau'n ffurfiol o'ch "
                        + "Gwasanaeth Rheithgor. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor "
                        + "drwy ffonio 01792 637000.",
                    Map.of(),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.NEXT_ATTENDANCE_DATE_COURT,
                    "You are next required to attend for Jury Service on 03/01/2023 at 08:00 at WARRINGTON "
                        + "Court. If you have any questions, please contact the jury office on 01244 356726.",
                    "You are next required to attend for Jury Service on 03/01/2023 at 08:00 at CARMARTHEN "
                        + "Court. If you have any questions, please contact the jury office on 01792 637000.",
                    "Mae angen i chi fynychu'r llys eto ar gyfer eich Gwasanaeth Rheithgor ar 03/01/2023 "
                        + "am 08:00 yn LlysCAERFYRDDIN. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa "
                        + "rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-03",
                        "<attend_time>", "08:00"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.ON_CALL_COURT,
                    "You are not required to attend for Jury Service on 01/01/2023. We will be in touch by "
                        + "text/email with regards to your next attendance. If you have any questions, please contact"
                        + " the jury office on 01244 356726.",
                    "You are not required to attend for Jury Service on 01/01/2023. We will be in touch by "
                        + "text/email with regards to your next attendance. If you have any questions, please contact"
                        + " the jury office on 01792 637000.",
                    "Nid oes angen ichi fynychu'r Llys ar gyfer eich Gwasanaeth Rheithgor ar 01/01/2023. "
                        + "Byddwn yn cysylltu â chi drwy neges testun/e-bost ynghylch pryd fydd angen i chi fynychu'r "
                        + "Llys eto. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.PLEASE_CONTACT_COURT,
                    "Please contact WARRINGTON Jury Office on 01244 356726 with regard to your jury service.",
                    "Please contact CARMARTHEN Jury Office on 01792 637000 with regard to your jury service.",
                    "Cysylltwch â Swyddfa RheithgorCAERFYRDDIN drwy ffonio 01792 637000  ynghylch eich gwasanaeth "
                        + "rheithgor.",
                    Map.of(),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.DELAYED_START_COURT,
                    "You are due for Jury Service on 01/01/2023, but please do not attend the court until "
                        + "contacted by the Jury Team. Please attend work if you are able, you will receive sufficient "
                        + "notice when required to attend. If you have any questions, please contact the jury office "
                        + "on 01244 356726.",
                    "You are due for Jury Service on 01/01/2023, but please do not attend the court until "
                        + "contacted by the Jury Team. Please attend work if you are able, you will receive sufficient "
                        + "notice when required to attend. If you have any questions, please contact the jury office "
                        + "on 01792 637000.",
                    "Rydych i fod i ddechrau eich Gwasanaeth Rheithgor ar 01/01/2023, ond peidiwch â mynd i'r"
                        + " llys hyd nes bydd y Tîm Rheithgor yn cysylltu â chi. Ewch i'ch gwaith os allwch chi "
                        + "oherwydd "
                        + "byddwch yn cael digon o rybudd pan fydd angen ichi fynychu'r llys. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-01"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.SELECTION_COURT,
                    "You have been selected to form a panel from which a jury will be chosen. Please attend "
                        + "WARRINGTON Court on 02/01/2023 at 15:30. If you have any questions, please "
                        + "contact the jury office on 01244 356726.",
                    "You have been selected to form a panel from which a jury will be chosen. Please attend "
                        + "CARMARTHEN Court on 02/01/2023 at 15:30. If you have any questions, please "
                        + "contact the jury office on 01792 637000.",
                    "Rydych wedi cael eich dewis i fod yn rhan o banel a bydd Rheithgor yn cael ei ddewis o blith y "
                        + "panel hwnnw. Ewch i LysCAERFYRDDIN ar 02/01/2023 am 15:30. Os oes gennych unrhyw "
                        + "gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(
                        "<attend_date>", "2023-01-02",
                        "<attend_time>", "15:30"
                    ),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.BAD_WEATHER_COURT,
                    "Due to local adverse weather conditions, you are not required to attend the court until further "
                        + "notice. Please await further information. If you have any questions, please contact the "
                        + "jury office on 01244 356726.",
                    "Due to local adverse weather conditions, you are not required to attend the court until further "
                        + "notice. Please await further information. If you have any questions, please contact the "
                        + "jury office on 01792 637000.",
                    "Oherwydd amodau tywydd gwael yn lleol, nid oes angen ichi fynychu'r llys hyd nes y dywedir "
                        + "wrthych yn wahanol. Arhoswch am ragor o wybodaeth. Os oes gennych unrhyw gwestiynau, "
                        + "cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.CHECK_INBOX_COURT,
                    "If you've provided us with an email address, we'll use this to keep in touch. Please check your "
                        + "inbox regularly including your junk and spam mailbox.",
                    "If you've provided us with an email address, we'll use this to keep in touch. Please check your "
                        + "inbox regularly including your junk and spam mailbox.",
                    "Eich gwasanaeth rheithgor. Os ydych wedi rhoi eich cyfeiriad e-bost i ni, byddwn yn defnyddio "
                        + "hwn i gysylltu â chi. Gwiriwch eich mewnflwch yn rheolaidd gan gynnwys eich blwch junk a "
                        + "spam.",
                    Map.of(),
                    MessageType.SendType.SMS
                );
                addTests(
                    MessageType.BRING_LUNCH_COURT,
                    "Your jury panel is likely to go into deliberation tomorrow. Please bring a packed lunch to court"
                        + " as you will not be able to leave the deliberation room to get refreshments. Please do not "
                        + "bring metal cutlery or glass. If you have any questions, please contact the jury office on "
                        + "01244 356726.",
                    "Your jury panel is likely to go into deliberation tomorrow. Please bring a packed lunch to court"
                        + " as you will not be able to leave the deliberation room to get refreshments. Please do not "
                        + "bring metal cutlery or glass. If you have any questions, please contact the jury office on "
                        + "01792 637000.",
                    "Mae eich panel rheithgor yn debygol o gychwyn trafod yn yr ystafell ymneilltuo yfory. Dewch â "
                        + "phecyn bwyd gyda chi i'r llys oherwydd ni chaniateir i chi adael yr ystafell ymneilltuo i "
                        + "nôl bwyd. Peidiwch â dod â chyllell a fforc metel nac unrhyw eitemau gwydr gyda chi. Os oes "
                        + "gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio 01792 637000.",
                    Map.of(),
                    MessageType.SendType.EMAIL_AND_SMS
                );
                addTests(
                    MessageType.EXCUSED_COURT,
                    "You have been excused from attending jury service on this occasion and are no longer required to"
                        + " attend court. If you have any questions, please contact the jury office on 01244 356726.",
                    "You have been excused from attending jury service on this occasion and are no longer required to"
                        + " attend court. If you have any questions, please contact the jury office on 01792 637000.",
                    "Rydych wedi cael eich esgusodi o wasanaethu ar reithgor ac nid oes rhaid i chi fynd i'r llys "
                        + "mwyach. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio "
                        + "01792 637000.",
                    Map.of(),
                    MessageType.SendType.EMAIL_AND_SMS
                );

                addTests(
                    MessageType.SENTENCING_INVITE_COURT,
                    "The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.\n"
                        + "\nPlease contact the court if you wish to attend on 01244 356726.\n"
                        + "\nAlternatively contact the court after the hearing date quoting reference number "
                        + "T123456789 if you would like to know the sentence.\n"
                        + "\nPlease note that jurors do not have a role in sentencing, and as such your attendance is"
                        + " entirely voluntary and travel and subsistence payments cannot be claimed.\n"
                        + "\nPlease do not reply to this email as this mailbox is unmonitored.",

                    "The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.\n"
                        + "\nPlease contact the court if you wish to attend on 01792 637000.\n"
                        + "\nAlternatively contact the court after the hearing date quoting reference number "
                        + "T123456789 if you would like to know the sentence.\n"
                        + "\nPlease note that jurors do not have a role in sentencing, and as such your attendance is"
                        + " entirely voluntary and travel and subsistence payments cannot be claimed.\n"
                        + "\nPlease do not reply to this email as this mailbox is unmonitored.",
                    "Bydd y diffynnydd yn y treial yr oeddech yn rheithiwr ynddo yn cael ei ddedfrydu ar 02/01/2023.\n"
                        + "\nCysylltwch â'r llys os ydych yn dymuno bod yn bresennol drwy ffonio 01792 637000.\n"
                        + "\nFel arall, cysylltwch â'r llys ar ôl dyddiad y gwrandawiad, gan ddyfynnu'r cyfeirnod "
                        + "T123456789 os hoffech wybod beth oedd y ddedfryd.\n"
                        + "\nNoder, nid oes gan reithwyr rôl i'w chwarae wrth ddedfrydu diffynyddion ac felly mae "
                        + "eich presenoldeb yn gyfan gwbl wirfoddol ac ni allwch hawlio costau teithio a chynhaliaeth"
                        + ".\n"
                        + "\nPeidiwch ag ymateb i'r neges e-bost hon oherwydd nid yw'r mewnflwch hwn yn cael ei "
                        + "fonitro.",
                    Map.of("<sentence_date>", "2023-01-02",
                        "<trial_no>", "T123456789"),
                    MessageType.SendType.EMAIL
                );
                addTests(
                    MessageType.SENTENCING_DATE_COURT,
                    "The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.\n"
                        + "\nPlease contact the court after the hearing date on 01244 356726 quoting reference number"
                        + " T123456789 if you would like to know the sentence.\n"
                        + "\nPlease do not reply to this email as this mailbox is unmonitored.",
                    "The defendant in the trial in which you were a juror is being sentenced on 02/01/2023.\n"
                        + "\nPlease contact the court after the hearing date on 01792 637000 quoting reference number"
                        + " T123456789 if you would like to know the sentence.\n"
                        + "\nPlease do not reply to this email as this mailbox is unmonitored.",
                    "Bydd y diffynnydd yn y treial yr oeddech yn rheithiwr ynddo yn cael ei ddedfrydu ar 02/01/2023.\n"
                        + "\nCysylltwch â'r Llys ar ôl dyddiad y gwrandawiad drwy ffonio 01792 637000 a dyfynnu'r "
                        + "cyfeirnod T123456789 os hoffech wybod beth oedd y ddedfryd.\n"
                        + "\nPeidiwch ag ymateb i'r neges e-bost hon oherwydd nid yw'r mewnflwch hwn yn cael ei "
                        + "fonitro.",
                    Map.of("<sentence_date>", "2023-01-02",
                        "<trial_no>", "T123456789"),
                    MessageType.SendType.EMAIL
                );
            }

            private ResponseEntity<ViewMessageTemplateDto> triggerValid(
                MessageType messageType, String locCode, Map<String, String> placeholders) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

                ResponseEntity<ViewMessageTemplateDto> response = template.exchange(
                    new RequestEntity<>(placeholders, httpHeaders, POST, toUri(messageType, locCode)),
                    ViewMessageTemplateDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);

                assertThat(response.getBody()).isNotNull();
                return response;
            }

            @TestFactory
            @DisplayName("Generated")
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert"//False positive
            })
            Stream<DynamicContainer> tests() {
                return tests.stream();
            }


            private void addTests(MessageType messageType, String englishMessage,
                                  String welshEnglishMessage, String welshMessage,
                                  Map<String, String> placeholders,
                                  MessageType.SendType sendType) {
                List<DynamicTest> dynamicTests = new ArrayList<>();

                dynamicTests.add(
                    DynamicTest.dynamicTest("English Court",
                        () -> addTest(messageType,
                            TestData.ENGLISH_LOC_CODE,
                            englishMessage,
                            null,
                            sendType,
                            placeholders)
                    )
                );
                dynamicTests.add(
                    DynamicTest.dynamicTest("Welsh Court",
                        () -> addTest(messageType,
                            TestData.WELSH_LOC_CODE,
                            welshEnglishMessage,
                            welshMessage,
                            sendType,
                            placeholders)
                    )
                );

                this.tests.add(DynamicContainer.dynamicContainer(messageType.name(), dynamicTests));
            }

            private void addTest(MessageType messageType,
                                 String locCode,
                                 String englishMessage,
                                 String welshMessage,
                                 MessageType.SendType sendType,
                                 Map<String, String> placeholders) throws Exception {
                ResponseEntity<ViewMessageTemplateDto> response = triggerValid(messageType, locCode, placeholders);
                ViewMessageTemplateDto viewMessageTemplateDto = response.getBody();
                assertThat(viewMessageTemplateDto).isNotNull();

                assertThat(viewMessageTemplateDto.getMessageTemplateEnglish()).isEqualTo(englishMessage);
                assertThat(viewMessageTemplateDto.getMessageTemplateWelsh()).isEqualTo(welshMessage);
                assertThat(viewMessageTemplateDto.getSendType()).isEqualTo(sendType);
                assertThat(viewMessageTemplateDto.getPlaceholders()).isNull();
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(
                String messageType, String locCode, String court, Map<String, String> request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", court);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, toUri(messageType, locCode)), String.class);
            }


            @Test
            void missingPlaceholderValue() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        Map.of("<trial_no>", "T100000001")),
                    "Placeholder <sentence_date> must have a value",
                    MojException.BusinessRuleViolation.ErrorCode.PLACEHOLDER_MUST_HAVE_VALUE
                );
            }

            @Test
            void badDataFormatDate() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.SENTENCING_DATE_COURT.name(), "462", "462",
                        Map.of(
                            "<sentence_date>", "202301-02",
                            "<trial_no>", "T12345"
                        )),
                    "Invalid must be in format 'yyyy-MM-dd'",
                    MojException.BusinessRuleViolation.ErrorCode.INVALID_FORMAT
                );
            }

            @Test
            void badDataFormatTime() throws Exception {
                assertBusinessRuleViolation(
                    triggerInvalid(MessageType.ATTENDANCE_TIME_CHANGED_COURT.name(), "462", "462",
                        Map.of(
                            "<attend_time>", "25:63a"
                        )),
                    "Invalid must be in format 'HH:mm'",
                    MojException.BusinessRuleViolation.ErrorCode.INVALID_FORMAT
                );
            }

            @Test
            void unauthorisedNotMemberOfCourt() throws Exception {
                assertForbiddenResponse(triggerInvalid(
                    MessageType.SELECTION_COURT.name(),
                    "415", "416", Map.of()
                ), toUrl(MessageType.SELECTION_COURT.name(), "415"));
            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid(MessageType.SELECTION_COURT.name(),
                        "INVALID",
                        "INVALID",
                        Map.of()
                    ),
                    "getMessageDetailsPopulated.locCode: size must be between 3 and 3"
                );
            }

            @Test
            void invalidMessageType() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid("INVALID",
                        "415",
                        "415",
                        Map.of()
                    ),
                    "INVALID is the incorrect data type or is not in the expected format (message_type)"
                );
            }

            @Test
            void badPayload() throws Exception {
                Map<String, String> request = new HashMap<>();
                request.put("<trial_no>", null);
                assertInvalidPathParam(
                    triggerInvalid(MessageType.SELECTION_COURT.name(), "415", "415",
                        request
                    ),
                    "getMessageDetailsPopulated.placeholderValues[<trial_no>].<map value>: must not be null"
                );
            }
        }
    }
}
