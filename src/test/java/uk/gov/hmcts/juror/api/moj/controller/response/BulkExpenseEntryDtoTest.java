package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


class BulkExpenseEntryDtoTest extends AbstractValidatorTest<BulkExpenseEntryDto> {
    public static BulkExpenseEntryDto createValid() {
        BulkExpenseEntryDto bulkExpenseEntryDto = new BulkExpenseEntryDto();
        bulkExpenseEntryDto.setAppearanceDate(LocalDate.now());
        bulkExpenseEntryDto.setAttendanceType(AttendanceType.FULL_DAY);
        bulkExpenseEntryDto.setPaymentMethod(PaymentMethod.BACS);
        return bulkExpenseEntryDto;
    }

    @Override
    protected BulkExpenseEntryDto createValidObject() {
        return createValid();
    }

    @ParameterizedTest(name = "Positive - from appearance is mapped correctly {0}")
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("PMD.LawOfDemeter")
    void positiveFromAppearance(boolean isCash) {
        LocalDate date = LocalDate.now();
        BulkExpenseEntryDto expectedBulkExpenseEntryDto = BulkExpenseEntryDto.builder()
            .publicTransport(new BigDecimal("1.00"))
            .taxi(new BigDecimal("2.00"))
            .motorcycle(new BigDecimal("3.00"))
            .car(new BigDecimal("4.00"))
            .bicycle(new BigDecimal("5.00"))
            .parking(new BigDecimal("6.00"))
            .foodAndDrink(new BigDecimal("7.00"))
            .lossOfEarnings(new BigDecimal("8.00"))
            .extraCare(new BigDecimal("9.00"))
            .other(new BigDecimal("10.00"))
            .smartCard(new BigDecimal("11.00"))
            .appearanceDate(date)
            .attendanceType(AttendanceType.FULL_DAY)
            .paymentMethod(
                Boolean.TRUE.equals(isCash) ? PaymentMethod.CASH : PaymentMethod.BACS)
            .build();


        Appearance appearance = mock(Appearance.class);
        when(appearance.getPublicTransportTotal()).thenReturn(new BigDecimal("1.00"));
        when(appearance.getHiredVehicleTotal()).thenReturn(new BigDecimal("2.00"));
        when(appearance.getMotorcycleTotal()).thenReturn(new BigDecimal("3.00"));
        when(appearance.getCarTotal()).thenReturn(new BigDecimal("4.00"));
        when(appearance.getBicycleTotal()).thenReturn(new BigDecimal("5.00"));
        when(appearance.getParkingTotal()).thenReturn(new BigDecimal("6.00"));
        when(appearance.getSubsistenceTotal()).thenReturn(new BigDecimal("7.00"));
        when(appearance.getLossOfEarningsTotal()).thenReturn(new BigDecimal("8.00"));
        when(appearance.getChildcareTotal()).thenReturn(new BigDecimal("9.00"));
        when(appearance.getMiscAmountTotal()).thenReturn(new BigDecimal("10.00"));
        when(appearance.getSmartCardAmountTotal()).thenReturn(new BigDecimal("11.00"));
        when(appearance.getAttendanceDate()).thenReturn(date);
        when(appearance.getAttendanceType()).thenReturn(AttendanceType.FULL_DAY);
        when(appearance.getPayCash()).thenReturn(isCash);

        assertEquals(expectedBulkExpenseEntryDto, BulkExpenseEntryDto.fromAppearance(appearance),
            "Created bulk expense Entity DTO should match values from provided apperance object");

        verify(appearance, times(1)).getPublicTransportTotal();
        verify(appearance, times(1)).getHiredVehicleTotal();
        verify(appearance, times(1)).getMotorcycleTotal();
        verify(appearance, times(1)).getCarTotal();
        verify(appearance, times(1)).getBicycleTotal();
        verify(appearance, times(1)).getParkingTotal();
        verify(appearance, times(1)).getSubsistenceTotal();
        verify(appearance, times(1)).getLossOfEarningsTotal();
        verify(appearance, times(1)).getChildcareTotal();
        verify(appearance, times(1)).getMiscAmountTotal();
        verify(appearance, times(1)).getSmartCardAmountTotal();
        verify(appearance, times(1)).getAttendanceDate();
        verify(appearance, times(1)).getAttendanceType();
        verify(appearance, times(1)).getPayCash();
        verifyNoMoreInteractions(appearance);
    }


    @Nested
    class AppearanceDate extends AbstractValidationFieldTestLocalDate {
        protected AppearanceDate() {
            super("appearanceDate", BulkExpenseEntryDto::setAppearanceDate);
            addRequiredTest(null);
        }
    }

    @Nested
    class AttendanceTypeTest extends AbstractValidationFieldTestBase<AttendanceType> {
        protected AttendanceTypeTest() {
            super("attendanceType", BulkExpenseEntryDto::setAttendanceType);
            addRequiredTest(null);
        }
    }

    @Nested
    class PaymentMethodTest extends AbstractValidationFieldTestBase<PaymentMethod> {
        protected PaymentMethodTest() {
            super("paymentMethod", BulkExpenseEntryDto::setPaymentMethod);
            addRequiredTest(null);
        }
    }

    @Nested
    class OriginalValue extends AbstractValidationFieldTestBase<BulkExpenseEntryDto> {
        protected OriginalValue() {
            super("originalValue", BulkExpenseEntryDto::setOriginalValue);
            addNotRequiredTest(createValid());
        }
    }
}
