package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtCatchmentStatusDto;
import uk.gov.hmcts.juror.api.bureau.domain.CourtCatchmentEntity;
import uk.gov.hmcts.juror.api.bureau.domain.CourtCatchmentRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.QPool;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class CourtCatchmentServiceImpl implements CourtCatchmentService {

    private final CourtCatchmentRepository courtCatchmentRepository;
    private final PoolRepository poolRepository;
    private final JurorResponseRepository jurorResponseRepository;


    @Autowired
    public CourtCatchmentServiceImpl(final CourtCatchmentRepository courtCatchmentRepository,
                                     final PoolRepository poolRepository,
                                     final JurorResponseRepository responseRepository) {
        Assert.notNull(courtCatchmentRepository, "CourtCatchmentRepository cannot be null");
        this.courtCatchmentRepository = courtCatchmentRepository;
        this.poolRepository = poolRepository;
        this.jurorResponseRepository = responseRepository;
    }

    @Override
    @Transactional
    public CourtCatchmentStatusDto CourtCatchmentFinder(String jurorNumber) {

        Optional<Pool> optPool = poolRepository.findOne(QPool.pool.jurorNumber.eq(jurorNumber));
        Pool poolDetails = optPool.isPresent()
            ?
            optPool.get()
            :
                null;
        // validate credentials.
        if (poolDetails == null) {
            log.info("Could not find juror using juror number {}", jurorNumber);
            throw new InvalidJurorCredentialsException("Juror not found");
        }

        JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);

        if (jurorResponse == null) {
            log.info("Could not find juror response for  juror number {}", jurorNumber);
            throw new InvalidJurorCredentialsException("Response not found");
        }

        CourtCatchmentStatusDto courtCatchmentStatusDto = new CourtCatchmentStatusDto();

        if (jurorResponse.getPostcode() != null
            && !jurorResponse.getPostcode().equals("")) {

            String postCode = jurorResponse.getPostcode();
            log.info("Post code from juror response {}", postCode);

            //Get new courtCatchmentCode

            String responseCourtCatchmentCode = getCourtCatchmentCode(postCode);

            if (responseCourtCatchmentCode == null) {
                log.error("court Catchment Code for pool {} is null", poolDetails.getCourt().getLocCode());
                throw new LocationCodeNotFoundException("loc_code not found");
            } else {
                log.debug("court Catchment Code for response {}", responseCourtCatchmentCode);
                log.debug("court Catchment Code for pool {}", poolDetails.getCourt().getLocCode());

                if (responseCourtCatchmentCode.equals(poolDetails.getCourt().getLocCode())) {
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

        final ArrayList<CourtCatchmentEntity> courtCatchmentEntities =
            Lists.newArrayList(courtCatchmentRepository.findAll());

        for (CourtCatchmentEntity courtCatchmentEntity : courtCatchmentEntities) {
            if (postCode.contains(courtCatchmentEntity.getPostCode())) {
                courtCode = courtCatchmentEntity.getCourtCode();

            }
        }

        return courtCode;
    }

    /**
     * Failed to validate juror credentials.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    class InvalidJurorCredentialsException extends RuntimeException {
        InvalidJurorCredentialsException(String message) {
            super(message);
        }
    }


    /**
     * Failed to validate juror credentials.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    class LocationCodeNotFoundException extends RuntimeException {
        LocationCodeNotFoundException(String message) {
            super(message);
        }
    }


}


