package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;

import java.util.Map;

/**
 * Service for operations to determine payload for Juror Comms Notification Templates.
 */
public interface JurorCommsNotifyPayLoadService {

    /**
     * Generates a Map containing key:value pairs of data elements to be supplied to the Notify Service.
     * Currently, specific to extracting data from print_files.detail_rec (detailData)
     *
     * @param templateId template for which the payload is to be assembled for.
     * @param detailData source of data.
     * @param pool       pool details for juror.
     * @return Map of key values pairs of data elements for the template.
     */
    Map<String, String> generatePayLoadData(String templateId, String detailData, Pool pool);

    /**
     * Generates a Map containing key:value pairs of data elements to be supplied to the Notify Service.
     *
     * @param templateId template for which the payload is to be assembled for.
     * @return Map of key values pairs of data elements for the template.
     */
    Map<String, String> generatePayLoadData(String templateId, Pool pool);

    WelshCourtLocation getWelshCourtLocation(String locationCode);

    Boolean isWelshCourtAndComms(Boolean welsh, WelshCourtLocation welshCourtLocation);

}
