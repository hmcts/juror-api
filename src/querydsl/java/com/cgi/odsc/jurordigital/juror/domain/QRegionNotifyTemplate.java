package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QRegionNotifyTemplate is a Querydsl query type for RegionNotifyTemplate
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRegionNotifyTemplate extends EntityPathBase<RegionNotifyTemplate> {

    private static final long serialVersionUID = 133519568L;

    public static final QRegionNotifyTemplate regionNotifyTemplate = new QRegionNotifyTemplate("regionNotifyTemplate");

    public final NumberPath<Integer> legacyTemplateId = createNumber("legacyTemplateId", Integer.class);

    public final StringPath messageFormat = createString("messageFormat");

    public final StringPath notifyTemplateId = createString("notifyTemplateId");

    public final StringPath regionId = createString("regionId");

    public final NumberPath<Integer> regionTemplateId = createNumber("regionTemplateId", Integer.class);

    public final StringPath templateName = createString("templateName");

    public final StringPath triggeredTemplateId = createString("triggeredTemplateId");

    public final StringPath welsh = createString("welsh");

    public QRegionNotifyTemplate(String variable) {
        super(RegionNotifyTemplate.class, forVariable(variable));
    }

    public QRegionNotifyTemplate(Path<? extends RegionNotifyTemplate> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRegionNotifyTemplate(PathMetadata metadata) {
        super(RegionNotifyTemplate.class, metadata);
    }

}

