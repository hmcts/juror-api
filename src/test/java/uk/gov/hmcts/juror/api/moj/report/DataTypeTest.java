package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.TooManyMethods"
})
class DataTypeTest {

    @Test
    void jurorNumber() {
        assertMatchesStandard(DataType.JUROR_NUMBER, "juror_number", "Juror Number", String.class,
            QJuror.juror.jurorNumber, QJuror.juror);
    }

    @Test
    void firstName() {
        assertMatchesStandard(DataType.FIRST_NAME, "first_name", "First Name", String.class,
            QJuror.juror.firstName, QJuror.juror);
    }

    @Test
    void lastName() {
        assertMatchesStandard(DataType.LAST_NAME, "last_name", "Last Name", String.class,
            QJuror.juror.lastName, QJuror.juror);
    }

    @Test
    void status() {
        assertMatchesStandard(DataType.STATUS, "status", "Status", String.class,
            QJurorPool.jurorPool.status.statusDesc, QJurorPool.jurorPool);
    }

    @Test
    void deferrals() {
        assertMatchesStandard(DataType.DEFERRALS, "deferrals", "Deferrals", String.class,
            QJuror.juror.noDefPos, QJuror.juror);
    }

    @Test
    void absences() {
        assertMatchesStandard(DataType.ABSENCES, "absences", "Absences", Long.class,
            QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT).count());
    }

    @Test
    void mainPhone() {
        assertMatchesStandard(DataType.MAIN_PHONE, "main_phone", "Main Phone", String.class,
            QJuror.juror.phoneNumber, QJuror.juror);
    }

    @Test
    void mobilePhone() {
        assertMatchesStandard(DataType.MOBILE_PHONE, "mobile_phone", "Mobile Phone", String.class,
            QJuror.juror.altPhoneNumber, QJuror.juror);
    }

    @Test
    void homePhone() {
        assertMatchesStandard(DataType.HOME_PHONE, "home_phone", "Home Phone", String.class,
            QJuror.juror.phoneNumber, QJuror.juror);
    }

    @Test
    void otherPhone() {
        assertMatchesStandard(DataType.OTHER_PHONE, "other_phone", "Other Phone", String.class,
            QJuror.juror.altPhoneNumber, QJuror.juror);
    }

    @Test
    void workPhone() {
        assertMatchesStandard(DataType.WORK_PHONE, "work_phone", "Work Phone", String.class,
            QJuror.juror.workPhone, QJuror.juror);
    }

    @Test
    void email() {
        assertMatchesStandard(DataType.EMAIL, "email", "Email", String.class,
            QJuror.juror.email, QJuror.juror);
    }

    @Test
    void contactDetails() {
        assertMatchesCombined(DataType.CONTACT_DETAILS, "contact_details", "Contact Details", List.class,
            DataType.MAIN_PHONE, DataType.OTHER_PHONE, DataType.WORK_PHONE, DataType.EMAIL);
    }

    @Test
    void warning() {
        assertMatchesStandard(DataType.WARNING, "warning", "Warning", String.class,
            new CaseBuilder()
                .when(QJuror.juror.policeCheck.isNull()
                    .or(QJuror.juror.policeCheck.notIn(PoliceCheck.ELIGIBLE, PoliceCheck.INELIGIBLE)))
                .then("Not police checked")
                .when(QJuror.juror.policeCheck.eq(PoliceCheck.INELIGIBLE)).then("Failed police check")
                .otherwise(""), QJuror.juror);
    }

    @Test
    void postcode() {
        assertMatchesStandard(DataType.POSTCODE, "postcode", "Postcode", String.class,
            QJuror.juror.postcode, QJuror.juror);
    }

    @Test
    void postponedTo() {
        assertMatchesStandard(DataType.POSTPONED_TO, "postponed_to", "Postcode", LocalDate.class,
            QJurorPool.jurorPool.deferralDate, QJuror.juror);
    }

    @Test
    void deferredTo() {
        assertMatchesStandard(DataType.DEFERRED_TO, "deferred_to", "Deferred To", LocalDate.class,
            QJurorPool.jurorPool.deferralDate, QJuror.juror);
    }

    @Test
    void numberDeferred() {
        assertMatchesStandard(DataType.NUMBER_DEFERRED, "number_deferred", "Number Deferred", Long.class,
            QJurorPool.jurorPool.count(), QJurorPool.jurorPool);
    }

    @Test
    void reasonableAdjustmentCode() {
        assertMatchesStandard(DataType.REASONABLE_ADJUSTMENT_CODE, "reasonable_adjustment_code",
            "Reasonable Adjustment Code", String.class,
            QJuror.juror.reasonableAdjustmentCode, QJuror.juror);
    }

    @Test
    void reasonableAdjustmentMessage() {
        assertMatchesStandard(DataType.REASONABLE_ADJUSTMENT_MESSAGE, "reasonable_adjustment_message",
            "Reasonable Adjustment Message", String.class,
            QJuror.juror.reasonableAdjustmentMessage, QJuror.juror);
    }

    @Test
    void reasonableAdjustment() {
        assertMatchesCombined(DataType.REASONABLE_ADJUSTMENT, "reasonable_adjustment", "Reasonable Adjustment",
            List.class,
            DataType.REASONABLE_ADJUSTMENT_CODE, DataType.REASONABLE_ADJUSTMENT_MESSAGE);
    }

    @Test
    void onCall() {
        assertMatchesStandard(DataType.ON_CALL, "on_call", "On Call", Boolean.class,
            QJurorPool.jurorPool.onCall, QJurorPool.jurorPool);
    }

    @Test
    void serviceNextAttendanceDate() {
        assertMatchesStandard(DataType.NEXT_ATTENDANCE_DATE, "next_attendance_date", "Next attendance date",
            LocalDate.class, QJurorPool.jurorPool.nextDate, QJurorPool.jurorPool);
    }

    @Test
    void serviceStartDate() {
        assertMatchesStandard(DataType.SERVICE_START_DATE, "service_start_date", "Service Start Date",
            LocalDate.class, QPoolRequest.poolRequest.returnDate, QPoolRequest.poolRequest);
    }

    @Test
    void poolNumber() {
        assertMatchesStandard(DataType.POOL_NUMBER, "pool_number", "Pool Number",
            String.class, QPoolRequest.poolRequest.poolNumber, QPoolRequest.poolRequest);
    }

    @Test
    void jurorAddress() {
        assertMatchesCombined(DataType.JUROR_ADDRESS, "juror_address", "Address", List.class,
            DataType.ADDRESS_LINE_1, DataType.ADDRESS_LINE_2, DataType.ADDRESS_LINE_3, DataType.ADDRESS_LINE_4,
            DataType.ADDRESS_LINE_5, DataType.POSTCODE);
    }

    void assertMatchesStandard(DataType dataType,
                               String id,
                               String displayName,
                               Class<?> type,
                               Expression<?> expression,
                               EntityPath<?>... entityPath) {
        assertThat(dataType.getDisplayName()).isEqualTo(displayName);
        assertThat(dataType.getDataType()).isEqualTo(type);
        assertThat(dataType.getExpression()).isEqualTo(expression);
        assertThat(dataType.getReturnTypes()).isNull();
        assertThat(dataType.getRequiredTables()).containsExactly(entityPath);
        assertThat(dataType.getId()).isEqualTo(id);
    }

    void assertMatchesCombined(DataType dataType,
                               String id,
                               String displayName,
                               Class<?> type,
                               DataType... nestedDataTypes) {
        assertThat(dataType.getDisplayName()).isEqualTo(displayName);
        assertThat(dataType.getDataType()).isEqualTo(type);
        assertThat(dataType.getReturnTypes()).containsExactly(nestedDataTypes);
        assertThat(dataType.getExpression()).isNull();
        assertThat(dataType.getRequiredTables()).isNull();
        assertThat(dataType.getId()).isEqualTo(id);

    }
}
