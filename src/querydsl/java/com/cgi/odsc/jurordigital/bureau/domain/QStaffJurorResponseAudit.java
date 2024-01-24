package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStaffJurorResponseAudit is a Querydsl query type for StaffJurorResponseAudit
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStaffJurorResponseAudit extends EntityPathBase<StaffJurorResponseAudit> {

    private static final long serialVersionUID = -1280696371L;

    public static final QStaffJurorResponseAudit staffJurorResponseAudit = new QStaffJurorResponseAudit("staffJurorResponseAudit");

    public final DateTimePath<java.util.Date> created = createDateTime("created", java.util.Date.class);

    public final DateTimePath<java.util.Date> dateReceived = createDateTime("dateReceived", java.util.Date.class);

    public final StringPath jurorNumber = createString("jurorNumber");

    public final DateTimePath<java.util.Date> staffAssignmentDate = createDateTime("staffAssignmentDate", java.util.Date.class);

    public final StringPath staffLogin = createString("staffLogin");

    public final StringPath teamLeaderLogin = createString("teamLeaderLogin");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QStaffJurorResponseAudit(String variable) {
        super(StaffJurorResponseAudit.class, forVariable(variable));
    }

    public QStaffJurorResponseAudit(Path<? extends StaffJurorResponseAudit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStaffJurorResponseAudit(PathMetadata metadata) {
        super(StaffJurorResponseAudit.class, metadata);
    }

}

