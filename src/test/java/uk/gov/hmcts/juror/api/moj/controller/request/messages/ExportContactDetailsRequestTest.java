package uk.gov.hmcts.juror.api.moj.controller.request.messages;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("PMD.TooManyMethods")
class ExportContactDetailsRequestTest extends AbstractValidatorTest<ExportContactDetailsRequest> {
    @Override
    protected ExportContactDetailsRequest createValidObject() {
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

    @Nested
    class ExportItemsListTest extends AbstractValidationFieldTestList<ExportContactDetailsRequest.ExportItems> {
        protected ExportItemsListTest() {
            super("exportItems", ExportContactDetailsRequest::setExportItems);
            addNotEmptyTest(null);
            addNullValueInListTest(null);
        }
    }

    @Nested
    class JurorsListTest extends AbstractValidationFieldTestList<JurorAndPoolRequest> {
        protected JurorsListTest() {
            super("jurors", ExportContactDetailsRequest::setJurors);
            addNotEmptyTest(null);
            addNullValueInListTest(null);
        }
    }

    @Nested
    class ExportItemsTest {


        <T> void assertValid(ExportContactDetailsRequest.ExportItems value,
                             String title, Expression<?> expression,
                             T tupleResponse,
                             String expectedResponse) {
            assertThat(value.getTitle()).isEqualTo(title);
            assertThat(value.getExpression()).isEqualTo(expression);

            Tuple tuple = mock(Tuple.class);
            doReturn(tupleResponse).when(tuple).get(expression);

            value.getAsStringFunction().apply(tuple);

            assertThat(value.getAsStringFunction().apply(tuple)).isEqualTo(expectedResponse);
        }

        @Test
        void positiveJurorNumber() {
            assertValid(ExportContactDetailsRequest.ExportItems.JUROR_NUMBER,
                "Juror Number", QJuror.juror.jurorNumber,
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_JUROR_NUMBER);
        }

        @Test
        void positiveTitle() {
            assertValid(ExportContactDetailsRequest.ExportItems.TITLE,
                "Title", QJuror.juror.title,
                TestConstants.VALID_TITLE, TestConstants.VALID_TITLE);
        }

        @Test
        void positiveFirstName() {
            assertValid(ExportContactDetailsRequest.ExportItems.FIRST_NAME,
                "First Name", QJuror.juror.firstName,
                "John", "John");
        }

        @Test
        void positiveLastName() {
            assertValid(ExportContactDetailsRequest.ExportItems.LAST_NAME,
                "Last Name", QJuror.juror.lastName,
                "Doe", "Doe");
        }

        @Test
        void positiveEmail() {
            assertValid(ExportContactDetailsRequest.ExportItems.EMAIL,
                "Email", QJuror.juror.email,
                TestConstants.VALID_EMAIL, TestConstants.VALID_EMAIL);
        }

        @Test
        void positiveMainPhone() {
            assertValid(ExportContactDetailsRequest.ExportItems.MAIN_PHONE,
                "Main Phone", QJuror.juror.phoneNumber,
                TestConstants.VALID_PHONE_NUMBER, TestConstants.VALID_PHONE_NUMBER);
        }

        @Test
        void positiveOtherPhone() {
            assertValid(ExportContactDetailsRequest.ExportItems.OTHER_PHONE,
                "Other Phone", QJuror.juror.altPhoneNumber,
                TestConstants.VALID_PHONE_NUMBER, TestConstants.VALID_PHONE_NUMBER);
        }

        @Test
        void positiveWorkPhone() {
            assertValid(ExportContactDetailsRequest.ExportItems.WORK_PHONE,
                "Work Phone", QJuror.juror.workPhone,
                TestConstants.VALID_PHONE_NUMBER, TestConstants.VALID_PHONE_NUMBER);
        }

        @Test
        void positiveAddressLine1() {
            assertValid(ExportContactDetailsRequest.ExportItems.ADDRESS_LINE_1,
                "Address Line 1", QJuror.juror.addressLine1,
                "1 Some Street", "1 Some Street");
        }

        @Test
        void positiveAddressLine2() {
            assertValid(ExportContactDetailsRequest.ExportItems.ADDRESS_LINE_2,
                "Address Line 2", QJuror.juror.addressLine2,
                "Some Area", "Some Area");
        }

        @Test
        void positiveAddressLine3() {
            assertValid(ExportContactDetailsRequest.ExportItems.ADDRESS_LINE_3,
                "Address Line 3", QJuror.juror.addressLine3,
                "Some City", "Some City");
        }

        @Test
        void positiveAddressLine4() {
            assertValid(ExportContactDetailsRequest.ExportItems.ADDRESS_LINE_4,
                "Address Line 4", QJuror.juror.addressLine4,
                "Some County", "Some County");
        }

        @Test
        void positiveAddressLine5() {
            assertValid(ExportContactDetailsRequest.ExportItems.ADDRESS_LINE_5,
                "Address Line 5", QJuror.juror.addressLine5,
                "Some Country", "Some Country");
        }

        @Test
        void positivePostcode() {
            assertValid(ExportContactDetailsRequest.ExportItems.POSTCODE,
                "Postcode", QJuror.juror.postcode,
                TestConstants.VALID_POSTCODE, TestConstants.VALID_POSTCODE);
        }

        @Test
        void positiveWelshLanguage() {
            assertValid(ExportContactDetailsRequest.ExportItems.WELSH_LANGUAGE,
                "Welsh language", QJuror.juror.welsh,
                true, "true");
        }

        @Test
        void positiveStatus() {
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(1);
            jurorStatus.setStatusDesc("Summoned");

            assertValid(ExportContactDetailsRequest.ExportItems.STATUS,
                "Status", QJurorPool.jurorPool.status,
                jurorStatus, "Summoned");
        }

        @Test
        void positiveStatusNull() {
            assertValid(ExportContactDetailsRequest.ExportItems.STATUS,
                "Status", QJurorPool.jurorPool.status,
                null, "");
        }

        @Test
        void positivePoolNumber() {
            assertValid(ExportContactDetailsRequest.ExportItems.POOL_NUMBER,
                "Pool Number", QJurorPool.jurorPool.pool.poolNumber,
                TestConstants.VALID_POOL_NUMBER, TestConstants.VALID_POOL_NUMBER);
        }

        @Test
        void positiveNextDueAtCourtDate() {
            assertValid(ExportContactDetailsRequest.ExportItems.NEXT_DUE_AT_COURT_DATE,
                "Next due at court date", QJurorPool.jurorPool.nextDate,
                LocalDate.of(2021, 1, 1), "01/01/2021");
        }

        @Test
        void positiveDateDeferredTo() {
            assertValid(ExportContactDetailsRequest.ExportItems.DATE_DEFERRED_TO,
                "Date deferred to", QJurorPool.jurorPool.deferralDate,
                LocalDate.of(2021, 1, 1), "01/01/2021");
        }

        @Test
        void positiveDateDeferredToNull() {
            assertValid(ExportContactDetailsRequest.ExportItems.DATE_DEFERRED_TO,
                "Date deferred to", QJurorPool.jurorPool.deferralDate,
                null, "");
        }

        @Test
        void positiveCompletionDate() {
            assertValid(ExportContactDetailsRequest.ExportItems.COMPLETION_DATE,
                "Completion date", QJuror.juror.completionDate,
                LocalDate.of(2021, 1, 1), "01/01/2021");
        }

        @Test
        void positiveCompletionDateNull() {
            assertValid(ExportContactDetailsRequest.ExportItems.COMPLETION_DATE,
                "Completion date", QJuror.juror.completionDate,
                null, "");
        }

        @Test
        @SuppressWarnings("unchecked")
        void positiveAsString() {
            ExportContactDetailsRequest.ExportItems exportItems = mock(ExportContactDetailsRequest.ExportItems.class);
            Function<Tuple, String> asStringFunction = mock(Function.class);
            doReturn(asStringFunction).when(exportItems).getAsStringFunction();
            doCallRealMethod().when(exportItems).getAsString(any());

            Tuple tuple = mock(Tuple.class);
            String val = TestConstants.VALID_JUROR_NUMBER;
            doReturn(val).when(asStringFunction).apply(tuple);

            assertThat(exportItems.getAsString(tuple)).isEqualTo(TestConstants.VALID_JUROR_NUMBER);

            verify(asStringFunction).apply(tuple);
        }
    }
}
