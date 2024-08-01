package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;

import java.time.LocalDate;
import java.util.List;

public interface IPoolStatisticsRepository {
    List<Tuple> getStatisticsByCourtLocationAndPoolType(String owner, String courtLocationCode, String poolType,
                                                        LocalDate weekCommencing, int numberOfWeeks);

    List<Tuple> getNilPools(String owner, String courtLocationCode, String poolType,
                            LocalDate weekCommencing, int numberOfWeeks);
}
