package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
        AttendanceType attendanceType = mock(AttendanceType.class);
        appearance.setAttendanceType(attendanceType);
        when(attendanceType.getIsLongTrial()).thenReturn(true);
        assertThat(appearance.isLongTrialDay()).isEqualTo(true);
    }

    @Test
    void isLongTrialDayFalse() {
        Appearance appearance = new Appearance();
        AttendanceType attendanceType = mock(AttendanceType.class);
        appearance.setAttendanceType(attendanceType);
        when(attendanceType.getIsLongTrial()).thenReturn(false);
        assertThat(appearance.isLongTrialDay()).isEqualTo(false);
    }

    @Test
    void isLongTrialDayNull() {
        Appearance appearance = new Appearance();
        AttendanceType attendanceType = mock(AttendanceType.class);
        appearance.setAttendanceType(attendanceType);
        when(attendanceType.getIsLongTrial()).thenReturn(null);
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
        doReturn(new BigDecimal("1.00")).when(appearance).getSmartCardAmountDue();
        assertThat(appearance.getSubsistenceTotalDue()).isEqualTo(
            new BigDecimal("9.00"));
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
        doReturn(new BigDecimal("1.00")).when(appearance).getSmartCardAmountPaid();
        assertThat(appearance.getSubsistenceTotalPaid()).isEqualTo(
            new BigDecimal("9.00"));
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

        doReturn(new BigDecimal("3.00")).when(appearance).getSmartCardAmountDue();
        doReturn(new BigDecimal("1.00")).when(appearance).getSmartCardAmountPaid();

        assertThat(appearance.getTotalChanged())
            .isEqualTo(new BigDecimal("8.00"));
    }


    @Nested
    @DisplayName("isExpenseDetailsValid()")
    class IsExpenseDetailsValid {

        private Appearance mockAppearance() {
            Appearance appearance = spy(new Appearance());

            doReturn(BigDecimal.ZERO).when(appearance).getPublicTransportDue();
            doReturn(BigDecimal.ZERO).when(appearance).getPublicTransportPaid();
            doReturn(BigDecimal.ZERO).when(appearance).getHiredVehicleDue();
            doReturn(BigDecimal.ZERO).when(appearance).getHiredVehiclePaid();
            doReturn(BigDecimal.ZERO).when(appearance).getMotorcycleDue();
            doReturn(BigDecimal.ZERO).when(appearance).getMotorcyclePaid();
            doReturn(BigDecimal.ZERO).when(appearance).getCarDue();
            doReturn(BigDecimal.ZERO).when(appearance).getCarPaid();
            doReturn(BigDecimal.ZERO).when(appearance).getBicycleDue();
            doReturn(BigDecimal.ZERO).when(appearance).getBicyclePaid();
            doReturn(BigDecimal.ZERO).when(appearance).getParkingDue();
            doReturn(BigDecimal.ZERO).when(appearance).getParkingPaid();
            doReturn(BigDecimal.ZERO).when(appearance).getSubsistenceDue();
            doReturn(BigDecimal.ZERO).when(appearance).getSubsistencePaid();
            doReturn(BigDecimal.ZERO).when(appearance).getLossOfEarningsDue();
            doReturn(BigDecimal.ZERO).when(appearance).getLossOfEarningsPaid();
            doReturn(BigDecimal.ZERO).when(appearance).getChildcareDue();
            doReturn(BigDecimal.ZERO).when(appearance).getChildcarePaid();
            doReturn(BigDecimal.ZERO).when(appearance).getMiscAmountDue();
            doReturn(BigDecimal.ZERO).when(appearance).getMiscAmountPaid();
            doReturn(BigDecimal.ZERO).when(appearance).getTotalDue();
            doReturn(BigDecimal.ZERO).when(appearance).getTotalPaid();
            doReturn(AppearanceStage.EXPENSE_EDITED).when(appearance).getAppearanceStage();
            doReturn(BigDecimal.ZERO).when(appearance).getSmartCardAmountDue();
            doReturn(BigDecimal.ZERO).when(appearance).getSmartCardAmountPaid();
            return appearance;
        }

        @Test
        void positiveTypical() {
            Appearance appearance = mockAppearance();
            assertThat(appearance.isExpenseDetailsValid()).isTrue();
        }

        @Test
        void negativePublicTransport() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getPublicTransportPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeHiredVehicle() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getHiredVehiclePaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeMotorcycle() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getMotorcyclePaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeCar() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getCarPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeBicycle() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getBicyclePaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeParking() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getParkingPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeSubsistence() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getSubsistencePaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeLossOfEarnings() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getLossOfEarningsPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeChildcare() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getChildcarePaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeMiscAmount() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getMiscAmountPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeTotalPaid() {
            Appearance appearance = mockAppearance();
            doReturn(new BigDecimal("0.01")).when(appearance).getTotalPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeSmartCardAmountEdited() {
            Appearance appearance = mockAppearance();
            doReturn(AppearanceStage.EXPENSE_EDITED).when(appearance).getAppearanceStage();
            doReturn(new BigDecimal("1.0")).when(appearance).getSmartCardAmountDue();
            doReturn(new BigDecimal("0.9")).when(appearance).getSmartCardAmountPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @Test
        void negativeSmartCardAmountAuthorised() {
            Appearance appearance = mockAppearance();
            doReturn(AppearanceStage.EXPENSE_AUTHORISED).when(appearance).getAppearanceStage();
            doReturn(new BigDecimal("1.0")).when(appearance).getSmartCardAmountDue();
            doReturn(new BigDecimal("0.9")).when(appearance).getSmartCardAmountPaid();
            assertThat(appearance.isExpenseDetailsValid()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"EXPENSE_EDITED", "EXPENSE_AUTHORISED"})
        void positiveSmartCardAmountNotEditedOrAuthorised(AppearanceStage stage) {
            Appearance appearance = mockAppearance();
            doReturn(stage).when(appearance).getAppearanceStage();
            doReturn(new BigDecimal("1.0")).when(appearance).getSmartCardAmountDue();
            doReturn(new BigDecimal("0.0")).when(appearance).getSmartCardAmountPaid();
            assertThat(appearance.isExpenseDetailsValid()).isTrue();
        }
    }
}