package uk.gov.hmcts.juror.api.moj.report.bespoke;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceMod;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;
import uk.gov.hmcts.juror.api.moj.service.report.IReport;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractJurorAmendmentReport implements IReport {

    protected final JurorAuditService jurorAuditService;
    protected final JurorServiceMod jurorService;
    protected final UserService userService;

    public enum Changed {
        LAST_NAME(Juror::getLastName),
        DATE_OF_BIRTH(Juror::getDateOfBirth),
        ADDRESS(Juror::getCombinedAddressExcludingPostcode),
        BANK_ACCOUNT_HOLDER_NAME(Juror::getBankAccountNumber),
        SORT_CODE(Juror::getSortCode),
        BANK_ACCOUNT_NUMBER(Juror::getBankAccountNumber),
        FIRST_NAME(Juror::getFirstName),
        POSTCODE(Juror::getPostcode);

        private final Function<Juror, Object> getValueFunction;


        Changed(Function<Juror, Object> getValueFunction) {
            this.getValueFunction = getValueFunction;
        }


        public static void updateCurrentValues(Map<Changed, Object> currentChangedValueLog, Juror currentJurorValue) {
            for (Changed changed : Changed.values()) {
                currentChangedValueLog.put(changed, changed.getValue(currentJurorValue));
            }
        }

        public static Collection<? extends JurorAmendmentReportRow> getChanges(
            UserService userService,
            Map<Changed, Object> currentChangedValueLog, Juror juror) {
            List<JurorAmendmentReportRow> changes = new ArrayList<>();
            for (Changed changed : Changed.values()) {
                Object currentValue = currentChangedValueLog.get(changed);
                Object newValue = changed.getValue(juror);
                if (currentValue == null && newValue == null) {
                    continue;
                }
                if (currentValue == null || !currentValue.equals(newValue)) {
                    changes.add(
                        new JurorAmendmentReportRow(
                            juror.getJurorNumber(),
                            changed,
                            formatObject(newValue),
                            formatObject(currentValue),
                            juror.getLastUpdate(),
                            userService.findUserByUsername(juror.getLastModifiedBy()).getName()
                        ));
                    currentChangedValueLog.put(changed, newValue);
                }
            }
            return changes;
        }

        private static Object formatObject(Object value) {
            if (value instanceof LocalDateTime dateTime) {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
            }
            if (value instanceof LocalDate date) {
                return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            }
            return value;
        }

        private Object getValue(Juror currentJurorValue) {
            return getValueFunction.apply(currentJurorValue);
        }
    }

    protected AbstractReportResponse.TableData<List<JurorAmendmentReportRow>> getTableData(
        List<String> jurorNumbers) {
        return getTableDataAudits(jurorAuditService.getAllAuditsFor(jurorNumbers));

    }

    protected AbstractReportResponse.TableData<List<JurorAmendmentReportRow>>
    getTableDataAudits(List<Juror> jurorAudits) {


        Map<String, List<Juror>> jurorNumberToJurorMap = jurorAudits
            .stream()
            .collect(Collectors.groupingBy(Juror::getJurorNumber));

        List<JurorAmendmentReportRow> changedData = new ArrayList<>();
        jurorNumberToJurorMap.forEach((jurorNumber, jurors) -> {
            changedData.addAll(getChangesFromJurorAudits(jurorNumber, jurors));
        });
        StandardReportResponse.TableData<List<JurorAmendmentReportRow>> tableData =
            new StandardReportResponse.TableData<>();

        List<AbstractReportResponse.TableData.Heading> headings = new ArrayList<>();
        headings.add(AbstractReportResponse.TableData.Heading.builder()
            .id("juror_number")
            .name("Juror Number")
            .dataType(String.class.getSimpleName())
            .build());
        headings.add(AbstractReportResponse.TableData.Heading.builder()
            .id("changed")
            .name("Changed")
            .dataType(String.class.getSimpleName())
            .build());

        headings.add(AbstractReportResponse.TableData.Heading.builder()
            .id("from")
            .name("From")
            .dataType(String.class.getSimpleName())
            .build());
        headings.add(AbstractReportResponse.TableData.Heading.builder()
            .id("to")
            .name("To")
            .dataType(String.class.getSimpleName())
            .build());
        headings.add(AbstractReportResponse.TableData.Heading.builder()
            .id("changed_on")
            .name("Changed on")
            .dataType(LocalDateTime.class.getSimpleName())
            .build());
        headings.add(AbstractReportResponse.TableData.Heading.builder()
            .id("changed_by")
            .name("Changed By")
            .dataType(String.class.getSimpleName())
            .build());
        tableData.setHeadings(headings);
        tableData.setData(changedData);
        return tableData;
    }

    private Collection<JurorAmendmentReportRow> getChangesFromJurorAudits(String jurorNumber, List<Juror> jurors) {
        jurors.sort(Comparator.comparing(Juror::getLastUpdate));

        Juror currentJurorValue = jurorService.getJurorFromJurorNumber(jurorNumber);
        Map<Changed, Object> currentChangedValueLog = new HashMap<>();
        Changed.updateCurrentValues(currentChangedValueLog, currentJurorValue);


        List<JurorAmendmentReportRow> changes = new ArrayList<>();
        jurors.forEach(juror -> changes.addAll(Changed.getChanges(userService, currentChangedValueLog, juror)));

        return changes;
    }


    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JurorAmendmentReportRow {
        private String jurorNumber;
        private Changed changed;
        private Object from;
        private Object to;
        @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
        private LocalDateTime changedOn;
        private String changedBy;
    }


}
