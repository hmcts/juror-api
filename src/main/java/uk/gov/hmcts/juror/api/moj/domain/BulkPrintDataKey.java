package uk.gov.hmcts.juror.api.moj.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;


@EqualsAndHashCode
public class BulkPrintDataKey  implements Serializable {


        private String jurorNo;
        private Long  id;
        private LocalDate creationDate;
}
