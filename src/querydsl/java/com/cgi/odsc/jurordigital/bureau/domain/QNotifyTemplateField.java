package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotifyTemplateField is a Querydsl query type for NotifyTemplateField
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QNotifyTemplateField extends EntityPathBase<NotifyTemplateField> {

    private static final long serialVersionUID = 1907478420L;

    public static final QNotifyTemplateField notifyTemplateField = new QNotifyTemplateField("notifyTemplateField");

    public final BooleanPath convertToDate = createBoolean("convertToDate");

    public final StringPath databaseField = createString("databaseField");

    public final NumberPath<Integer> fieldLength = createNumber("fieldLength", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jdClassName = createString("jdClassName");

    public final StringPath jdClassProperty = createString("jdClassProperty");

    public final NumberPath<Integer> positionFrom = createNumber("positionFrom", Integer.class);

    public final NumberPath<Integer> positionTo = createNumber("positionTo", Integer.class);

    public final StringPath templateField = createString("templateField");

    public final StringPath templateId = createString("templateId");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QNotifyTemplateField(String variable) {
        super(NotifyTemplateField.class, forVariable(variable));
    }

    public QNotifyTemplateField(Path<? extends NotifyTemplateField> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotifyTemplateField(PathMetadata metadata) {
        super(NotifyTemplateField.class, metadata);
    }

}

