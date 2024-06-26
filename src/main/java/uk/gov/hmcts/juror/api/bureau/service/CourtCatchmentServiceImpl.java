package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtCatchmentStatusDto;
import uk.gov.hmcts.juror.api.moj.domain.CourtCatchmentArea;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.CourtCatchmentAreaRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtCatchmentServiceImpl implements CourtCatchmentService {

    private final CourtCatchmentAreaRepository courtCatchmentAreaRepository;
    private final JurorPoolService jurorPoolService;
    private final JurorResponseService jurorResponseService;


    @Override
    @Transactional
    public CourtCatchmentStatusDto courtCatchmentFinder(String jurorNumber) {
        JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
        JurorCommonResponseRepositoryMod.AbstractResponse jurorResponse =
            jurorResponseService.getCommonJurorResponse(jurorNumber);

        CourtCatchmentStatusDto courtCatchmentStatusDto = new CourtCatchmentStatusDto();

        if (jurorResponse.getPostcode() != null && !jurorResponse.getPostcode().isEmpty()) {

            String postCode = jurorResponse.getPostcode();
            log.info("Post code from juror response {}", postCode);

            //Get new courtCatchmentCode
            String responseCourtCatchmentCode = getCourtCatchmentCode(postCode);

            if (responseCourtCatchmentCode == null) {
                log.error("court Catchment Code for pool {} is null", jurorPool.getCourt().getLocCode());
                throw new LocationCodeNotFoundException("loc_code not found");
            } else {
                log.debug("court Catchment Code for response {}", responseCourtCatchmentCode);
                log.debug("court Catchment Code for pool {}", jurorPool.getCourt().getLocCode());

                if (responseCourtCatchmentCode.equals(jurorPool.getCourt().getLocCode())) {
                    log.debug("Pool and Response Loc_Code are same, No change");
                    courtCatchmentStatusDto.setCourtCatchmentStatus("Unchanged");
                } else {
                    log.debug("Pool and Response Loc_Code are different, Changed");
                    courtCatchmentStatusDto.setCourtCatchmentStatus("Changed");
                }

            }
        } else {
            throw new InvalidJurorCredentialsException("Post code missing from Juror response");
        }
        return courtCatchmentStatusDto;
    }


    /**
     * Returns the court code based on the catchment area.
     *
     * @param postCode postcode
     * @return courtCode
     */

    private String getCourtCatchmentCode(String postCode) {

        String courtCode = null;

        final ArrayList<CourtCatchmentArea> courtCatchmentEntities =
            Lists.newArrayList(courtCatchmentAreaRepository.findAll());

        for (CourtCatchmentArea courtCatchmentEntity : courtCatchmentEntities) {
            if (postCode.contains(courtCatchmentEntity.getPostcode())) {
                courtCode = courtCatchmentEntity.getLocCode();
            }
        }

        return courtCode;
    }

    /**
     * Failed to validate juror credentials.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class InvalidJurorCredentialsException extends RuntimeException {
        InvalidJurorCredentialsException(String message) {
            super(message);
        }
    }


    /**
     * Failed to validate juror credentials.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class LocationCodeNotFoundException extends RuntimeException {
        LocationCodeNotFoundException(String message) {
            super(message);
        }
    }
}


