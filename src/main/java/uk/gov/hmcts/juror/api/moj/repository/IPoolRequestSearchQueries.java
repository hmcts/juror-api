package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface IPoolRequestSearchQueries {


    JPAQuery<Tuple> selectFromPoolRequest();

    void addPoolNumberPredicate(JPAQuery<Tuple> query, String poolNumber);

    void addPartialPoolNumberPredicate(JPAQuery<Tuple> query, String poolNumberPrefix);

    void addCourtLocationPredicate(JPAQuery<Tuple> query, String courtLocationCode);

    void addCourtUserPredicate(JPAQuery<Tuple> query, List<String> courtLocationCodes);

    void addServiceStartDatePredicate(JPAQuery<Tuple> query, LocalDate returnDate);

    void addPoolStatusPredicate(JPAQuery<Tuple> query, Collection<PoolSearchRequestDto.PoolStatus> poolStatus);

    void addPoolStagePredicate(JPAQuery<Tuple> query, List<PoolSearchRequestDto.PoolStage> poolStage);

    void addPoolTypePredicate(JPAQuery<Tuple> query, List<String> poolType);

    void addNilPoolPredicate(JPAQuery<Tuple> query, Boolean isNilPool);

    void orderByStringColumn(JPAQuery<Tuple> query, StringPath simpleColumn, SortDirection sortDirection);

    void orderByDateColumn(JPAQuery<Tuple> query, DatePath<LocalDate> simpleColumn, SortDirection sortDirection);

    void orderByPoolStage(JPAQuery<Tuple> query, SortDirection sortDirection);

    void orderByPoolStatus(JPAQuery<Tuple> query, SortDirection sortDirection);

}
