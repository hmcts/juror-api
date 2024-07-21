package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractGroupedReport extends AbstractReport<GroupedTableData> {


    final IReportGroupBy groupBy;

    public AbstractGroupedReport(EntityPath<?> from, IReportGroupBy groupBy, IDataType... dataType) {
        this(null, from, groupBy, dataType);
    }


    public AbstractGroupedReport(PoolRequestRepository poolRequestRepository,
                                 EntityPath<?> from, IReportGroupBy groupBy, IDataType... dataType) {
        super(poolRequestRepository, from, combine(groupBy, dataType));
        this.groupBy = groupBy;
    }


    @Override
    protected GroupedTableData getTableData(List<Tuple> data) {
        return groupData(groupBy,
            getTableDataAsList(data)
        );
    }

    private GroupedTableData groupData(IReportGroupBy groupBy, List<LinkedHashMap<String, Object>> data) {
        List<GroupedTableData> groupedData = data
            .stream()
            .map(stringObjectLinkedHashMap -> {
                GroupedTableData groupedTableData = new GroupedTableData();
                groupedTableData.putAll(stringObjectLinkedHashMap);
                groupedTableData.setType(GroupedTableData.Type.DATA);
                return groupedTableData;
            }).toList();
        return subGroup(groupBy, groupedData);
    }


    private GroupedTableData subGroup(IReportGroupBy groupBy, List<GroupedTableData> data) {
        Map<String, List<GroupedTableData>> groupedData = groupBy.sortData(data
            .stream()
            .collect(Collectors.groupingBy(groupBy::getGroupFunction,
                LinkedHashMap::new,
                Collectors.toList())));

        GroupedTableData response = new GroupedTableData();
        response.putAll(groupedData);
        response.setType(GroupedTableData.Type.GROUPED);

        if (groupBy.getNested() != null) {
            groupedData.forEach((key, value) -> {
                Map<String, ?> nestedData = subGroup(groupBy.getNested(), value);
                response.put(key, nestedData);
            });
        }
        return response;
    }

    @Override
    protected GroupedReportResponse createBlankResponse() {
        return GroupedReportResponse.builder()
            .groupBy(groupBy.getGroupedByResponse())
            .build();
    }

    @Override
    public AbstractReportResponse<GroupedTableData> getStandardReportResponse(
        StandardReportRequest request) {
        AbstractReportResponse<GroupedTableData> response =
            super.getStandardReportResponse(request);

        IReportGroupBy tmpGroupBy = groupBy;
        while (tmpGroupBy != null) {
            tmpGroupBy.getCombinedKeysToRemove().forEach(key -> {
                response.getTableData().getHeadings().removeIf(heading -> key.equals(heading.getId()));
                response.getTableData().getData().removeDataKey(key);
            });
            tmpGroupBy = tmpGroupBy.getNested();
        }
        return response;
    }


    static IDataType[] combine(IReportGroupBy groupBy, IDataType... dataType) {
        List<IDataType> list = new ArrayList<>();
        list.addAll(groupBy.getCombinedRequiredDataTypes());
        list.addAll(Arrays.asList(dataType));
        return list.toArray(new IDataType[0]);
    }
}
