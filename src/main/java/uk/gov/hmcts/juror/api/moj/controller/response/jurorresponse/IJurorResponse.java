package uk.gov.hmcts.juror.api.moj.controller.response.jurorresponse;

public interface IJurorResponse {
    void setCurrentOwner(String owner);

    String getJurorNumber();
}
