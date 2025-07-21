package uk.gov.hmcts.juror.api.moj.controller.bureaudashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
@Schema(description = "Bureau pools under responded information DTO")
public class BureauPoolsUnderRespondedInfoDto {}
