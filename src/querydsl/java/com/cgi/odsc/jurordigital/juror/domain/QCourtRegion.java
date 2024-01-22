package com.cgi.odsc.jurordigital.juror.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCourtRegion is a Querydsl query type for CourtRegion
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCourtRegion extends EntityPathBase<CourtRegion> {

    private static final long serialVersionUID = 210531942L;

    public static final QCourtRegion courtRegion = new QCourtRegion("courtRegion");

    public final StringPath notifyAccountKey = createString("notifyAccountKey");

    public final StringPath regionId = createString("regionId");

    public final StringPath regionName = createString("regionName");

    public final ListPath<RegionNotifyTemplate, QRegionNotifyTemplate> regionNotifyTemplates = this.<RegionNotifyTemplate, QRegionNotifyTemplate>createList("regionNotifyTemplates", RegionNotifyTemplate.class, QRegionNotifyTemplate.class, PathInits.DIRECT2);

    public QCourtRegion(String variable) {
        super(CourtRegion.class, forVariable(variable));
    }

    public QCourtRegion(Path<? extends CourtRegion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCourtRegion(PathMetadata metadata) {
        super(CourtRegion.class, metadata);
    }

}

