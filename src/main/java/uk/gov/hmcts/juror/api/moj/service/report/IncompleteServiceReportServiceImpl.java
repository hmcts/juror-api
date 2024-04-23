package uk.gov.hmcts.juror.api.moj.service.report;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.report.IIncompleteServiceRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("PMD.LawOfDemeter")
public class IncompleteServiceReportServiceImpl implements IncompleteServiceReportService {
    private final CourtLocationRepository courtLocationRepository;
    private final IIncompleteServiceRepository incompleteServiceRepository;

    @Override
    @Transactional(readOnly = true)
    public AbstractReportResponse<List<Map<String, Object>>> viewIncompleteServiceReport(String location,
                                                                                         LocalDate cutOffDate) {

        // check if user has access to the location
        final CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(location,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        final List<Tuple> incompleteServiceResult =
            incompleteServiceRepository.getIncompleteServiceByLocationAndDate(location, cutOffDate);

        final AbstractReportResponse<List<Map<String, Object>>> response = new AbstractReportResponse<>();
        final Map<String, AbstractReportResponse.DataTypeValue> headings = new ConcurrentHashMap<>();

        setHeadings(cutOffDate, courtLocation, incompleteServiceResult, response, headings);

        final List<AbstractReportResponse.TableData.Heading> TableDataHeadings = new ArrayList<>();

        setTableHeadings(TableDataHeadings);

        response.setTableData(new StandardReportResponse.TableData());
        response.getTableData().setHeadings(TableDataHeadings);
        response.getTableData().setData(getTableData(incompleteServiceResult));

        return response;
    }

    private List<Map<String, Object>> getTableData(List<Tuple> incompleteServiceResult) {

        final List<Map<String, Object>> data = new ArrayList<>();

        incompleteServiceResult.stream().forEach(tuple -> {
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("juror_number", tuple.get(0, String.class));
            dataMap.put("first_name", tuple.get(1, String.class));
            dataMap.put("last_name", tuple.get(2, String.class));
            dataMap.put("pool_number", tuple.get(3, String.class));
            dataMap.put("next_attendance_date", tuple.get(4, LocalDate.class).toString());
            data.add(dataMap);
        });

        return data;
    }

    private static void setTableHeadings(List<AbstractReportResponse.TableData.Heading> tableDataHeadings) {
        tableDataHeadings.add(StandardReportResponse.TableData.Heading.builder()
            .id("juror_number")
            .name("Juror Number")
            .dataType("String")
            .headings(null)
            .build());

        tableDataHeadings.add(StandardReportResponse.TableData.Heading.builder()
            .id("first_name")
            .name("First Name")
            .dataType("String")
            .headings(null)
            .build());

        tableDataHeadings.add(StandardReportResponse.TableData.Heading.builder()
            .id("last_name")
            .name("Last Name")
            .dataType("String")
            .headings(null)
            .build());

        tableDataHeadings.add(StandardReportResponse.TableData.Heading.builder()
            .id("pool_number")
            .name("Pool Number")
            .dataType("String")
            .headings(null)
            .build());

        tableDataHeadings.add(StandardReportResponse.TableData.Heading.builder()
            .id("next_attendance_date")
            .name("Next attendance date")
            .dataType("LocalDate")
            .headings(null)
            .build());
    }

    private void setHeadings(LocalDate cutOffDate, CourtLocation courtLocation,
                                  List<Tuple> incompleteServiceResult, AbstractReportResponse<?> response,
                                  Map<String, AbstractReportResponse.DataTypeValue> headings) {

        headings.put("cut_off_date", AbstractReportResponse.DataTypeValue.builder().displayName("Cut-off Date")
            .dataType(LocalDate.class.getSimpleName()).value(cutOffDate.toString()).build());

        headings.put("total_incomplete_service", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total Incomplete Service")
            .dataType(Integer.class.getSimpleName()).value(incompleteServiceResult.size()).build());

        headings.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
            .dataType(String.class.getSimpleName()).value(StringUtils.capitalize(
                StringUtils.lowerCase(courtLocation.getName())) + " (" + courtLocation.getLocCode() + ")").build());

        headings.put("report_created", AbstractReportResponse.DataTypeValue.builder().displayName("Report Created")
            .dataType(LocalDateTime.class.getSimpleName()).value(LocalDateTime.now()).build());

        response.setHeadings(headings);
    }

}
