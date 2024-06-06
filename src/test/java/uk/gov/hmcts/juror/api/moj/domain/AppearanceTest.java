package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("PMD.TooManyMethods")
class AppearanceTest {

    @Test
    void givenBuilderWithDefaultValueThanDefaultValueIsPresent() {
        Appearance appearance = Appearance.builder().build();

        assertEquals(Boolean.FALSE, appearance.getNonAttendanceDay(), "Default value should be present with"
            + " builder");
    }

    @Test
    void givenBuilderWithDefaultValueThanNoArgsConstructorWorks() {
        Appearance appearance = new Appearance();
        assertEquals(Boolean.FALSE, appearance.getNonAttendanceDay(), "Default value should be present with "
            + "NoArgsConstructor");
    }


    private Appearance getAppearanceForTotals() {
        return Appearance.builder()
            .publicTransportDue(new BigDecimal("1.00"))
            .hiredVehicleDue(new BigDecimal("10.00"))
            .motorcycleDue(new BigDecimal("100.00"))
            .carDue(new BigDecimal("1000.00"))
            .bicycleDue(new BigDecimal("10000.00"))
            .parkingDue(new BigDecimal("100000.00"))
            .subsistenceDue(new BigDecimal("1000000.00"))
            .lossOfEarningsDue(new BigDecimal("10000000.00"))
            .childcareDue(new BigDecimal("100000000.00"))
            .miscAmountDue(new BigDecimal("1000000000.11"))
            .smartCardAmountDue(new BigDecimal("1.10"))


            .publicTransportPaid(new BigDecimal("2.00"))
            .hiredVehiclePaid(new BigDecimal("20.00"))
            .motorcyclePaid(new BigDecimal("200.00"))
            .carPaid(new BigDecimal("2000.00"))
            .bicyclePaid(new BigDecimal("20000.00"))
            .parkingPaid(new BigDecimal("200000.00"))
            .subsistencePaid(new BigDecimal("2000000.00"))
            .lossOfEarningsPaid(new BigDecimal("20000000.00"))
            .childcarePaid(new BigDecimal("200000000.00"))
            .miscAmountPaid(new BigDecimal("2000000000.22"))

            .smartCardAmountPaid(new BigDecimal("11.11"))
            .build();
    }

    private Appearance getAppearanceForTotalsNulls() {
        return Appearance.builder()
            .publicTransportDue(null)
            .hiredVehicleDue(null)
            .motorcycleDue(null)
            .carDue(null)
            .bicycleDue(null)
            .parkingDue(null)
            .subsistenceDue(null)
            .lossOfEarningsDue(null)
            .childcareDue(null)
            .miscAmountDue(null)
            .smartCardAmountDue(null)
            .publicTransportPaid(null)
            .hiredVehiclePaid(null)
            .motorcyclePaid(null)
            .carPaid(null)
            .bicyclePaid(null)
            .parkingPaid(null)
            .subsistencePaid(null)
            .lossOfEarningsPaid(null)
            .childcarePaid(null)
            .miscAmountPaid(null)
            .smartCardAmountPaid(null)
            .build();
    }

    @DisplayName("getTotalDue")
    @Nested
    class GetTotalDue {
        @Test
        void positiveTypical() {
            assertEquals(new BigDecimal("1111111110.01"),
                getAppearanceForTotals().getTotalDue(),
                "Total due should sum all the due amounts");
        }

        @Test
        void positiveAllNull() {
            assertEquals(BigDecimal.ZERO,
                getAppearanceForTotalsNulls().getTotalDue(),
                "Total due should be 0 if all the due amounts are null");
        }
    }

    @DisplayName("getTotalPaid")
    @Nested
    class GetTotalPaid {
        @Test
        void positiveTypical() {
            assertEquals(new BigDecimal("2222222211.11"),
                getAppearanceForTotals().getTotalPaid(),
                "Total paid should sum all the due amounts");
        }

        @Test
        void positiveAllNull() {
            assertEquals(BigDecimal.ZERO,
                getAppearanceForTotalsNulls().getTotalPaid(),
                "Total paid should be 0 if all the due amounts are null");
        }
    }

