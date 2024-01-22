package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QJurorCommsPrintFiles is a Querydsl query type for JurorCommsPrintFiles
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJurorCommsPrintFiles extends EntityPathBase<JurorCommsPrintFiles> {

    private static final long serialVersionUID = -921649174L;

    public static final QJurorCommsPrintFiles jurorCommsPrintFiles = new QJurorCommsPrintFiles("jurorCommsPrintFiles");

    public final DateTimePath<java.util.Date> creationDate = createDateTime("creationDate", java.util.Date.class);

    public final StringPath detailRec = createString("detailRec");

    public final StringPath digitalComms = createString("digitalComms");

    public final BooleanPath extractedFlag = createBoolean("extractedFlag");

    public final StringPath formType = createString("formType");

    public final StringPath jurorNumber = createString("jurorNumber");

    public final StringPath notifyName = createString("notifyName");

    public final StringPath printFileName = createString("printFileName");

    public final StringPath templateId = createString("templateId");

    public final StringPath templateName = createString("templateName");

    public QJurorCommsPrintFiles(String variable) {
        super(JurorCommsPrintFiles.class, forVariable(variable));
    }

    public QJurorCommsPrintFiles(Path<? extends JurorCommsPrintFiles> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJurorCommsPrintFiles(PathMetadata metadata) {
        super(JurorCommsPrintFiles.class, metadata);
    }

}

