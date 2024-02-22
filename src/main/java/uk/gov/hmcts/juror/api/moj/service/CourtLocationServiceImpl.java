package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.RevisionSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtQueriesRepository;
import uk.gov.hmcts.juror.api.moj.utils.RevisionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.domain.CourtLocationQueries.filterByLocCodes;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtLocationServiceImpl implements CourtLocationService {

    private final CourtLocationRepository courtLocationRepository;

    @NonNull
    private final CourtQueriesRepository courtQueriesRepository;


    /**
    * Retrieves court location records from the database (using the JWT to filter out court locations the current user
    * does not own or have access to) and populates a lightweight DTO of selected properties for each record.
    *
    * @return a Data Transfer Object containing a list of court locations with a selection of court location properties
    *       filtered based on the currently logged-in user's access.
    */
    @Override
    @Transactional(readOnly = true)
    public CourtLocationListDto buildCourtLocationDataResponse(BureauJWTPayload payload) {
        log.trace("Enter buildCourtLocationDataResponse");

        Iterable<CourtLocation> courtLocationRecords;
        if (JurorDigitalApplication.JUROR_OWNER.equals(payload.getOwner())) {
            courtLocationRecords = courtLocationRepository.findAll();
        } else {
            courtLocationRecords = courtLocationRepository.findAll(filterByLocCodes(payload.getStaff().getCourts()));
        }

        return new CourtLocationListDto(mapCourtLocationsToDto(courtLocationRecords));
    }

    /**
     * Retrieves all court location records from the database (regardless of the currently logged-in user's
     * access/permissions.
     *
     * @return a Data Transfer Object containing an unfiltered list of court locations with a selection of court
     *      location properties.
     */
    @Override
    public CourtLocationListDto buildAllCourtLocationDataResponse() {
        log.trace("Enter buildCourtLocationDataResponse");
        return new CourtLocationListDto(mapCourtLocationsToDto(courtLocationRepository.findAll()));
    }

    @Override
    public CourtLocation getCourtLocation(String locCode) {
        return courtLocationRepository.findByLocCode(locCode).orElse(null);
    }

    @Override
    public CourtLocation getCourtLocationByName(String locName) {
        return courtLocationRepository.findByName(locName).orElse(null);
    }

    @Override
    public List<CourtLocationDataDto> getCourtLocationsByPostcode(@Length(max = 4) String postcode) {
        return courtQueriesRepository.getCourtDetailsFilteredByPostcode(postcode);
    }

    CourtLocation getCourtLocationFromEffectiveFromDate(String locCode, LocalDate date) {

        return RevisionUtil.findRevisionsSorted(courtLocationRepository, locCode, RevisionSort.desc())
            .filter(courtLocationRevision -> {
                LocalDate effectiveFrom = courtLocationRevision.getEntity().getRatesEffectiveFrom();
                return effectiveFrom == null || !date.isBefore(effectiveFrom);
            })
            .findFirst().orElseThrow(() -> new MojException.NotFound(
                "No court location rates are active on date: " + date
                    + " for court " + locCode, null))
            .getEntity();
    }

    @Override
    public CourtRates getCourtRates(String locCode, LocalDate date) {
        CourtLocation courtLocation = getCourtLocationFromEffectiveFromDate(locCode, date);

        return CourtRates.builder()
            .carRate0Passengers(courtLocation.getCarMileageRatePerMile0Passengers())
            .carRate1Passenger(courtLocation.getCarMileageRatePerMile1Passengers())
            .carRate2OrMorePassenger(courtLocation.getCarMileageRatePerMile2OrMorePassengers())
            .motorcycleRate0Passenger(courtLocation.getMotorcycleMileageRatePerMile0Passengers())
            .motorcycleRate1OrMorePassenger(courtLocation.getMotorcycleMileageRatePerMile1Passengers())
            .bicycleRate0OrMorePassenger(courtLocation.getBikeRate())
            .subsistenceRateStandard(courtLocation.getSubsistenceRateStandard())
            .subsistenceRateLongDay(courtLocation.getSubsistenceRateLongDay())
            .financialLossHalfDayLimit(courtLocation.getLimitFinancialLossHalfDay())
            .financialLossFullDayLimit(courtLocation.getLimitFinancialLossFullDay())
            .financialLossHalfDayLongTrialLimit(courtLocation.getLimitFinancialLossHalfDayLongTrial())
            .financialLossFullDayLongTrialLimit(courtLocation.getLimitFinancialLossFullDayLongTrial())
            .publicTransportSoftLimit(courtLocation.getPublicTransportSoftLimit())
            .build();
    }

    @Override
    public BigDecimal getYieldForCourtLocation(String locCode) {
        return courtLocationRepository.findByLocCode(locCode)
            .map(CourtLocation::getYield)
            .orElse(BigDecimal.ZERO);
    }

    @Override
    public boolean getVotersLock(String locCode) {

        Optional<CourtLocation> courtLocationRecord = courtLocationRepository.findByLocCode(locCode);
        int votersLock = 0;

        if (courtLocationRecord.isPresent()) {

            CourtLocation courtLocation = courtLocationRecord.get();
            votersLock = courtLocation.getVotersLock();

            if (votersLock == 0) {
                courtLocation.setVotersLock(1);
                courtLocationRepository.save(courtLocation);
                votersLock = 1;
            } else {
                return false;
            }
        }
        return votersLock > 0;
    }


    @Override
    public boolean releaseVotersLock(String locCode) {

        Optional<CourtLocation> courtLocationRecord = courtLocationRepository.findByLocCode(locCode);

        if (courtLocationRecord.isPresent()) {
            CourtLocation courtLocation = courtLocationRecord.get();
            int votersLock = courtLocation.getVotersLock();

            if (votersLock == 1) {
                courtLocation.setVotersLock(0);
                courtLocationRepository.save(courtLocation);
            } else {
                return false;
            }
        }
        return true;
    }

    private List<CourtLocationDataDto> mapCourtLocationsToDto(
        Iterable<CourtLocation> courtLocationList) {
        log.trace("Enter mapCourtLocationsToDto");
        List<CourtLocationDataDto> courtLocations = new ArrayList<>();

        courtLocationList.forEach(courtLocation -> {
            log.debug(String.format("Mapping court location: %s - %s to DTO",
                courtLocation.getLocCode(), courtLocation.getName()
            ));
            CourtLocationDataDto courtLocationData =
                new CourtLocationDataDto(courtLocation);

            courtLocations.add(courtLocationData);
            log.trace(String.format("Court location data added: %s", courtLocationData));
        });

        log.debug(String.format("Court Location data for %d courts retrieved", courtLocations.size()));
        log.trace("Exit mapCourtLocationsToDto");
        return courtLocations;
    }
}
