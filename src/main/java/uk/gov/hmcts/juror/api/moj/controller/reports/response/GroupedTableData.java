package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Data
@ToString(callSuper = true)
public class GroupedTableData extends LinkedHashMap<String, Object> {

    @JsonIgnore
    private Type type;

    @JsonIgnore
    public int getSize() {
        if (Type.DATA.equals(type)) {
            return this.size();
        } else {
            return this.values()
                .stream()
                .map(o -> {
                    if (o instanceof Collection<?> collection) {
                        return collection.size();
                    }
                    if (o instanceof GroupedTableData) {
                        return ((GroupedTableData) o).getSize();
                    }
                    throw new IllegalArgumentException("Invalid type");
                })
                .reduce(0, Integer::sum);
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
