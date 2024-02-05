package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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

    @DisplayName("PublicTransportTotal")
    @Nested
    class PublicTransportTotal extends AbstractAppearanceIndividualTotalSumTest {
        private PublicTransportTotal() {
            super(Appearance::getPublicTransportTotal,
                Appearance::setPublicTransportDue,
                Appearance::setPublicTransportPaid);
        }
    }

    @DisplayName("HiredVehicleTotal")
    @Nested
    class HiredVehicleTotal extends AbstractAppearanceIndividualTotalSumTest {
        private HiredVehicleTotal() {
            super(Appearance::getHiredVehicleTotal,
                Appearance::setHiredVehicleDue,
                Appearance::setHiredVehiclePaid);
        }
    }

    @DisplayName("MotorcycleTotal")
    @Nested
    class MotorcycleTotal extends AbstractAppearanceIndividualTotalSumTest {
        private MotorcycleTotal() {
            super(Appearance::getMotorcycleTotal,
                Appearance::setMotorcycleDue,
                Appearance::setMotorcyclePaid);
        }
    }

    @DisplayName("CarTotal")
    @Nested
    class CarTotal extends AbstractAppearanceIndividualTotalSumTest {
        private CarTotal() {
            super(Appearance::getCarTotal,
                Appearance::setCarDue,
                Appearance::setCarPaid);
        }
    }

    @DisplayName("BicycleTotal")
    @Nested
    class BicycleTotal extends AbstractAppearanceIndividualTotalSumTest {
        private BicycleTotal() {
            super(Appearance::getBicycleTotal,
                Appearance::setBicycleDue,
                Appearance::setBicyclePaid);
        }
    }

    @DisplayName("ParkingTotal")
    @Nested
    class ParkingTotal extends AbstractAppearanceIndividualTotalSumTest {
        private ParkingTotal() {
            super(Appearance::getParkingTotal,
                Appearance::setParkingDue,
                Appearance::setParkingPaid);
        }
    }

    @DisplayName("SubsistenceTotal")
    @Nested
    class SubsistenceTotal extends AbstractAppearanceIndividualTotalSumTest {
        private SubsistenceTotal() {
            super(Appearance::getSubsistenceTotal,
                Appearance::setSubsistenceDue,
                Appearance::setSubsistencePaid);
        }
    }

    @DisplayName("LossOfEarningsTotal")
    @Nested
    class LossOfEarningsTotal extends AbstractAppearanceIndividualTotalSumTest {
        private LossOfEarningsTotal() {
            super(Appearance::getLossOfEarningsTotal,
                Appearance::setLossOfEarningsDue,
                Appearance::setLossOfEarningsPaid);
        }
    }

    @DisplayName("ChildcareTotal")
    @Nested
    class ChildcareTotal extends AbstractAppearanceIndividualTotalSumTest {
        private ChildcareTotal() {
            super(Appearance::getChildcareTotal,
                Appearance::setChildcareDue,
                Appearance::setChildcarePaid);
        }
    }

    @DisplayName("MiscAmountTotal")
    @Nested
    class MiscAmountTotal extends AbstractAppearanceIndividualTotalSumTest {
        private MiscAmountTotal() {
            super(Appearance::getMiscAmountTotal,
                Appearance::setMiscAmountDue,
                Appearance::setMiscAmountPaid);
        }
    }

    @DisplayName("SmartCardAmount")
    @Nested
    class SmartCardAmountTotal extends AbstractAppearanceIndividualTotalSumTest {
        private SmartCardAmountTotal() {
            super(Appearance::getSmartCardAmountTotal,
                Appearance::setSmartCardAmountDue,
                Appearance::setSmartCardAmountPaid);
        }
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

    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    abstract static class AbstractAppearanceIndividualTotalSumTest {

        private final Function<Appearance, BigDecimal> getTotalSupplier;
        private final BiConsumer<Appearance, BigDecimal> setDueAmount;
        private final BiConsumer<Appearance, BigDecimal> setPaidAmount;

        private AbstractAppearanceIndividualTotalSumTest(Function<Appearance, BigDecimal> getTotalSupplier,
                                                         BiConsumer<Appearance, BigDecimal> setDueAmount,
                                                         BiConsumer<Appearance, BigDecimal> setPaidAmount) {
            this.getTotalSupplier = getTotalSupplier;
            this.setDueAmount = setDueAmount;
            this.setPaidAmount = setPaidAmount;
        }

        private Appearance createAppearance() {
            return new Appearance();
        }

        private void triggerTest(BigDecimal dueValue, BigDecimal paidValue) {
            Appearance appearance = createAppearance();
            setDueAmount.accept(appearance, dueValue);
            setPaidAmount.accept(appearance, paidValue);

            assertEquals(
                Optional.ofNullable(dueValue).orElse(BigDecimal.ZERO)
                    .add(Optional.ofNullable(paidValue).orElse(BigDecimal.ZERO)),
                getTotalSupplier.apply(appearance),
                "Expect sum of due and paid to match"
            );
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void positiveDueAmountNullPaidNotNull() {
            triggerTest(null, new BigDecimal("1.11"));
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void positiveDueAmountNotNullPaidNull() {
            triggerTest(new BigDecimal("6.31"), null);
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void positiveDueAmountNotNullPaidNotNull() {
            triggerTest(new BigDecimal("3.11"), new BigDecimal("6.71"));
        }
    }
}