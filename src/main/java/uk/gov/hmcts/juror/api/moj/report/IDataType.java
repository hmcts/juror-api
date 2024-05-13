package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

import java.util.List;

public interface IDataType {
    String getId();

    List<EntityPath<?>> getRequiredTables();

    String getDisplayName();

    Class<?> getDataType();

    Expression<?> getExpression();

    IDataType[] getReturnTypes();

}
