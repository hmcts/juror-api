package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.Voters;
import uk.gov.hmcts.juror.api.moj.repository.VotersRepository;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VotersServiceImpl implements VotersService {
    static final int AGE_LOWER_SP_ID = 101;
    static final int AGE_UPPER_SP_ID = 100;

    @NonNull
    private final VotersRepository votersRepository;
    @NonNull
    private final SystemParameterRepository systemParameterRepository;

    /* function returns a map of juror number and flag values */
    @Override
    public Map<String,String> getVoters(String owner, PoolCreateRequestDto poolCreateRequestDto) throws SQLException {

        LocalDate attendanceDate = poolCreateRequestDto.getStartDate();
        String postcodeString = createPostCodeString(poolCreateRequestDto.getPostcodes());

        //Determine Max and Min DOB allowed based on attendance date
        LocalDate maxDateOfBirth = calculateDateLimit(attendanceDate, AGE_LOWER_SP_ID, 18);
        LocalDate minDateOfBirth = calculateDateLimit(attendanceDate, AGE_UPPER_SP_ID, 76);

        List<String> voters = votersRepository.callGetVoters(poolCreateRequestDto.getCitizensToSummon(),
                minDateOfBirth,
                maxDateOfBirth,
                poolCreateRequestDto.getCatchmentArea(),
                postcodeString,
                "N");
        log.info("Obtained {} records from voters table for pool {} ", voters.size(),
                poolCreateRequestDto.getPoolNumber());

        Map<String, String> voterMap = new HashMap<>();

        voters.forEach(voter -> voterMap.put(voter.split(",")[0], voter.split(",")[1]));

        return voterMap;
    }

    @Override
    public Map<String,String> getVotersForCoronerPool(String postcode, int number,
                                                      String locCode) throws SQLException {

        LocalDate currentDate = LocalDate.now();

        //Determine Max and Min DOB allowed based on attendance date
        LocalDate maxDateOfBirth = calculateDateLimit(currentDate, AGE_LOWER_SP_ID, 18);
        LocalDate minDateOfBirth = calculateDateLimit(currentDate, AGE_UPPER_SP_ID, 76);

        List<String> voters = votersRepository.callGetVoters(
            number,
            minDateOfBirth,
            maxDateOfBirth,
            locCode,
            postcode, "C");

        log.info("Obtained {} records from voters table for coroner pool ", voters.size());

        Map<String, String> votersMap = new HashMap<>();

        voters.forEach(voter -> votersMap.put(voter.split(",")[0], voter.split(",")[1]));
        return votersMap;
    }

    @Override
    public void markVoterAsSelected(Voters voter, Date attendanceDate) {

        voter.setDateSelected1(attendanceDate);
        votersRepository.save(voter);
        log.info("Voter with Juror number {} has been selected for a pool.", voter.getJurorNumber());
    }

    private String createPostCodeString(List<String> postcodes) {

        StringBuilder postcodeString = new StringBuilder();

        postcodes.forEach(postcode -> postcodeString.append(postcode).append(","));
        //trim the last comma from the string buffer
        return postcodeString.substring(0, postcodeString.length() - 1);

    }

    private LocalDate calculateDateLimit(LocalDate attendanceDate, int systemParamId, int defaultAge) {
        Optional<SystemParameter> optJurAgeParam = systemParameterRepository.findById(systemParamId);
        final SystemParameter jurorAgeParameter = optJurAgeParam.orElse(null);
        int jurorAgeLimit = defaultAge;
        if (jurorAgeParameter != null) {
            try {
                jurorAgeLimit = Integer.parseInt(jurorAgeParameter.getSpValue());
            } catch (Exception e) {
                log.error("Failed to parse age constraint parameter from database: Using default {}",
                    jurorAgeLimit, e);
            }
        }
        return attendanceDate.minusYears(jurorAgeLimit);
    }

}
