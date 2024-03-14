package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserCourtDtoTest extends AbstractValidatorTest<UserCourtDto> {

    public static UserCourtDto getValidObject() {
        return UserCourtDto.builder()
            .primaryCourt(
                CourtDto.builder().locCode("001").build())
            .satelliteCourts(
                List.of(
                    CourtDto.builder().locCode("002").build(),
                    CourtDto.builder().locCode("003").build()
                )
            )
            .build();
    }

    @Override
    protected UserCourtDto createValidObject() {
        return getValidObject();
    }

    @Nested
    class PrimaryCourtTest extends AbstractValidationFieldTestBase<CourtDto> {
        protected PrimaryCourtTest() {
            super("primaryCourt", UserCourtDto::setPrimaryCourt);
            addRequiredTest(null);
        }
    }

    @Test
    void positiveConstructorJustMain() {
        UserCourtDto userCourtDto = new UserCourtDto(List.of(
            mockCourtLocation("name1", "001", CourtType.MAIN)
        ));
        assertThat(userCourtDto.getPrimaryCourt())
            .isEqualTo(CourtDto.builder()
                .locCode("001")
                .name("name1")
                .courtType(CourtType.MAIN)
                .build());
        assertThat(userCourtDto.getSatelliteCourts()).isEmpty();
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveConstructorJustHasSatellite() {
        UserCourtDto userCourtDto = new UserCourtDto(List.of(
            mockCourtLocation("name1", "001", CourtType.MAIN),
            mockCourtLocation("name2", "002", CourtType.SATELLITE),
            mockCourtLocation("name3", "003", CourtType.SATELLITE)
        ));
        assertThat(userCourtDto.getPrimaryCourt())
            .isEqualTo(CourtDto.builder()
                .locCode("001")
                .name("name1")
                .courtType(CourtType.MAIN)
                .build());
        assertThat(userCourtDto.getSatelliteCourts())
            .hasSize(2)
            .containsExactly(
                CourtDto.builder()
                    .locCode("002")
                    .name("name2")
                    .courtType(CourtType.SATELLITE)
                    .build(),
                CourtDto.builder()
                    .locCode("003")
                    .name("name3")
                    .courtType(CourtType.SATELLITE)
                    .build());

    }

    private CourtLocation mockCourtLocation(String name, String locCode, CourtType courtType) {
        CourtLocation location = mock(CourtLocation.class);
        when(location.getName()).thenReturn(name);
        when(location.getLocCode()).thenReturn(locCode);
        when(location.getType()).thenReturn(courtType);
        return location;
    }
}
