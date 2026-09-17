package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "court_email_attachment", schema = "juror_mod")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtEmailAttachment {
    @Id
    @Column(name = "loc_code")
    private String locCode;

    @Column(name = "file_name_en")
    private String fileNameEn;

    @Column(name = "file_name_cy")
    private String fileNameCy;
}
