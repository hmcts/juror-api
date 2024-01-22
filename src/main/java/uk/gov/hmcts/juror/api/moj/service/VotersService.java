package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.Voters;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;

public interface VotersService {
    Map<String,String> getVoters(String owner, PoolCreateRequestDto poolCreateRequestDto) throws SQLException;

    Map<String,String> getVotersForCoronerPool(String postcode, int number, String locCode) throws SQLException;

    void markVoterAsSelected(Voters voter, Date attendanceDate);
}
