package uk.gov.hmcts.juror.api.bureau.domain;


import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * QueryDSL queries for {@link CourtCatchmentEntity}.
 */

public class CourtCatchmentEntityQueries {

    private CourtCatchmentEntityQueries() {

    }

    /**
     * Matches Court Codes  in CourtCatchment Table where postcodes = 'postcode'.
     *
     * @return courtCode
     */

    private static final QCourtCatchmentEntity courtCatchmentEntityDetail = QCourtCatchmentEntity.courtCatchmentEntity;

    public static BooleanExpression matchLocCode(String postCode) {
        return courtCatchmentEntityDetail.courtCode.eq(postCode);
    }

}
