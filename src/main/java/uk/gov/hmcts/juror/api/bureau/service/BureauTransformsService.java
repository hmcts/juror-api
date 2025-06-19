package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.CombinedJurorResponse;

import java.util.List;

/**
 * Service to handle data transforms for bureau domain / DTO objects.
 */
public interface BureauTransformsService {

    /**
     * Transforms domain models to a list of DTOs and flags for urgency.
     *
     * @param details domain models to transform
     * @return transformed DTOs
     */
    List<BureauResponseSummaryDto> convertToDtos(Iterable<ModJurorDetail> details);

    /**
     * Transforms domain model to response DTO.
     *
     * @param detail domain model instance to transform
     * @return response DTO
     */
    BureauResponseSummaryDto detailToDto(ModJurorDetail detail);

    BureauResponseSummaryDto detailToDto(
        CombinedJurorResponse jurorResponse,
        Juror juror,
        JurorPool jurorPool,
        PoolRequest pool
    );

    /**
     * Transforms Staff entity to StaffDto.
     *
     * @param staffMember entity to convert
     * @return transformed DTO
     */
    StaffDto toStaffDto(User staffMember);

    CourtResponseSummaryDto detailCourtToDto(
        CombinedJurorResponse jurorResponse,
        Juror juror,
        JurorPool jurorPool,
        PoolRequest pool,
        String locCode);
}
