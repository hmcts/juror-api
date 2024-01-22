package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QJurorResponseAudit is a Querydsl query type for JurorResponseAudit
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJurorResponseAudit extends EntityPathBase<JurorResponseAudit> {

    private static final long serialVersionUID = 482141235L;

    public static final QJurorResponseAudit jurorResponseAudit = new QJurorResponseAudit("jurorResponseAudit");

    public final DateTimePath<java.util.Date> changed = createDateTime("changed", java.util.Date.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath login = createString("login");

    public final EnumPath<com.cgi.odsc.jurordigital.juror.domain.ProcessingStatus> newProcessingStatus = createEnum("newProcessingStatus", com.cgi.odsc.jurordigital.juror.domain.ProcessingStatus.class);

    public final EnumPath<com.cgi.odsc.jurordigital.juror.domain.ProcessingStatus> oldProcessingStatus = createEnum("oldProcessingStatus", com.cgi.odsc.jurordigital.juror.domain.ProcessingStatus.class);

    public QJurorResponseAudit(String variable) {
        super(JurorResponseAudit.class, forVariable(variable));
    }

    public QJurorResponseAudit(Path<? extends JurorResponseAudit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJurorResponseAudit(PathMetadata metadata) {
        super(JurorResponseAudit.class, metadata);
    }

}

