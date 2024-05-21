package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.querydsl.core.types.ExpressionUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.QUser;

import static org.assertj.core.api.Assertions.assertThat;

class UserSearchDtoTest extends AbstractValidatorTest<UserSearchDto> {


    @Override
    protected UserSearchDto createValidObject() {
        return UserSearchDto.builder()
            .pageNumber(1)
            .pageLimit(1)
            .build();
    }

    @Nested
    class PageNumberTest extends AbstractValidationFieldTestLong {
        protected PageNumberTest() {
            super("pageNumber", UserSearchDto::setPageNumber);
            addMin(1L, null);
        }
    }


    @Nested
    class PageLimitTest extends AbstractValidationFieldTestLong {
        protected PageLimitTest() {
            super("pageLimit", UserSearchDto::setPageLimit);
            addMin(1L, null);
        }
    }

    @Nested
    class CourtTest extends AbstractValidationFieldTestString {
        protected CourtTest() {
            super("court", UserSearchDto::setCourt);
            addInvalidPatternTest("invalid", "^\\d{3}$",
                null);
        }
    }

    @Nested
    class SortFieldTest {
        @Test
        void nameTest() {
            assertThat(UserSearchDto.SortField.NAME.getComparableExpression())
                .isEqualTo(QUser.user.name);
        }

        @Test
        void emailTest() {
            assertThat(UserSearchDto.SortField.EMAIL.getComparableExpression())
                .isEqualTo(QUser.user.email);
        }

        @Test
        void userTypeTest() {
            assertThat(UserSearchDto.SortField.USER_TYPE.getComparableExpression())
                .isEqualTo(QUser.user.userType);
        }

        @Test
        void courtTest() {
            assertThat(UserSearchDto.SortField.COURT.getComparableExpression())
                .isEqualTo(QUser.user.courts.any().owner);
        }

        @Test
        void lastSignedInTest() {
            assertThat(UserSearchDto.SortField.LAST_SIGNED_IN.getComparableExpression())
                .isEqualTo(QUser.user.lastLoggedIn);
        }

        @Test
        void activeTest() {
            assertThat(UserSearchDto.SortField.ACTIVE.getComparableExpression())
                .isEqualTo(QUser.user.active);
        }

        @Test
        void managerTest() {
            assertThat(UserSearchDto.SortField.MANAGER.getComparableExpression())
                .isEqualTo(ExpressionUtils.path(Boolean.class, "is_manager"));
        }

        @Test
        void seniorJurorOfficerTest() {
            assertThat(UserSearchDto.SortField.SENIOR_JUROR_OFFICER.getComparableExpression())
                .isEqualTo(ExpressionUtils.path(Boolean.class, "is_senior_juror_officer"));
        }
    }
}
