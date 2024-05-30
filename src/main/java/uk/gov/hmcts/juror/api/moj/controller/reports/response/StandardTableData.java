package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.support.HasSize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class StandardTableData extends ArrayList<LinkedHashMap<String, Object>> implements HasSize {


    public StandardTableData(List<LinkedHashMap<String, Object>> data) {
        super(data);
    }

    @SafeVarargs
    public static StandardTableData of(LinkedHashMap<String, Object>... data) {
        return new StandardTableData(List.of(data));
    }

    public void removeDataTypes(IDataType... dataTypes) {
        for (LinkedHashMap<String, Object> data : this) {
            for (IDataType dataType : dataTypes) {
                data.remove(dataType.getId());
            }
        }
    }

    @Override
    public Long getSize() {
        return (long) this.size();
    }
}
