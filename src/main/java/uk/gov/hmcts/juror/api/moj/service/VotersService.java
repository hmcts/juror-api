package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.Voters;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public interface VotersService {

    List<Voters> getVotersForCoronerPool(String postcode, int number, String locCode);

    List<Voters> getVoters(PoolCreateRequestDto poolCreateRequestDto);

    List<Voters> getVoters(List<String> postcodes,
                           int citizensToSummon,
                           LocalDate attendanceDate,
                           String locCode,
                           boolean isCoroners);

    void markVotersAsSelected(List<Voters> voters, Date attendanceDate);
}
