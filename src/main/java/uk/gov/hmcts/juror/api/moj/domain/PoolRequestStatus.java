package uk.gov.hmcts.juror.api.moj.domain;

/**
 * Defines the status of a pool request, where REQUESTED indicates the pool has been requested by a court
 * (or the bureau) but has not yet been populated with potential jurors. CREATED indicates the bureau pool record exists
 * and the bureau have started the summonsing process (populating the pool with potential jurors). There is a scenario
 * where court deferrals may be added to a pool during the 'Request a Pool' phase - however the pool is deemed to still
 * be in a REQUESTED state at this point, and only ever becomes CREATED when the bureau begins summonsing citizens
 */
public enum PoolRequestStatus {

    REQUESTED,
    CREATED

}