    @DisplayName("public LocalTime getTimeSpentAtCourt()")
    @Nested
    class GetTimeSpentAtCourt {
        @Test
        void timeOutNull() {
            Appearance appearance = new Appearance();
            appearance.setTimeIn(LocalTime.of(8, 30));
            appearance.setTimeOut(null);
            assertThat(appearance.getTimeSpentAtCourt())
                .isEqualTo(LocalTime.of(0, 0));
        }

        @Test
        void timeInNull() {
            Appearance appearance = new Appearance();
            appearance.setTimeIn(null);
            appearance.setTimeOut(LocalTime.of(17, 30));
            assertThat(appearance.getTimeSpentAtCourt())
                .isEqualTo(LocalTime.of(0, 0));

        }

        @Test
        void timeOutAndTimeInNull() {
            Appearance appearance = new Appearance();
            appearance.setTimeIn(null);
            appearance.setTimeOut(null);
            assertThat(appearance.getTimeSpentAtCourt())
                .isEqualTo(LocalTime.of(0, 0));

        }

        @Test
        void timeOutAndTimeInNotNull() {
            Appearance appearance = new Appearance();
            appearance.setTimeIn(LocalTime.of(8, 30));
            appearance.setTimeOut(LocalTime.of(17, 30));
            assertThat(appearance.getTimeSpentAtCourt())
                .isEqualTo(LocalTime.of(9, 0));

        }
    }

    @Test
    void isLongTrialDayTrue() {
        Appearance appearance = new Appearance();
        appearance.setAttendanceType(AttendanceType.FULL_DAY_LONG_TRIAL);
        assertThat(appearance.isLongTrialDay()).isEqualTo(true);
    }

    @Test
    void isLongTrialDayFalse() {
        Appearance appearance = new Appearance();
        appearance.setAttendanceType(AttendanceType.FULL_DAY);
        assertThat(appearance.isLongTrialDay()).isEqualTo(false);
    }

    @Test
    void isLongTrialDayNull() {
        Appearance appearance = new Appearance();
        appearance.setAttendanceType(AttendanceType.ABSENT);
        assertThat(appearance.isLongTrialDay()).isNull();
    }

