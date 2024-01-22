package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotifyTemplateMapping is a Querydsl query type for NotifyTemplateMapping
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QNotifyTemplateMapping extends EntityPathBase<NotifyTemplateMapping> {

    private static final long serialVersionUID = 834537704L;

    public static final QNotifyTemplateMapping notifyTemplateMapping = new QNotifyTemplateMapping("notifyTemplateMapping");

    public final StringPath formType = createString("formType");

    public final NumberPath<Integer> notificationType = createNumber("notificationType", Integer.class);

    public final StringPath notifyName = createString("notifyName");

    public final StringPath templateId = createString("templateId");

    public final StringPath templateName = createString("templateName");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QNotifyTemplateMapping(String variable) {
        super(NotifyTemplateMapping.class, forVariable(variable));
    }

    public QNotifyTemplateMapping(Path<? extends NotifyTemplateMapping> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotifyTemplateMapping(PathMetadata metadata) {
        super(NotifyTemplateMapping.class, metadata);
    }

}

