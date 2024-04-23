package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class IncompleteServiceReport extends AbstractStandardReport {

    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public IncompleteServiceReport(CourtLocationRepository courtLocationRepository) {
        super(
            QJuror.juror,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_BY_JP,
            DataType.NEXT_ATTENDANCE_DATE);
        this.courtLocationRepository = courtLocationRepository;
        isCourtUserOnly();
        addAuthenticationConsumer(request -> checkOwnership(request.getLocCode(), false));
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query
            .where(QJurorPool.jurorPool.nextDate.loe(request.getDate()))
            .where(QJurorPool.jurorPool.owner.eq(request.getLocCode()))
            .where(QJurorPool.jurorPool.status.status.in(List.of(2, 3, 4)))
            .orderBy(QJuror.juror.jurorNumber.asc())
            .fetch();
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new HashMap<>();
        map.put("total_incomplete_service", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total incomplete service")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().size())
            .build());


        map.put("cut_off_date", AbstractReportResponse.DataTypeValue.builder().displayName("Cut-off Date")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getDate())).build());


        courtLocationRepository.findByLocCode(request.getLocCode()).ifPresent(courtLocation -> {
            map.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
                .dataType(String.class.getSimpleName())
                .value(StringUtils.capitalize(StringUtils.lowerCase(courtLocation.getName()))
                    + " (" + courtLocation.getLocCode() + ")")
                .build());
        });

        map.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
            .dataType(String.class.getSimpleName()).value(StringUtils.capitalize(
                StringUtils.lowerCase(courtLocation.getName())) + " (" + courtLocation.getLocCode() + ")").build());

        return map;
    }

    @Override
    public Class<?> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireDate,
        Validators.RequireLocCode {

    }
}