    @Test
    void positiveGetTotalChanged() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("3.01")).when(appearance).getTotalDue();
        doReturn(new BigDecimal("1.00")).when(appearance).getTotalPaid();
        assertThat(appearance.getTotalChanged())
            .isEqualTo(new BigDecimal("2.01"));
    }

    @Test
    void positiveGetFinancialLossTotal() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("3.00")).when(appearance).getLossOfEarningsDue();
        doReturn(new BigDecimal("1.00")).when(appearance).getLossOfEarningsPaid();


        doReturn(new BigDecimal("30.00")).when(appearance).getChildcareDue();
        doReturn(new BigDecimal("10.00")).when(appearance).getChildcarePaid();


        doReturn(new BigDecimal("300.00")).when(appearance).getMiscAmountDue();
        doReturn(new BigDecimal("100.00")).when(appearance).getMiscAmountPaid();
        assertThat(appearance.getTotalChanged())
            .isEqualTo(new BigDecimal("222.00"));
    }

    @Test
    void positiveGetBalanceToPay() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("100.00")).when(appearance).getTotalDue();
        doReturn(new BigDecimal("95.20")).when(appearance).getTotalPaid();
        assertThat(appearance.getBalanceToPay()).isEqualTo(
            new BigDecimal("4.80"));
    }

    @Test
    void positiveGetTotalFinancialLossDue() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("1.00")).when(appearance).getLossOfEarningsDue();
        doReturn(new BigDecimal("10.00")).when(appearance).getChildcareDue();
        doReturn(new BigDecimal("100.00")).when(appearance).getMiscAmountDue();
        assertThat(appearance.getTotalFinancialLossDue()).isEqualTo(
            new BigDecimal("111.00"));
    }

    @Test
    void positiveGetTotalFinancialLossDueNullValues() {
        Appearance appearance = spy(new Appearance());
        doReturn(null).when(appearance).getLossOfEarningsDue();
        doReturn(null).when(appearance).getChildcareDue();
        doReturn(null).when(appearance).getMiscAmountDue();
        assertThat(appearance.getTotalFinancialLossDue()).isZero();

    }

    @Test
    void positiveGetTotalTravelDue() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("1.00")).when(appearance).getCarDue();
        doReturn(new BigDecimal("10.00")).when(appearance).getMotorcycleDue();
        doReturn(new BigDecimal("100.00")).when(appearance).getBicycleDue();
        doReturn(new BigDecimal("1000.00")).when(appearance).getParkingDue();
        doReturn(new BigDecimal("10000.00")).when(appearance).getPublicTransportDue();
        doReturn(new BigDecimal("100000.00")).when(appearance).getHiredVehicleDue();
        assertThat(appearance.getTotalTravelDue()).isEqualTo(
            new BigDecimal("111111.00"));
    }

    @Test
    void positiveGetTotalTravelDueNullValues() {
        Appearance appearance = spy(new Appearance());
        doReturn(null).when(appearance).getCarDue();
        doReturn(null).when(appearance).getMotorcycleDue();
        doReturn(null).when(appearance).getBicycleDue();
        doReturn(null).when(appearance).getParkingDue();
        doReturn(null).when(appearance).getPublicTransportDue();
        doReturn(null).when(appearance).getHiredVehicleDue();
        assertThat(appearance.getTotalTravelDue()).isZero();
    }

    @Test
    void positiveGetTotalFinancialLossPaid() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("1.00")).when(appearance).getLossOfEarningsPaid();
        doReturn(new BigDecimal("10.00")).when(appearance).getChildcarePaid();
        doReturn(new BigDecimal("100.00")).when(appearance).getMiscAmountPaid();
        assertThat(appearance.getTotalFinancialLossPaid()).isEqualTo(
            new BigDecimal("111.00"));
    }

    @Test
    void positiveGetTotalFinancialLossPaidNullValues() {
        Appearance appearance = spy(new Appearance());
        doReturn(null).when(appearance).getLossOfEarningsPaid();
        doReturn(null).when(appearance).getChildcarePaid();
        doReturn(null).when(appearance).getMiscAmountPaid();
        assertThat(appearance.getTotalFinancialLossPaid()).isZero();

    }

    @Test
    void positiveGetTotalTravelPaid() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("1.00")).when(appearance).getCarPaid();
        doReturn(new BigDecimal("10.00")).when(appearance).getMotorcyclePaid();
        doReturn(new BigDecimal("100.00")).when(appearance).getBicyclePaid();
        doReturn(new BigDecimal("1000.00")).when(appearance).getParkingPaid();
        doReturn(new BigDecimal("10000.00")).when(appearance).getPublicTransportPaid();
        doReturn(new BigDecimal("100000.00")).when(appearance).getHiredVehiclePaid();
        assertThat(appearance.getTotalTravelPaid()).isEqualTo(
            new BigDecimal("111111.00"));
    }

    @Test
    void positiveGetTotalTravelPaidNullValues() {
        Appearance appearance = spy(new Appearance());
        doReturn(null).when(appearance).getCarPaid();
        doReturn(null).when(appearance).getMotorcyclePaid();
        doReturn(null).when(appearance).getBicyclePaid();
        doReturn(null).when(appearance).getParkingPaid();
        doReturn(null).when(appearance).getPublicTransportPaid();
        doReturn(null).when(appearance).getHiredVehiclePaid();
        assertThat(appearance.getTotalTravelDue()).isZero();
    }


    @Test
    void positiveGetSubsistenceTotal() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("300.00")).when(appearance).getSubsistenceDue();
        doReturn(new BigDecimal("100.00")).when(appearance).getSubsistencePaid();

        doReturn(new BigDecimal("10.00")).when(appearance).getSmartCardAmountDue();
        doReturn(new BigDecimal("30.00")).when(appearance).getSmartCardAmountPaid();

        assertThat(appearance.getTotalChanged())
            .isEqualTo(new BigDecimal("220.00"));
    }

    @Test
    void positiveGetTravelTotal() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("3.00")).when(appearance).getCarDue();
        doReturn(new BigDecimal("1.00")).when(appearance).getCarPaid();

        doReturn(new BigDecimal("30.00")).when(appearance).getMotorcycleDue();
        doReturn(new BigDecimal("10.00")).when(appearance).getMotorcyclePaid();

        doReturn(new BigDecimal("300.00")).when(appearance).getBicycleDue();
        doReturn(new BigDecimal("100.00")).when(appearance).getBicyclePaid();

        doReturn(new BigDecimal("3000.00")).when(appearance).getParkingDue();
        doReturn(new BigDecimal("1000.00")).when(appearance).getParkingPaid();

        doReturn(new BigDecimal("30000.00")).when(appearance).getPublicTransportDue();
        doReturn(new BigDecimal("10000.00")).when(appearance).getPublicTransportPaid();

        doReturn(new BigDecimal("300000.00")).when(appearance).getHiredVehicleDue();
        doReturn(new BigDecimal("100000.00")).when(appearance).getHiredVehiclePaid();

        assertThat(appearance.getTotalChanged())
            .isEqualTo(new BigDecimal("222222.00"));
    }

    @Test
    void positiveGetSubsistenceTotalDue() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("10.00")).when(appearance).getSubsistenceDue();
        assertThat(appearance.getSubsistenceTotalDue()).isEqualTo(
            new BigDecimal("10.00"));
    }

    @Test
    void positiveGetSubsistenceTotalDueNullValues() {
        Appearance appearance = spy(new Appearance());
        doReturn(null).when(appearance).getSubsistenceDue();
        doReturn(null).when(appearance).getSmartCardAmountDue();
        assertThat(appearance.getSubsistenceTotalDue()).isZero();
    }

    @Test
    void positiveGetSubsistenceTotalPaid() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("10.00")).when(appearance).getSubsistencePaid();
        assertThat(appearance.getSubsistenceTotalPaid()).isEqualTo(
            new BigDecimal("10.00"));
    }

    @Test
    void positiveGetSubsistenceTotalPaidNullValues() {
        Appearance appearance = spy(new Appearance());
        doReturn(null).when(appearance).getSubsistencePaid();
        doReturn(null).when(appearance).getSmartCardAmountPaid();
        assertThat(appearance.getSubsistenceTotalPaid()).isZero();
    }

    @Test
    void positiveGetSubsistenceTotalChanged() {
        Appearance appearance = spy(new Appearance());
        doReturn(new BigDecimal("20.00")).when(appearance).getSubsistenceDue();
        doReturn(new BigDecimal("10.00")).when(appearance).getSubsistencePaid();

        assertThat(appearance.getSubsistenceTotalChanged())
            .isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void positiveGetSmartCardTotalChangedDraft() {
        Appearance appearance = spy(new Appearance());
        doReturn(AppearanceStage.EXPENSE_ENTERED).when(appearance).getAppearanceStage();
        doReturn(new BigDecimal("20.00")).when(appearance).getSmartCardAmountDue();
        doReturn(new BigDecimal("0.00")).when(appearance).getSmartCardAmountPaid();

        assertThat(appearance.getSmartCardTotalChanged())
            .isEqualTo(new BigDecimal("20.00"));
    }

    @Test
    void positiveGetSmartCardTotalChangedApproved() {
        Appearance appearance = spy(new Appearance());
        doReturn(AppearanceStage.EXPENSE_AUTHORISED).when(appearance).getAppearanceStage();
        doReturn(new BigDecimal("15.00")).when(appearance).getSmartCardAmountDue();
        doReturn(new BigDecimal("20.00")).when(appearance).getSmartCardAmountPaid();

        assertThat(appearance.getSmartCardTotalChanged())
            .isEqualTo(new BigDecimal("-5.00"));
    }

    @Nested
    @DisplayName("public Map<String, Object> getExpensesWhereDueIsLessThenPaid()")
    class GetExpensesWhereDueIsLessThenPaid {
        private Appearance mockAppearance(AppearanceStage stage) {
            Appearance appearance = spy(new Appearance());

            doReturn(new BigDecimal("0.01")).when(appearance).getPublicTransportDue();
            doReturn(new BigDecimal("1.01")).when(appearance).getPublicTransportPaid();
            doReturn(new BigDecimal("0.02")).when(appearance).getHiredVehicleDue();
            doReturn(new BigDecimal("1.02")).when(appearance).getHiredVehiclePaid();
            doReturn(new BigDecimal("0.03")).when(appearance).getMotorcycleDue();
            doReturn(new BigDecimal("1.03")).when(appearance).getMotorcyclePaid();
            doReturn(new BigDecimal("0.04")).when(appearance).getCarDue();
            doReturn(new BigDecimal("1.04")).when(appearance).getCarPaid();
            doReturn(new BigDecimal("0.05")).when(appearance).getBicycleDue();
            doReturn(new BigDecimal("1.05")).when(appearance).getBicyclePaid();
            doReturn(new BigDecimal("0.06")).when(appearance).getParkingDue();
            doReturn(new BigDecimal("1.06")).when(appearance).getParkingPaid();
            doReturn(new BigDecimal("0.07")).when(appearance).getSubsistenceDue();
            doReturn(new BigDecimal("1.07")).when(appearance).getSubsistencePaid();
            doReturn(new BigDecimal("0.08")).when(appearance).getLossOfEarningsDue();
            doReturn(new BigDecimal("1.09")).when(appearance).getLossOfEarningsPaid();
            doReturn(new BigDecimal("0.10")).when(appearance).getChildcareDue();
            doReturn(new BigDecimal("1.10")).when(appearance).getChildcarePaid();
            doReturn(new BigDecimal("0.11")).when(appearance).getMiscAmountDue();
            doReturn(new BigDecimal("1.11")).when(appearance).getMiscAmountPaid();
            doReturn(new BigDecimal("0.12")).when(appearance).getTotalDue();
            doReturn(new BigDecimal("1.12")).when(appearance).getTotalPaid();
            doReturn(stage).when(appearance).getAppearanceStage();
            doReturn(new BigDecimal("1.13")).when(appearance).getSmartCardAmountDue();
            doReturn(new BigDecimal("0.13")).when(appearance).getSmartCardAmountPaid();
            return appearance;
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"EXPENSE_EDITED", "EXPENSE_AUTHORISED"})
        void positiveNoSmartCard(AppearanceStage stage) {
            Map<String, Object> errors = Map.of();
            Appearance appearance = mockAppearance(stage);
            doNothing().when(appearance).addExpenseToErrors(any(), any(), any(), any());
            assertThat(appearance.getExpensesWhereDueIsLessThenPaid()).isEmpty();
            verify(appearance)
                .addExpenseToErrors(errors, "publicTransport",
                    new BigDecimal("0.01"), new BigDecimal("1.01"));
            verify(appearance)
                .addExpenseToErrors(errors, "hiredVehicle",
                    new BigDecimal("0.02"), new BigDecimal("1.02"));
            verify(appearance)
                .addExpenseToErrors(errors, "motorcycle",
                    new BigDecimal("0.03"), new BigDecimal("1.03"));
            verify(appearance)
                .addExpenseToErrors(errors, "car",
                    new BigDecimal("0.04"), new BigDecimal("1.04"));
            verify(appearance)
                .addExpenseToErrors(errors, "bicycle",
                    new BigDecimal("0.05"), new BigDecimal("1.05"));
            verify(appearance)
                .addExpenseToErrors(errors, "parking",
                    new BigDecimal("0.06"), new BigDecimal("1.06"));
            verify(appearance)
                .addExpenseToErrors(errors, "subsistence",
                    new BigDecimal("0.07"), new BigDecimal("1.07"));
            verify(appearance)
                .addExpenseToErrors(errors, "lossOfEarnings",
                    new BigDecimal("0.08"), new BigDecimal("1.09"));
            verify(appearance)
                .addExpenseToErrors(errors, "childcare",
                    new BigDecimal("0.10"), new BigDecimal("1.10"));
            verify(appearance)
                .addExpenseToErrors(errors, "miscAmount",
                    new BigDecimal("0.11"), new BigDecimal("1.11"));
            verify(appearance)
                .addExpenseToErrors(errors, "total",
                    new BigDecimal("0.12"), new BigDecimal("1.12"));
            verify(appearance, times(11))
                .addExpenseToErrors(any(), any(), any(), any());
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.INCLUDE,
            names = {"EXPENSE_EDITED", "EXPENSE_AUTHORISED"})
        void positiveWithSmartCardFailed(AppearanceStage stage) {
            Map<String, Object> errors = Map.of(
                "smartCardAmount", "Must be at most £0.13"
            );
            Appearance appearance = mockAppearance(stage);
            doNothing().when(appearance).addExpenseToErrors(any(), any(), any(), any());
            assertThat(appearance.getExpensesWhereDueIsLessThenPaid()).isEqualTo(
                errors
            );
            verify(appearance)
                .addExpenseToErrors(errors, "publicTransport",
                    new BigDecimal("0.01"), new BigDecimal("1.01"));
            verify(appearance)
                .addExpenseToErrors(errors, "hiredVehicle",
                    new BigDecimal("0.02"), new BigDecimal("1.02"));
            verify(appearance)
                .addExpenseToErrors(errors, "motorcycle",
                    new BigDecimal("0.03"), new BigDecimal("1.03"));
            verify(appearance)
                .addExpenseToErrors(errors, "car",
                    new BigDecimal("0.04"), new BigDecimal("1.04"));
            verify(appearance)
                .addExpenseToErrors(errors, "bicycle",
                    new BigDecimal("0.05"), new BigDecimal("1.05"));
            verify(appearance)
                .addExpenseToErrors(errors, "parking",
                    new BigDecimal("0.06"), new BigDecimal("1.06"));
            verify(appearance)
                .addExpenseToErrors(errors, "subsistence",
                    new BigDecimal("0.07"), new BigDecimal("1.07"));
            verify(appearance)
                .addExpenseToErrors(errors, "lossOfEarnings",
                    new BigDecimal("0.08"), new BigDecimal("1.09"));
            verify(appearance)
                .addExpenseToErrors(errors, "childcare",
                    new BigDecimal("0.10"), new BigDecimal("1.10"));
            verify(appearance)
                .addExpenseToErrors(errors, "miscAmount",
                    new BigDecimal("0.11"), new BigDecimal("1.11"));
            verify(appearance)
                .addExpenseToErrors(errors, "total",
                    new BigDecimal("0.12"), new BigDecimal("1.12"));
            verify(appearance, times(11))
                .addExpenseToErrors(any(), any(), any(), any());
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.INCLUDE,
            names = {"EXPENSE_EDITED", "EXPENSE_AUTHORISED"})
        void positiveWithSmartCardPassed(AppearanceStage stage) {
            Map<String, Object> errors = Map.of();
            Appearance appearance = mockAppearance(stage);
            doReturn(new BigDecimal("0.13")).when(appearance).getSmartCardAmountDue();
            doReturn(new BigDecimal("1.13")).when(appearance).getSmartCardAmountPaid();

            doNothing().when(appearance).addExpenseToErrors(any(), any(), any(), any());
            assertThat(appearance.getExpensesWhereDueIsLessThenPaid()).isEqualTo(errors);
            verify(appearance)
                .addExpenseToErrors(errors, "publicTransport",
                    new BigDecimal("0.01"), new BigDecimal("1.01"));
            verify(appearance)
                .addExpenseToErrors(errors, "hiredVehicle",
                    new BigDecimal("0.02"), new BigDecimal("1.02"));
            verify(appearance)
                .addExpenseToErrors(errors, "motorcycle",
                    new BigDecimal("0.03"), new BigDecimal("1.03"));
            verify(appearance)
                .addExpenseToErrors(errors, "car",
                    new BigDecimal("0.04"), new BigDecimal("1.04"));
            verify(appearance)
                .addExpenseToErrors(errors, "bicycle",
                    new BigDecimal("0.05"), new BigDecimal("1.05"));
            verify(appearance)
                .addExpenseToErrors(errors, "parking",
                    new BigDecimal("0.06"), new BigDecimal("1.06"));
            verify(appearance)
                .addExpenseToErrors(errors, "subsistence",
                    new BigDecimal("0.07"), new BigDecimal("1.07"));
            verify(appearance)
                .addExpenseToErrors(errors, "lossOfEarnings",
                    new BigDecimal("0.08"), new BigDecimal("1.09"));
            verify(appearance)
                .addExpenseToErrors(errors, "childcare",
                    new BigDecimal("0.10"), new BigDecimal("1.10"));
            verify(appearance)
                .addExpenseToErrors(errors, "miscAmount",
                    new BigDecimal("0.11"), new BigDecimal("1.11"));
            verify(appearance)
                .addExpenseToErrors(errors, "total",
                    new BigDecimal("0.12"), new BigDecimal("1.12"));
            verify(appearance, times(11))
                .addExpenseToErrors(any(), any(), any(), any());
        }
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void addExpenseToErrorsIsLessThan() {
        Appearance appearance = new Appearance();
        Map<String, Object> errors = new ConcurrentHashMap<>();
        appearance.addExpenseToErrors(errors, "publicTransport", new BigDecimal("1.00"), new BigDecimal("1.01"));
        assertThat(errors).containsEntry("publicTransport", "Must be at least £1.01");
    }

    @Test
    void addExpenseToErrorsIsEqualTo() {
        Appearance appearance = new Appearance();
        Map<String, Object> errors = new ConcurrentHashMap<>();
        appearance.addExpenseToErrors(errors, "publicTransport", new BigDecimal("1.01"), new BigDecimal("1.01"));
        assertThat(errors).isEmpty();
    }

    @Test
    void addExpenseToErrorsIsGreaterThan() {
        Appearance appearance = new Appearance();
        Map<String, Object> errors = new ConcurrentHashMap<>();
        appearance.addExpenseToErrors(errors, "publicTransport", new BigDecimal("1.02"), new BigDecimal("1.01"));
        assertThat(errors).isEmpty();
    }

    @Nested
    @DisplayName("public void setPayAttendanceType(PayAttendanceType payAttendanceType)")
    class SetPayAttendanceType {
        @Test
        void positiveTypical() {
            Appearance appearance = new Appearance();
            appearance.setAttendanceType(AttendanceType.FULL_DAY);

            appearance.setPayAttendanceType(PayAttendanceType.HALF_DAY);
            assertThat(appearance.getAttendanceType()).isEqualTo(AttendanceType.HALF_DAY);
        }

        @Test
        void positiveTypicalLongTrial() {
            Appearance appearance = new Appearance();
            appearance.setAttendanceType(AttendanceType.FULL_DAY_LONG_TRIAL);

            appearance.setPayAttendanceType(PayAttendanceType.HALF_DAY);
            assertThat(appearance.getAttendanceType()).isEqualTo(AttendanceType.HALF_DAY_LONG_TRIAL);
        }

        @ParameterizedTest
        @EnumSource(value = AttendanceType.class, mode = EnumSource.Mode.INCLUDE,
            names = {"ABSENT", "NON_ATTENDANCE", "NON_ATTENDANCE_LONG_TRIAL"})
        void positiveAttendanceTypeNoUpdate(AttendanceType attendanceType) {
            Appearance appearance = new Appearance();
            appearance.setAttendanceType(attendanceType);
            appearance.setPayAttendanceType(PayAttendanceType.HALF_DAY);
            assertThat(appearance.getAttendanceType()).isEqualTo(attendanceType);
        }
    }

    @Nested
    class IsFullDay {

        @Test
        void positiveHalfDayOnLimit() {
            Appearance appearance = spy(new Appearance());
            doReturn(LocalTime.of(4, 0, 0))
                .when(appearance).getEffectiveTime();
            assertThat(appearance.isFullDay()).isFalse();
        }

        @Test
        void positiveHalfDayBelowLimit() {
            Appearance appearance = spy(new Appearance());
            doReturn(LocalTime.of(3, 59, 59))
                .when(appearance).getEffectiveTime();
            assertThat(appearance.isFullDay()).isFalse();
        }

        @Test
        void positiveFullDay() {
            Appearance appearance = spy(new Appearance());
            doReturn(LocalTime.of(4, 0, 1))
                .when(appearance).getEffectiveTime();
            assertThat(appearance.isFullDay()).isTrue();
        }
    }
}