package uk.gov.hmcts.juror.api.moj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PoolRequestListAndCount {

    List<PoolRequest> poolRequestList;

    Long poolRequestCount;

}
