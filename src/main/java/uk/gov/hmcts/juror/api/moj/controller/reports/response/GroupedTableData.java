package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedHashMap;

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
