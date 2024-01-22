package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;

import java.util.List;

public interface ICourtroomRepository {
    List<Tuple> getCourtroomsForLocation(List<String> courts);
}
