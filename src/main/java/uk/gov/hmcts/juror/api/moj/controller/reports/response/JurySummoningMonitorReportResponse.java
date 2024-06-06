package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@SuppressWarnings("PMD.TooManyFields")
public class JurySummoningMonitorReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;

    private int totalJurorsNeeded;
    private int bureauDeferralsIncluded;
    private int bureauToSupply;
    private int initiallySummoned;
    private double ratio;
    private int additionalSummonsIssued;
    private int reminderLettersIssued;
    private int totalConfirmedJurors;
    private int deferralsRefused;
    private int excusalsRefused;
    private int totalUnavailable;
    private int nonResponded;
    private int undeliverable;
    private int awaitingInformation;
    private int disqualifiedPoliceCheck;
    private int disqualifiedOther;
    private int deferred;
    private int postponed;
    private int excused;
    private int bereavement;
    private int carer;
    private int childcare;
    private int cjsEmployment;
    private int criminalRecord;
    private int deceased;
    private int deferredByCourt;
    private int excusedByBureau;
    private int financialHardship;
    private int forces;
    private int holiday;
    private int ill;
    private int languageDifficulties;
    private int medical;
    private int mentalHealth;
    private int movedFromArea;
    private int other;
    private int personalEngagement;
    private int postponementOfService;
    private int recentlyServed;
    private int religiousReasons;
    private int student;
    private int travellingDifficulties;
    private int workRelated;

    public int getTotalExcused() {
        return bereavement + carer + childcare + cjsEmployment + criminalRecord + deceased + deferredByCourt
            + excusedByBureau + financialHardship + forces + holiday + ill + languageDifficulties + medical
            + mentalHealth + movedFromArea + other + personalEngagement + postponementOfService + recentlyServed
            + religiousReasons + student + travellingDifficulties + workRelated;
    }

}