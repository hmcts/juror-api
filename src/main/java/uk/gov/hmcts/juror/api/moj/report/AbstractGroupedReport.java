package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractGroupedReport extends AbstractReport<GroupedTableData> {


    final GroupBy groupBy;

    public AbstractGroupedReport(EntityPath<?> from, GroupBy groupBy, IDataType... dataType) {
        this(null, from, groupBy, dataType);
    }


    public AbstractGroupedReport(PoolRequestRepository poolRequestRepository,
                                 EntityPath<?> from, GroupBy groupBy, IDataType... dataType) {
        super(poolRequestRepository, from, combine(groupBy, dataType));
        this.groupBy = groupBy;
    }


    @Override
    protected GroupedTableData getTableData(List<Tuple> data) {
        return groupData(groupBy,
            getTableDataAsList(data)
        );
    }

    private GroupedTableData groupData(GroupBy groupBy, List<LinkedHashMap<String, Object>> data) {
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


    private GroupedTableData subGroup(GroupBy groupBy, List<GroupedTableData> data) {
        Map<String, List<GroupedTableData>> groupedData = data
            .stream()
            .collect(Collectors.groupingBy(e -> e.get(groupBy.getId()).toString()));

        GroupedTableData response = new GroupedTableData();
        response.putAll(groupedData);
        response.setType(GroupedTableData.Type.GROUPED);

        if (groupBy.nested != null) {
            groupedData.forEach((key, value) -> {
                Map<String, ?> nestedData = subGroup(groupBy.nested, value);
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

        GroupBy tmpGroupBy = groupBy;
        while (tmpGroupBy != null) {
            if (tmpGroupBy.removeGroupByFromResponse) {
                response.getTableData().getHeadings().removeIf(heading -> groupBy.getId().equals(heading.getId()));
                response.getTableData().getData().removeDataKey(groupBy.getId());
            }
            tmpGroupBy = tmpGroupBy.getNested();
        }
        return response;
    }


    @SuperBuilder
    @Getter
    @Data
    public static final class GroupBy {
        private DataType dataType;
        private boolean removeGroupByFromResponse;
        private GroupBy nested;


        public Collection<IDataType> getDataTypes() {
            List<IDataType> combinedDataTypes = new ArrayList<>();
            combinedDataTypes.add(this.dataType);
            if (nested != null) {
                combinedDataTypes.addAll(nested.getDataTypes());
            }
            return combinedDataTypes;
        }

        public GroupByResponse getGroupedByResponse() {
            return GroupByResponse.builder()
                .name(dataType)
                .nested(nested != null ? nested.getGroupedByResponse() : null)
                .build();
        }

        public String getId() {
            return dataType.getId();
        }
    }


    static IDataType[] combine(GroupBy groupBy, IDataType... dataType) {
        List<IDataType> list = new ArrayList<>();
        list.addAll(groupBy.getDataTypes());
        list.addAll(Arrays.asList(dataType));
        return list.toArray(new IDataType[0]);
    }
}
