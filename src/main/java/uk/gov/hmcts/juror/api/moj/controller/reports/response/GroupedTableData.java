package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.support.HasSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Data
@ToString(callSuper = true)
public class GroupedTableData extends LinkedHashMap<String, Object>
    implements HasSize {

    @JsonIgnore
    private Type type;

    @JsonIgnore
    public Long getSize() {
        if (Type.DATA.equals(type)) {
            return (long) this.size();
        } else {
            return this.values()
                .stream()
                .map(o -> {
                    if (o instanceof Collection<?> collection) {
                        return (long) collection.size();
                    }
                    if (o instanceof GroupedTableData) {
                        return ((GroupedTableData) o).getSize();
                    }
                    throw new IllegalArgumentException("Invalid type");
                })
                .reduce(0L, Long::sum);
        }
    }

    public void removeDataKey(String key) {
        if (Type.DATA.equals(type)) {
            this.remove(key);
        } else {
            removeDataKeyInternal(key, this.values());
        }
    }

    private void removeDataKeyInternal(String key, Collection<?> data) {
        data.forEach(o -> {
            if (o instanceof Collection<?> collection) {
                removeDataKeyInternal(key, collection);
            } else if (o instanceof GroupedTableData) {
                ((GroupedTableData) o).removeDataKey(key);
            }
        });
    }

    public List<GroupedTableData> getAllDataItems() {
        List<GroupedTableData> dataItems = new ArrayList<>();
        return addAllDataItemsInternal(dataItems, this.values());
    }

    private List<GroupedTableData> addAllDataItemsInternal(List<GroupedTableData> dataItems,
                                                           Collection<?> data) {
        data.forEach(o -> {
            if (o instanceof Collection<?> collection) {
                addAllDataItemsInternal(dataItems, collection);
            } else if (o instanceof GroupedTableData groupedTableData) {
                if (Type.DATA.equals(groupedTableData.getType())) {
                    dataItems.add(groupedTableData);
                } else {
                    addAllDataItemsInternal(dataItems, groupedTableData.values());
                }
            }
        });
        return dataItems;
    }

    public List<GroupedTableData> getAllDataItemsIfExist(String key) {
        Object data = this.get(key);
        if (data instanceof GroupedTableData groupedTableData) {
            return groupedTableData.getAllDataItems();
        }
        return new ArrayList<>();
    }

    public void removeDataTypes(IDataType[] dataTypes) {
        getAllDataItems().forEach(groupedTableData -> {
            for (IDataType dataType : dataTypes) {
                groupedTableData.remove(dataType.getId());
            }
        });
    }

    public enum Type {
        GROUPED,
        DATA
    }

    public GroupedTableData add(String key, Object value) {
        put(key, value);
        return this;
    }

    public GroupedTableData setType(Type type) {
        this.type = type;
        return this;
    }

}
