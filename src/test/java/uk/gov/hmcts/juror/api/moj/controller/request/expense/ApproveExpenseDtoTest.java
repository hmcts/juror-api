package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.LawOfDemeter")
public class ApproveExpenseDtoTest extends AbstractValidatorTest<ApproveExpenseDto> {
    @Override
    protected ApproveExpenseDto createValidObject() {
        return ApproveExpenseDto.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
            .dateToRevisions(
                List.of(
                    DateToRevisionTest.getValidObject()
                )
            )
            .build();
    }

    @Nested
    class JurorNumberTest extends AbstractValidationFieldTestString {

        protected JurorNumberTest() {
            super("jurorNumber", ApproveExpenseDto::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("INVALID", "^\\d{9}$", null);
        }
    }


    @Nested
    class ApprovalTypeTest extends AbstractValidationFieldTestBase<ApproveExpenseDto.ApprovalType> {
        protected ApprovalTypeTest() {
            super("approvalType", ApproveExpenseDto::setApprovalType);
            addRequiredTest(null);
        }
    }

    @Nested
    class DateToRevisionsTest extends AbstractValidationFieldTestList<ApproveExpenseDto.DateToRevision> {
        protected DateToRevisionsTest() {
            super("dateToRevisions", ApproveExpenseDto::setDateToRevisions);
            addNotEmptyTest(null);
            addNullValueInListTest(null);
        }
    }

    @Nested
    class DateToRevisionTest extends AbstractValidatorTest<ApproveExpenseDto.DateToRevision> {
        protected static ApproveExpenseDto.DateToRevision getValidObject() {
            return ApproveExpenseDto.DateToRevision.builder()
                .attendanceDate(LocalDate.of(2023, 1, 14))
                .version(1L)
                .build();
        }

        @Override
        protected ApproveExpenseDto.DateToRevision createValidObject() {
            return getValidObject();
        }

        @Nested
        class VersionTest extends AbstractValidationFieldTestLong {
            protected VersionTest() {
                super("version", ApproveExpenseDto.DateToRevision::setVersion);
                addRequiredTest(null);
            }
        }

        @Nested
        class AttendanceDateTest extends AbstractValidationFieldTestLocalDate {
            protected AttendanceDateTest() {
                super("attendanceDate", ApproveExpenseDto.DateToRevision::setAttendanceDate);
                addRequiredTest(null);
            }
        }
    }


    @Nested
    class ApprovalTypeEnumTest {

        @Test
        void positiveForApprovalIsApplicableTrue() {
            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
            appearance.setDraftExpense(false);
            assertThat(ApproveExpenseDto.ApprovalType.FOR_APPROVAL.isApplicable(appearance)).isTrue();
        }

        @Test
        void positiveForApprovalIsApplicableFalseIsDraft() {
            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
            appearance.setDraftExpense(true);
            assertThat(ApproveExpenseDto.ApprovalType.FOR_APPROVAL.isApplicable(appearance)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.EXCLUDE, names = {
            "EXPENSE_ENTERED"})
        void positiveForApprovalIsApplicableFalseWrongStage(AppearanceStage stage) {
            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(stage);
            appearance.setDraftExpense(false);
            assertThat(ApproveExpenseDto.ApprovalType.FOR_APPROVAL.isApplicable(appearance)).isFalse();
        }

        @Test
        void positiveForReApprovalIsApplicableTrue() {
            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(AppearanceStage.EXPENSE_EDITED);
            appearance.setDraftExpense(false);
            assertThat(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL.isApplicable(appearance)).isTrue();
        }

        @Test
        void positiveForReApprovalIsApplicableFalseIsDraft() {
            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(AppearanceStage.EXPENSE_EDITED);
            appearance.setDraftExpense(true);
            assertThat(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL.isApplicable(appearance)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.EXCLUDE, names = {
            "EXPENSE_EDITED"})
        void positiveForReApprovalIsApplicableFalseWrongStage(AppearanceStage stage) {
            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(stage);
            appearance.setDraftExpense(false);
            assertThat(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL.isApplicable(appearance)).isFalse();
        }
    }
}
