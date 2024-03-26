package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractGroupedReport extends AbstractReport<Map<String, List<LinkedHashMap<String, Object>>>> {

    final DataType groupBy;
    final boolean removeGroupByFromResponse;

    public AbstractGroupedReport(PoolRequestRepository poolRequestRepository,
                                 EntityPath<?> from, DataType groupBy,
                                 boolean removeGroupByFromResponse, DataType... dataType) {
        super(poolRequestRepository, from, combine(groupBy, dataType));
        this.removeGroupByFromResponse = removeGroupByFromResponse;
        this.groupBy = groupBy;
    }

    static DataType[] combine(DataType groupBy, DataType... dataType) {
        List<DataType> list = new ArrayList<>();
        list.add(groupBy);
        list.addAll(Arrays.asList(dataType));
        return list.toArray(new DataType[0]);
    }

    @Override
    protected Map<String, List<LinkedHashMap<String, Object>>> getTableData(List<Tuple> data) {
        return getTableDataAsList(data)
            .stream()
            .collect(Collectors.groupingBy(e -> e.get(groupBy.getId()).toString()));
    }

    @Override
    protected GroupedReportResponse createBlankResponse() {
        return GroupedReportResponse.builder()
            .groupBy(groupBy)
            .build();
    }

    @Override
    public AbstractReportResponse<Map<String, List<LinkedHashMap<String, Object>>>> getStandardReportResponse(
        StandardReportRequest request) {
        AbstractReportResponse<Map<String, List<LinkedHashMap<String, Object>>>> response =
            super.getStandardReportResponse(request);
        if (removeGroupByFromResponse) {
            response.getTableData().getHeadings().removeIf(heading -> groupBy.getId().equals(heading.getId()));
            response.getTableData().getData().forEach((string, linkedHashMaps) -> linkedHashMaps.forEach(
                linkedHashMap -> linkedHashMap.remove(groupBy.getId())));
        }
        return response;
    }
}
