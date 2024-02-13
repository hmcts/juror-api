package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.SortMethod;

public interface IsPageable {

    SortMethod getSortMethod();

    SortMethod.HasComparableExpression getSortField();

    long getPageLimit();

    long getPageNumber();
}
