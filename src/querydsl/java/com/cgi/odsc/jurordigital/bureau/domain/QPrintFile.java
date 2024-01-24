package com.cgi.odsc.jurordigital.bureau.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPrintFile is a Querydsl query type for PrintFile
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPrintFile extends EntityPathBase<PrintFile> {

    private static final long serialVersionUID = 1023331782L;

    public static final QPrintFile printFile = new QPrintFile("printFile");

    public final DateTimePath<java.util.Date> creationDate = createDateTime("creationDate", java.util.Date.class);

    public final StringPath detailRec = createString("detailRec");

    public final BooleanPath digitalComms = createBoolean("digitalComms");

    public final BooleanPath extractedFlag = createBoolean("extractedFlag");

    public final StringPath formType = createString("formType");

    public final StringPath partNo = createString("partNo");

    public final StringPath printFileName = createString("printFileName");

    public QPrintFile(String variable) {
        super(PrintFile.class, forVariable(variable));
    }

    public QPrintFile(Path<? extends PrintFile> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPrintFile(PathMetadata metadata) {
        super(PrintFile.class, metadata);
    }

}

