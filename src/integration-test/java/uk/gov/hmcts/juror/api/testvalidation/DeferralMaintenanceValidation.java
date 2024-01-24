package uk.gov.hmcts.juror.api.testvalidation;

import org.assertj.core.api.Assertions;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;

import java.time.LocalDate;
import java.util.List;

public final class DeferralMaintenanceValidation {
    private DeferralMaintenanceValidation() {
    }

    public static void validateGetDeferralOptionsByJurorNumberAndCourtLocationCode(
        List<DeferralOptionsDto.OptionSummaryDto> optionsSummary) {

        /*
         * first option - requested deferral date is 2023-05-30 (Tuesday)
         * expect 2 active pools to be returned as available deferral options
         */
        DeferralOptionsDto.OptionSummaryDto firstOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst()
            .orElse(null);
        Assertions.assertThat(firstOption)
            .as("Preferred deferral date of 30-05-2023 is a Tuesday, expect the deferral options to check "
                + "for week commencing 2023-05-29 (the previous Monday)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = firstOption.getDeferralOptions();
        Assertions.assertThat(deferralOptions.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-05-29, pool number: 415220401
        DeferralOptionsDto.DeferralOptionDto availablePool1 = deferralOptions.stream()
            .filter(pool -> "415220401".equals(pool.getPoolNumber()))
            .findFirst()
            .orElse(null);
        Assertions.assertThat(availablePool1)
            .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
            .isNotNull();
        Assertions.assertThat(availablePool1.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 5, 30));
        Assertions.assertThat(availablePool1.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(2);
        Assertions.assertThat(availablePool1.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.SURPLUS);
        // get second deferral option for w/c 2023-05-29, pool number: 415220502
        DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
            .filter(pool -> "415220502".equals(pool.getPoolNumber()))
            .findFirst()
            .orElse(null);
        Assertions.assertThat(availablePool2)
            .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
            .isNotNull();
        Assertions.assertThat(availablePool2.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 1));
        Assertions.assertThat(availablePool2.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(2);
        Assertions.assertThat(availablePool2.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        /*
         * second option - requested deferral date is 2023-06-12 (Monday)
         * expect 1 active pool to be returned as an available deferral option
         */
        DeferralOptionsDto.OptionSummaryDto secondOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst()
            .orElse(null);
        Assertions.assertThat(secondOption)
            .as("Preferred deferral date of 2023-06-12 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-12 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions2 = secondOption.getDeferralOptions();
        Assertions.assertThat(deferralOptions2.size())
            .as("Expect one available active pool option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-12, pool number: 415220503
        DeferralOptionsDto.DeferralOptionDto availablePool3 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220503"))
            .findFirst()
            .orElse(null);
        Assertions.assertThat(availablePool3)
            .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
            .isNotNull();
        Assertions.assertThat(availablePool3.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        Assertions.assertThat(availablePool3.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(4);
        Assertions.assertThat(availablePool3.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        /*
         * third option - requested deferral date is 2023-06-26 (Monday)
         * expect no active pools to be available as deferral options - use deferral maintenance
         */
        DeferralOptionsDto.OptionSummaryDto thirdOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 7, 3)))
            .findFirst()
            .orElse(null);
        Assertions.assertThat(thirdOption)
            .as("Preferred deferral date of 2023-07-03 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-07-03 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions3 = thirdOption.getDeferralOptions();
        Assertions.assertThat(deferralOptions3.size())
            .as("Expect one available deferral option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-07-3, no available pools - deferral maintenance
        DeferralOptionsDto.DeferralOptionDto deferralMaintenance = deferralOptions3.stream()
            .findFirst()
            .orElse(null);
        validateDeferralMaintenanceOptions(deferralMaintenance, 0);
    }

    public static void validateDeferralMaintenanceOptions(DeferralOptionsDto.DeferralOptionDto deferralMaintenance,
                                                          int expectedUtilisation) {
        Assertions.assertThat(deferralMaintenance)
            .as("Expect a valid deferral option to be returned for deferral maintenance")
            .isNotNull();
        Assertions.assertThat(deferralMaintenance.getPoolNumber())
            .as("Expect no pool to exists so no data to be populated for pool number")
            .isNull();
        Assertions.assertThat(deferralMaintenance.getServiceStartDate())
            .as("Expect no pool to exists so no data to be populated for service start date")
            .isNull();
        Assertions.assertThat(deferralMaintenance.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given deferral date")
            .isEqualTo(expectedUtilisation);
        Assertions.assertThat(deferralMaintenance.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given deferral date")
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);
    }
}