package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@SuppressWarnings("PMD.TooManyFields")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrintLetterDataResponseDto implements Serializable {

    String courtName;
    String courtAddressLine1;
    String courtAddressLine2;
    String courtAddressLine3;
    String courtAddressLine4;
    String courtAddressLine5;
    String courtAddressLine6;
    String courtPostCode;
    String courtPhoneNumber;
    String jurorFirstName;
    String jurorLastName;
    String jurorAddressLine1;
    String jurorAddressLine2;
    String jurorAddressLine3;
    String jurorAddressLine4;
    String jurorAddressLine5;
    String jurorPostcode;
    String date;
    String jurorNumber;
    String courtManager;
    String attendanceDate;
    String postponedToDate;
    String deferredToDate;
    String noShowDate;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    LocalTime noShowTime;
    String replyByDate;
    String dateOrderedToAttend;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    LocalTime attendTime;
    String defendant;
    String judgeName;
    String periodOfExemption;
    String signature;
    String url;
    List<AttendanceData> attendanceDataList;

    @Builder.Default
    Boolean welsh = Boolean.FALSE;

    @Builder
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttendanceData {
        private String nonAttendance;
        private BigDecimal lossOfEarnings;
        private BigDecimal childCare;
        private BigDecimal misc;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate attendanceDate;
    }

}
