package uk.gov.hmcts.juror.api.moj.report.support;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface AvailableListReport {


    default void addStandardFilters(JPAQuery<Tuple> query, StandardReportRequest request) {
        if (!request.getIncludeJurorsOnCall()) {
            query.where(QJurorPool.jurorPool.onCall.ne(true));
        }

        List<Integer> allowedStatus = new ArrayList<>();
        allowedStatus.add(IJurorStatus.RESPONDED);
        if (!request.getRespondedJurorsOnly()) {
            allowedStatus.add(IJurorStatus.SUMMONED);
        }
        if (request.getIncludePanelMembers()) {
            allowedStatus.add(IJurorStatus.PANEL);
            allowedStatus.add(IJurorStatus.JUROR);
        }
        query.where(QJurorPool.jurorPool.status.status.in(allowedStatus));
    }

    default Map<String, AbstractReportResponse.DataTypeValue> getHeadingsInternal(StandardReportRequest request,
                                                                                  AbstractReportResponse.TableData<?
                                                                                      extends HasSize> tableData) {
        return Map.of(
            "total_available_pool_members",
            AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total available pool members")
                .dataType(Long.class.getSimpleName())
                .value(tableData.getData().getSize())
                .build()
        );
    }

    interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireIncludeJurorsOnCall,
        AbstractReport.Validators.RequireIncludePanelMembers,
        AbstractReport.Validators.RequireRespondedJurorsOnly {
    }
}
