package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.Tuple;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.text.WordUtils;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Response DTO for listing pool request search results.
 */
@AllArgsConstructor
@Getter
@Schema(description = "Pool request search list response")
public class PoolRequestSearchListDto {

    private static final String BUREAU_STAGE_TEXT = "With the Bureau";
    private static final String COURT_STAGE_TEXT = "At court";

    @JsonProperty("poolRequests")
    @Schema(description = "List of pool requests")
    private List<PoolRequestSearchDataDto> data;

    @JsonProperty("resultsCount")
    @Schema(description = "Total number of results returned by search query (before pagination)")
    private long resultsCount;

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    @Getter
    @Schema(description = "Pool Request data")
    @ToString
    public static class PoolRequestSearchDataDto {

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("courtName")
        @Schema(name = "Court name", description = "Name for a given court location")
        private String courtName;

        @JsonProperty("poolStage")
        @Schema(name = "Pool stage", description = "Who currently owns the pool (Bureau or Court)")
        private String poolStage;

        @JsonProperty("poolStatus")
        @Schema(name = "Pool status",
            description = "What lifecycle stage is the pool currently in (Requested, Active or Completed) ")
        private String poolStatus;

        @JsonProperty("poolType")
        @Schema(name = "Pool type", description = "The type of court the pool is being requested for")
        private String poolType;

        @JsonProperty("serviceStartDate")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Service start date ",
            description = "The date the pool has been requested for and when the jurors are expected to first attend "
                + "the court")
        private LocalDate serviceStartDate;

        /**
         * Initialise an instance of this DTO class using a Tuple object returned by the database query:
         * <p/>
         * Tuple[0] - POOL_REQUEST.POOL_NUMBER
         * Tuple[1] - POOL_REQUEST.OWNER
         * Tuple[2] - COURT_LOCATION.LOC_NAME
         * Tuple[3] - POOL_TYPE.POOL_TYPE_DESCRIPTION
         * Tuple[4] - POOL_REQUEST.RETURN_DATE
         * Tuple[5] - POOL_REQUEST.NEW_REQUEST
         * Tuple[6] - SUM EXPRESSION (Count of active pool members)
         * Tuple[7] - POOL_REQUEST.NUMBER_REQUESTED
         * Tuple[8] - POOL_REQUEST.NIL_POOL
         *
         * @param queryResult a Tuple object representation of a result row from the Pool Request Search query
         */
        public PoolRequestSearchDataDto(Tuple queryResult) {
            final boolean ownedByBureau = Objects.requireNonNull(queryResult.get(1, String.class))
                .equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER);
            final boolean hasActivePoolMembers = Objects.requireNonNull(queryResult.get(6, Integer.class)) > 0;
            final Integer numberRequested = queryResult.get(7, Integer.class);
            final boolean isNilPool = Objects.requireNonNull(queryResult.get(8, Boolean.class));

            this.poolNumber = queryResult.get(0, String.class);
            this.poolStage = ownedByBureau ? BUREAU_STAGE_TEXT : COURT_STAGE_TEXT;
            this.courtName = WordUtils.capitalizeFully(Objects.requireNonNull(queryResult.get(2, String.class))
                .toLowerCase(Locale.ROOT));
            this.poolType = WordUtils.capitalize(Objects.requireNonNull(queryResult.get(3, String.class))
                .toLowerCase(Locale.ROOT), '.');
            this.serviceStartDate = queryResult.get(4, LocalDate.class);

            if (Objects.requireNonNull(queryResult.get(5, Character.class)) != 'N') {
                this.poolStatus = "Requested";
            } else if (isNilPool && numberRequested != null && numberRequested == 0) {
                this.poolStatus = "Nil";
            } else if (ownedByBureau || hasActivePoolMembers
                || (!isNilPool && serviceStartDate.isAfter(LocalDate.now()))) {
                this.poolStatus = "Active";
            } else {
                this.poolStatus = "Completed";
            }
        }

    }

}
