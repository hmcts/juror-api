package uk.gov.hmcts.juror.api.moj.report.bespoke;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
        BANK_ACCOUNT_HOLDER_NAME(Juror::getBankAccountName),
        SORT_CODE(Juror::getSortCode),
        BANK_ACCOUNT_NUMBER(Juror::getBankAccountNumber),
        FIRST_NAME(Juror::getFirstName),
        POSTCODE(Juror::getPostcode);

        private final Function<Juror, Object> getValueFunction;


        Changed(Function<Juror, Object> getValueFunction) {
            this.getValueFunction = getValueFunction;
        }


        public static Collection<JurorAmendmentReportRow> getChanges(
            UserService userService,
            Juror afterChangeJuror, Juror beforeChangeJuror) {
            List<JurorAmendmentReportRow> changes = new ArrayList<>();
            for (Changed changed : Changed.values()) {
                Object afterChangeValue = changed.getValue(afterChangeJuror);
                Object beforeChangeValue = changed.getValue(beforeChangeJuror);
                if (afterChangeValue == null && beforeChangeValue == null) {
                    continue;
                }
                if (afterChangeValue == null || !afterChangeValue.equals(beforeChangeValue)) {
                    changes.add(
                        new JurorAmendmentReportRow(
                            afterChangeJuror.getJurorNumber(),
                            changed,
                            formatObject(beforeChangeValue),
                            formatObject(afterChangeValue),
                            afterChangeJuror.getLastUpdate(),
                            userService.findUserByUsername(afterChangeJuror.getLastModifiedBy()).getName()
                        ));
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
        return getTableDataAudits(false, jurorAudits);
    }

    protected AbstractReportResponse.TableData<List<JurorAmendmentReportRow>>
        getTableDataAudits(boolean includeOneAuditEitherSide, List<Juror> jurorAudits) {
        Map<String, List<Juror>> jurorNumberToJurorMap = jurorAudits
            .stream()
            .collect(Collectors.groupingBy(Juror::getJurorNumber));

        List<JurorAmendmentReportRow> changedData = new ArrayList<>();
        jurorNumberToJurorMap.forEach((jurorNumber, jurors) -> changedData.addAll(
            getChangesFromJurorAudits(includeOneAuditEitherSide, jurorNumber, jurors)));
        changedData.sort(Comparator.comparing(JurorAmendmentReportRow::getChangedOn).reversed());


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
        StandardReportResponse.TableData<List<JurorAmendmentReportRow>> tableData =
            new StandardReportResponse.TableData<>();
        tableData.setHeadings(headings);
        tableData.setData(changedData);
        return tableData;
    }

    private Collection<JurorAmendmentReportRow> getChangesFromJurorAudits(boolean includeOneAuditEitherSide,
                                                                          String jurorNumber,
                                                                          List<Juror> jurors) {
        jurors.sort(Comparator.comparing(Juror::getLastUpdate).reversed());
        if (jurors.isEmpty()) {
            return List.of();
        }

        Juror currentJurorValue = null;
        if (includeOneAuditEitherSide) {
            Juror previousJurorAudit = jurorAuditService.getPreviousJurorAudit(jurors.get(jurors.size() - 1));
            if (previousJurorAudit != null) {
                jurors.add(previousJurorAudit);
            }
        } else {
            currentJurorValue = jurorService.getJurorFromJurorNumber(jurorNumber);
        }

        List<JurorAmendmentReportRow> changes = new ArrayList<>();
        for (Juror juror : jurors) {
            if (currentJurorValue == null) {
                currentJurorValue = juror;
                continue;
            }
            changes.addAll(Changed.getChanges(userService, currentJurorValue, juror));
            currentJurorValue = juror;
        }
        return changes;
    }


    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
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
