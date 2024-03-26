package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.User;

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
     * Transforms domain models to the output wrapper DTO.
     *
     * @param details domain models to transform
     * @return transformed DTOs
     */
    BureauResponseSummaryWrapper prepareOutput(Iterable<ModJurorDetail> details);

    /**
     * Transforms domain model to response DTO.
     *
     * @param detail domain model instance to transform
     * @return response DTO
     */
    BureauResponseSummaryDto detailToDto(ModJurorDetail detail);

    /**
     * Transforms Staff entity to StaffDto.
     *
     * @param staffMember entity to convert
     * @return transformed DTO
     */
    StaffDto toStaffDto(User staffMember);

    /**
     * Transforms {@link Team} entity to {@link TeamDto}.
     *
     * @param teamEntity entity to transform, null returns null
     * @return transformed DTO, nullable
     */
    TeamDto toTeamDto(Team teamEntity);
}
