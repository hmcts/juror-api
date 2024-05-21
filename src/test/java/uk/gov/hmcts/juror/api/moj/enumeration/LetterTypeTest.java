package uk.gov.hmcts.juror.api.moj.enumeration;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LetterTypeTest {


    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    abstract static class LetterTypeTestBase {

        private final List<FormCode> formCodes;
        private final List<ReissueLetterService.DataType> dataTypes;
        @Getter
        private final LetterType letterType;

        public LetterTypeTestBase(LetterType letterType, List<FormCode> formCodes,
                                  List<ReissueLetterService.DataType> dataTypes) {
            this.letterType = letterType;
            this.formCodes = formCodes;
            this.dataTypes = dataTypes;
        }

        @Test
        void positiveConstructorTest() {
            assertThat(letterType).isNotNull();
            assertThat(letterType.getFormCodes()).containsExactly(formCodes.toArray(new FormCode[0]));
            assertThat(letterType.getReissueDataTypes()).containsExactly(
                dataTypes.toArray(new ReissueLetterService.DataType[0]));
        }

        @Test
        void positiveLetterQueryConsumerTest() {
            assertThat(letterType.getLetterQueryConsumer()).isNull();
        }

        @SuppressWarnings("unchecked")
        protected JPAQuery<Tuple> mockJpaQuery() {
            return mock(JPAQuery.class);
        }

        protected JPAQuery<Tuple> positiveLetterQueryConsumerTestSetup() {
            JPAQuery<Tuple> jpaQuery = mockJpaQuery();
            assertThat(getLetterType().getLetterQueryConsumer()).isNotNull();
            getLetterType().getLetterQueryConsumer().accept(jpaQuery);
            return jpaQuery;
        }
    }

    @Nested
    @DisplayName("SUMMONED_REMINDERS")
    class SummonedReminders extends LetterTypeTestBase {

        SummonedReminders() {
            super(LetterType.SUMMONED_REMINDER, List.of(FormCode.ENG_SUMMONS_REMINDER, FormCode.BI_SUMMONS_REMINDER),
                List.of(
                    ReissueLetterService.DataType.JUROR_NUMBER,
                    ReissueLetterService.DataType.JUROR_FIRST_NAME,
                    ReissueLetterService.DataType.JUROR_LAST_NAME,
                    ReissueLetterService.DataType.JUROR_POSTCODE,
                    ReissueLetterService.DataType.DATE_PRINTED,
                    ReissueLetterService.DataType.EXTRACTED_FLAG,
                    ReissueLetterService.DataType.FORM_CODE
                ));
        }


        @Override
        @Test
        void positiveLetterQueryConsumerTest() {
            JPAQuery<Tuple> jpaQuery = positiveLetterQueryConsumerTestSetup();

            verify(jpaQuery, times(1)).where(
                QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)
                    .and(QJuror.juror.responded.eq(false)));
        }
    }
}
