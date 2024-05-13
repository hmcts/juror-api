package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
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
    void postponedTo() {
        assertMatchesStandard(DataType.POSTPONED_TO, "postponed_to", "Postponed to", LocalDate.class,
            QJurorPool.jurorPool.deferralDate, QJuror.juror);
    }

    @Test
    void deferredTo() {
        assertMatchesStandard(DataType.DEFERRED_TO, "deferred_to", "Deferred to", LocalDate.class,
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
            QReasonableAdjustments.reasonableAdjustments.code, QReasonableAdjustments.reasonableAdjustments);
    }

    @Test
    void reasonableAdjustmentCodeWithDescription() {
        assertMatchesStandard(DataType.REASONABLE_ADJUSTMENT_CODE_WITH_DESCRIPTION, "reasonable_adjustment_code_with_description",
            "Reasonable Adjustment Code With Description", String.class,
            QReasonableAdjustments.reasonableAdjustments.code
              .concat(" - ")
              .concat(QReasonableAdjustments.reasonableAdjustments.description), QReasonableAdjustments.reasonableAdjustments);
    }

    @Test
    void jurorReasonableAdjustmentMessage() {
        assertMatchesStandard(DataType.JUROR_REASONABLE_ADJUSTMENT_MESSAGE, "juror_reasonable_adjustment_message",
            "Juror Reasonable Adjustment Message", String.class, QJuror.juror.reasonableAdjustmentMessage,
            QJuror.juror);
    }

    @Test
    void jurorReasonableAdjustmentWithMessage() {
        assertMatchesCombined(DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE, "juror_reasonable_adjustment_with_message",
            "Reasonable Adjustments", List.class, DataType.REASONABLE_ADJUSTMENT_CODE_WITH_DESCRIPTION,
            DataType.JUROR_REASONABLE_ADJUSTMENT_MESSAGE);
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
    void jurorAddressLine1() {
        assertMatchesStandard(DataType.JUROR_ADDRESS_LINE_1, "juror_address_line_1", "Address Line 1",
            String.class, QJuror.juror.addressLine1, QJuror.juror);
    }

    @Test
    void jurorAddressLine2() {
        assertMatchesStandard(DataType.JUROR_ADDRESS_LINE_2, "juror_address_line_2", "Address Line 2",
            String.class, QJuror.juror.addressLine2, QJuror.juror);
    }

    @Test
    void jurorAddressLine3() {
        assertMatchesStandard(DataType.JUROR_ADDRESS_LINE_3, "juror_address_line_3", "Address Line 3",
            String.class, QJuror.juror.addressLine3, QJuror.juror);
    }

    @Test
    void jurorAddressLine4() {
        assertMatchesStandard(DataType.JUROR_ADDRESS_LINE_4, "juror_address_line_4", "Address Line 4",
            String.class, QJuror.juror.addressLine4, QJuror.juror);
    }

    @Test
    void jurorAddressLine5() {
        assertMatchesStandard(DataType.JUROR_ADDRESS_LINE_5, "juror_address_line_5", "Address Line 5",
            String.class, QJuror.juror.addressLine5, QJuror.juror);
    }

    @Test
    void jurorPostcode() {
        assertMatchesStandard(DataType.JUROR_POSTCODE, "juror_postcode", "Postcode", String.class,
            QJuror.juror.postcode, QJuror.juror);
    }

    @Test
    void jurorPostalAddress() {
        assertMatchesCombined(DataType.JUROR_POSTAL_ADDRESS, "juror_postal_address", "Address",
            List.class, DataType.JUROR_ADDRESS_LINE_1, DataType.JUROR_ADDRESS_LINE_2, DataType.JUROR_ADDRESS_LINE_3,
            DataType.JUROR_ADDRESS_LINE_4, DataType.JUROR_ADDRESS_LINE_5, DataType.JUROR_POSTCODE);
    }

    @Test
    void courtLocationNameAndCode() {
        assertMatchesStandard(DataType.COURT_LOCATION_NAME_AND_CODE, "court_location_name_and_code",
            "Court Location Name And Code", String.class,
            QCourtLocation.courtLocation.name.concat(" (")
                .concat(QCourtLocation.courtLocation.locCode).concat(")"), QPoolRequest.poolRequest);
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
