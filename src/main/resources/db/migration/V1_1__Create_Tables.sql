-- DROP ROLE juror;

do
$$begin if not exists (select * from pg_user where usename = 'juror') then
    CREATE ROLE juror WITH
        NOSUPERUSER
        NOCREATEDB
        NOCREATEROLE
        INHERIT
        LOGIN
        NOREPLICATION
        NOBYPASSRLS
        CONNECTION LIMIT -1;
end if;end
$$
;
-- DROP ROLE juror_digital;
do
$$begin if not exists (select * from pg_user where usename = 'juror_digital') then
    CREATE ROLE juror_digital WITH
        NOSUPERUSER
        NOCREATEDB
        NOCREATEROLE
        INHERIT
        LOGIN
        NOREPLICATION
        NOBYPASSRLS
        CONNECTION LIMIT -1;
end if;end
    $$
;
-- DROP ROLE juror_digital_user;
do
$$begin if not exists (select * from pg_user where usename = 'juror_digital_user') then
    CREATE ROLE juror_digital_user WITH
        NOSUPERUSER
        NOCREATEDB
        NOCREATEROLE
        INHERIT
        LOGIN
        NOREPLICATION
        NOBYPASSRLS
        CONNECTION LIMIT -1;
end if;end
$$
;
-- DROP ROLE juror_mod;
do
$$begin if not exists (select * from pg_user where usename = 'juror_mod') then
    CREATE ROLE juror_mod WITH
        NOSUPERUSER
        NOCREATEDB
        NOCREATEROLE
        INHERIT
        LOGIN
        NOREPLICATION
        NOBYPASSRLS
        CONNECTION LIMIT -1;
end if;end
$$
;



CREATE SCHEMA IF NOT EXISTS juror;

-- DROP TYPE juror.t_clob_detail_array;

CREATE TYPE juror.t_clob_detail_array AS
(
    detail_rec varchar(1260)
);

-- DROP TYPE juror.t_clob_rowid;

CREATE TYPE juror.t_clob_rowid AS
(
    rowid varchar(30)
);

-- DROP TYPE juror.t_creation_date;

CREATE TYPE juror.t_creation_date AS
(
    creation_date timestamp
);

-- DROP TYPE juror.t_file_name;

CREATE TYPE juror.t_file_name AS
(
    file_name varchar(100)
);

-- DROP TYPE juror.t_flags;

CREATE TYPE juror.t_flags AS
(
    flag varchar(2)
);

-- DROP TYPE juror.t_header;

CREATE TYPE juror.t_header AS
(
    "header" varchar(100)
);

-- DROP TYPE juror.t_rowid;

CREATE TYPE juror.t_rowid AS
(
    rowid varchar(30)
);
-- DROP TYPE juror.votersrowidtype;

CREATE TYPE juror.votersrowidtype AS
(
    row_id varchar(30)
);
-- DROP TYPE juror.votersrowidtable;

CREATE TYPE juror.votersrowidtable AS
(
    votersrowidtable juror.votersrowidtype
);


-- DROP SEQUENCE juror.aramis_count;

CREATE SEQUENCE juror.aramis_count
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999
    START 29
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_401;

CREATE SEQUENCE juror.aramis_invoice_number_401
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_402;

CREATE SEQUENCE juror.aramis_invoice_number_402
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_403;

CREATE SEQUENCE juror.aramis_invoice_number_403
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_404;

CREATE SEQUENCE juror.aramis_invoice_number_404
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_406;

CREATE SEQUENCE juror.aramis_invoice_number_406
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_407;

CREATE SEQUENCE juror.aramis_invoice_number_407
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_408;

CREATE SEQUENCE juror.aramis_invoice_number_408
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_409;

CREATE SEQUENCE juror.aramis_invoice_number_409
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_410;

CREATE SEQUENCE juror.aramis_invoice_number_410
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_411;

CREATE SEQUENCE juror.aramis_invoice_number_411
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_412;

CREATE SEQUENCE juror.aramis_invoice_number_412
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_413;

CREATE SEQUENCE juror.aramis_invoice_number_413
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_414;

CREATE SEQUENCE juror.aramis_invoice_number_414
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_415;

CREATE SEQUENCE juror.aramis_invoice_number_415
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 51
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_416;

CREATE SEQUENCE juror.aramis_invoice_number_416
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_417;

CREATE SEQUENCE juror.aramis_invoice_number_417
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_418;

CREATE SEQUENCE juror.aramis_invoice_number_418
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_419;

CREATE SEQUENCE juror.aramis_invoice_number_419
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_420;

CREATE SEQUENCE juror.aramis_invoice_number_420
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_421;

CREATE SEQUENCE juror.aramis_invoice_number_421
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 5
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_422;

CREATE SEQUENCE juror.aramis_invoice_number_422
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_423;

CREATE SEQUENCE juror.aramis_invoice_number_423
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_424;

CREATE SEQUENCE juror.aramis_invoice_number_424
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_425;

CREATE SEQUENCE juror.aramis_invoice_number_425
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_426;

CREATE SEQUENCE juror.aramis_invoice_number_426
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_427;

CREATE SEQUENCE juror.aramis_invoice_number_427
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_428;

CREATE SEQUENCE juror.aramis_invoice_number_428
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_429;

CREATE SEQUENCE juror.aramis_invoice_number_429
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_430;

CREATE SEQUENCE juror.aramis_invoice_number_430
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_431;

CREATE SEQUENCE juror.aramis_invoice_number_431
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_432;

CREATE SEQUENCE juror.aramis_invoice_number_432
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_433;

CREATE SEQUENCE juror.aramis_invoice_number_433
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_434;

CREATE SEQUENCE juror.aramis_invoice_number_434
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_435;

CREATE SEQUENCE juror.aramis_invoice_number_435
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 81
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_436;

CREATE SEQUENCE juror.aramis_invoice_number_436
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_437;

CREATE SEQUENCE juror.aramis_invoice_number_437
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_439;

CREATE SEQUENCE juror.aramis_invoice_number_439
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_440;

CREATE SEQUENCE juror.aramis_invoice_number_440
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_441;

CREATE SEQUENCE juror.aramis_invoice_number_441
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_442;

CREATE SEQUENCE juror.aramis_invoice_number_442
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 29
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_443;

CREATE SEQUENCE juror.aramis_invoice_number_443
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_444;

CREATE SEQUENCE juror.aramis_invoice_number_444
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_445;

CREATE SEQUENCE juror.aramis_invoice_number_445
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_446;

CREATE SEQUENCE juror.aramis_invoice_number_446
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_447;

CREATE SEQUENCE juror.aramis_invoice_number_447
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_448;

CREATE SEQUENCE juror.aramis_invoice_number_448
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_449;

CREATE SEQUENCE juror.aramis_invoice_number_449
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_450;

CREATE SEQUENCE juror.aramis_invoice_number_450
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_451;

CREATE SEQUENCE juror.aramis_invoice_number_451
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 66
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_452;

CREATE SEQUENCE juror.aramis_invoice_number_452
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_453;

CREATE SEQUENCE juror.aramis_invoice_number_453
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_454;

CREATE SEQUENCE juror.aramis_invoice_number_454
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_455;

CREATE SEQUENCE juror.aramis_invoice_number_455
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_456;

CREATE SEQUENCE juror.aramis_invoice_number_456
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_457;

CREATE SEQUENCE juror.aramis_invoice_number_457
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 25
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_458;

CREATE SEQUENCE juror.aramis_invoice_number_458
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_459;

CREATE SEQUENCE juror.aramis_invoice_number_459
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_460;

CREATE SEQUENCE juror.aramis_invoice_number_460
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_461;

CREATE SEQUENCE juror.aramis_invoice_number_461
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_462;

CREATE SEQUENCE juror.aramis_invoice_number_462
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_463;

CREATE SEQUENCE juror.aramis_invoice_number_463
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_464;

CREATE SEQUENCE juror.aramis_invoice_number_464
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_465;

CREATE SEQUENCE juror.aramis_invoice_number_465
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_466;

CREATE SEQUENCE juror.aramis_invoice_number_466
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_467;

CREATE SEQUENCE juror.aramis_invoice_number_467
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_468;

CREATE SEQUENCE juror.aramis_invoice_number_468
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_469;

CREATE SEQUENCE juror.aramis_invoice_number_469
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_470;

CREATE SEQUENCE juror.aramis_invoice_number_470
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_471;

CREATE SEQUENCE juror.aramis_invoice_number_471
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_472;

CREATE SEQUENCE juror.aramis_invoice_number_472
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_473;

CREATE SEQUENCE juror.aramis_invoice_number_473
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_474;

CREATE SEQUENCE juror.aramis_invoice_number_474
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_475;

CREATE SEQUENCE juror.aramis_invoice_number_475
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_476;

CREATE SEQUENCE juror.aramis_invoice_number_476
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_477;

CREATE SEQUENCE juror.aramis_invoice_number_477
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_478;

CREATE SEQUENCE juror.aramis_invoice_number_478
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_479;

CREATE SEQUENCE juror.aramis_invoice_number_479
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_480;

CREATE SEQUENCE juror.aramis_invoice_number_480
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_626;

CREATE SEQUENCE juror.aramis_invoice_number_626
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_invoice_number_799;

CREATE SEQUENCE juror.aramis_invoice_number_799
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_401;

CREATE SEQUENCE juror.aramis_unique_id_401
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_402;

CREATE SEQUENCE juror.aramis_unique_id_402
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_403;

CREATE SEQUENCE juror.aramis_unique_id_403
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_404;

CREATE SEQUENCE juror.aramis_unique_id_404
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_406;

CREATE SEQUENCE juror.aramis_unique_id_406
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_407;

CREATE SEQUENCE juror.aramis_unique_id_407
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_408;

CREATE SEQUENCE juror.aramis_unique_id_408
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_409;

CREATE SEQUENCE juror.aramis_unique_id_409
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_410;

CREATE SEQUENCE juror.aramis_unique_id_410
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_411;

CREATE SEQUENCE juror.aramis_unique_id_411
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_412;

CREATE SEQUENCE juror.aramis_unique_id_412
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_413;

CREATE SEQUENCE juror.aramis_unique_id_413
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_414;

CREATE SEQUENCE juror.aramis_unique_id_414
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_415;

CREATE SEQUENCE juror.aramis_unique_id_415
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 51
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_416;

CREATE SEQUENCE juror.aramis_unique_id_416
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_417;

CREATE SEQUENCE juror.aramis_unique_id_417
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_418;

CREATE SEQUENCE juror.aramis_unique_id_418
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_419;

CREATE SEQUENCE juror.aramis_unique_id_419
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_420;

CREATE SEQUENCE juror.aramis_unique_id_420
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_421;

CREATE SEQUENCE juror.aramis_unique_id_421
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 5
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_422;

CREATE SEQUENCE juror.aramis_unique_id_422
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_423;

CREATE SEQUENCE juror.aramis_unique_id_423
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_424;

CREATE SEQUENCE juror.aramis_unique_id_424
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_425;

CREATE SEQUENCE juror.aramis_unique_id_425
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_426;

CREATE SEQUENCE juror.aramis_unique_id_426
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_427;

CREATE SEQUENCE juror.aramis_unique_id_427
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_428;

CREATE SEQUENCE juror.aramis_unique_id_428
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_429;

CREATE SEQUENCE juror.aramis_unique_id_429
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_430;

CREATE SEQUENCE juror.aramis_unique_id_430
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_431;

CREATE SEQUENCE juror.aramis_unique_id_431
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_432;

CREATE SEQUENCE juror.aramis_unique_id_432
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_433;

CREATE SEQUENCE juror.aramis_unique_id_433
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_434;

CREATE SEQUENCE juror.aramis_unique_id_434
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_435;

CREATE SEQUENCE juror.aramis_unique_id_435
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 81
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_436;

CREATE SEQUENCE juror.aramis_unique_id_436
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_437;

CREATE SEQUENCE juror.aramis_unique_id_437
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_439;

CREATE SEQUENCE juror.aramis_unique_id_439
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_440;

CREATE SEQUENCE juror.aramis_unique_id_440
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_441;

CREATE SEQUENCE juror.aramis_unique_id_441
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_442;

CREATE SEQUENCE juror.aramis_unique_id_442
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 29
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_443;

CREATE SEQUENCE juror.aramis_unique_id_443
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_444;

CREATE SEQUENCE juror.aramis_unique_id_444
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_445;

CREATE SEQUENCE juror.aramis_unique_id_445
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_446;

CREATE SEQUENCE juror.aramis_unique_id_446
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_447;

CREATE SEQUENCE juror.aramis_unique_id_447
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_448;

CREATE SEQUENCE juror.aramis_unique_id_448
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_449;

CREATE SEQUENCE juror.aramis_unique_id_449
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_450;

CREATE SEQUENCE juror.aramis_unique_id_450
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_451;

CREATE SEQUENCE juror.aramis_unique_id_451
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 66
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_452;

CREATE SEQUENCE juror.aramis_unique_id_452
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_453;

CREATE SEQUENCE juror.aramis_unique_id_453
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_454;

CREATE SEQUENCE juror.aramis_unique_id_454
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_455;

CREATE SEQUENCE juror.aramis_unique_id_455
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_456;

CREATE SEQUENCE juror.aramis_unique_id_456
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_457;

CREATE SEQUENCE juror.aramis_unique_id_457
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 25
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_458;

CREATE SEQUENCE juror.aramis_unique_id_458
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_459;

CREATE SEQUENCE juror.aramis_unique_id_459
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_460;

CREATE SEQUENCE juror.aramis_unique_id_460
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_461;

CREATE SEQUENCE juror.aramis_unique_id_461
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_462;

CREATE SEQUENCE juror.aramis_unique_id_462
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_463;

CREATE SEQUENCE juror.aramis_unique_id_463
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_464;

CREATE SEQUENCE juror.aramis_unique_id_464
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_465;

CREATE SEQUENCE juror.aramis_unique_id_465
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_466;

CREATE SEQUENCE juror.aramis_unique_id_466
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_467;

CREATE SEQUENCE juror.aramis_unique_id_467
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_468;

CREATE SEQUENCE juror.aramis_unique_id_468
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_469;

CREATE SEQUENCE juror.aramis_unique_id_469
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_470;

CREATE SEQUENCE juror.aramis_unique_id_470
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_471;

CREATE SEQUENCE juror.aramis_unique_id_471
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_472;

CREATE SEQUENCE juror.aramis_unique_id_472
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_473;

CREATE SEQUENCE juror.aramis_unique_id_473
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_474;

CREATE SEQUENCE juror.aramis_unique_id_474
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_475;

CREATE SEQUENCE juror.aramis_unique_id_475
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_476;

CREATE SEQUENCE juror.aramis_unique_id_476
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_477;

CREATE SEQUENCE juror.aramis_unique_id_477
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_478;

CREATE SEQUENCE juror.aramis_unique_id_478
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_479;

CREATE SEQUENCE juror.aramis_unique_id_479
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_480;

CREATE SEQUENCE juror.aramis_unique_id_480
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_626;

CREATE SEQUENCE juror.aramis_unique_id_626
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.aramis_unique_id_799;

CREATE SEQUENCE juror.aramis_unique_id_799
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_401;

CREATE SEQUENCE juror.attend_audit_number_401
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_402;

CREATE SEQUENCE juror.attend_audit_number_402
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_403;

CREATE SEQUENCE juror.attend_audit_number_403
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_404;

CREATE SEQUENCE juror.attend_audit_number_404
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_406;

CREATE SEQUENCE juror.attend_audit_number_406
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_407;

CREATE SEQUENCE juror.attend_audit_number_407
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_408;

CREATE SEQUENCE juror.attend_audit_number_408
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000006
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_409;

CREATE SEQUENCE juror.attend_audit_number_409
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_410;

CREATE SEQUENCE juror.attend_audit_number_410
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_411;

CREATE SEQUENCE juror.attend_audit_number_411
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_412;

CREATE SEQUENCE juror.attend_audit_number_412
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_413;

CREATE SEQUENCE juror.attend_audit_number_413
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_414;

CREATE SEQUENCE juror.attend_audit_number_414
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_415;

CREATE SEQUENCE juror.attend_audit_number_415
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000030
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_416;

CREATE SEQUENCE juror.attend_audit_number_416
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000001
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_417;

CREATE SEQUENCE juror.attend_audit_number_417
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_418;

CREATE SEQUENCE juror.attend_audit_number_418
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_419;

CREATE SEQUENCE juror.attend_audit_number_419
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_420;

CREATE SEQUENCE juror.attend_audit_number_420
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_421;

CREATE SEQUENCE juror.attend_audit_number_421
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000009
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_422;

CREATE SEQUENCE juror.attend_audit_number_422
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_423;

CREATE SEQUENCE juror.attend_audit_number_423
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_424;

CREATE SEQUENCE juror.attend_audit_number_424
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000019
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_425;

CREATE SEQUENCE juror.attend_audit_number_425
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_426;

CREATE SEQUENCE juror.attend_audit_number_426
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_427;

CREATE SEQUENCE juror.attend_audit_number_427
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_428;

CREATE SEQUENCE juror.attend_audit_number_428
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_429;

CREATE SEQUENCE juror.attend_audit_number_429
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_430;

CREATE SEQUENCE juror.attend_audit_number_430
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_431;

CREATE SEQUENCE juror.attend_audit_number_431
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_432;

CREATE SEQUENCE juror.attend_audit_number_432
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_433;

CREATE SEQUENCE juror.attend_audit_number_433
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_434;

CREATE SEQUENCE juror.attend_audit_number_434
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_435;

CREATE SEQUENCE juror.attend_audit_number_435
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000066
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_436;

CREATE SEQUENCE juror.attend_audit_number_436
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_437;

CREATE SEQUENCE juror.attend_audit_number_437
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_439;

CREATE SEQUENCE juror.attend_audit_number_439
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_440;

CREATE SEQUENCE juror.attend_audit_number_440
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_441;

CREATE SEQUENCE juror.attend_audit_number_441
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_442;

CREATE SEQUENCE juror.attend_audit_number_442
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000073
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_443;

CREATE SEQUENCE juror.attend_audit_number_443
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_444;

CREATE SEQUENCE juror.attend_audit_number_444
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_445;

CREATE SEQUENCE juror.attend_audit_number_445
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_446;

CREATE SEQUENCE juror.attend_audit_number_446
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_447;

CREATE SEQUENCE juror.attend_audit_number_447
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_448;

CREATE SEQUENCE juror.attend_audit_number_448
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_449;

CREATE SEQUENCE juror.attend_audit_number_449
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_450;

CREATE SEQUENCE juror.attend_audit_number_450
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_451;

CREATE SEQUENCE juror.attend_audit_number_451
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000083
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_452;

CREATE SEQUENCE juror.attend_audit_number_452
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_453;

CREATE SEQUENCE juror.attend_audit_number_453
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_454;

CREATE SEQUENCE juror.attend_audit_number_454
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_455;

CREATE SEQUENCE juror.attend_audit_number_455
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_456;

CREATE SEQUENCE juror.attend_audit_number_456
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_457;

CREATE SEQUENCE juror.attend_audit_number_457
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000001
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_458;

CREATE SEQUENCE juror.attend_audit_number_458
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_459;

CREATE SEQUENCE juror.attend_audit_number_459
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_460;

CREATE SEQUENCE juror.attend_audit_number_460
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_461;

CREATE SEQUENCE juror.attend_audit_number_461
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_462;

CREATE SEQUENCE juror.attend_audit_number_462
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_463;

CREATE SEQUENCE juror.attend_audit_number_463
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_464;

CREATE SEQUENCE juror.attend_audit_number_464
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_465;

CREATE SEQUENCE juror.attend_audit_number_465
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_466;

CREATE SEQUENCE juror.attend_audit_number_466
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_467;

CREATE SEQUENCE juror.attend_audit_number_467
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_468;

CREATE SEQUENCE juror.attend_audit_number_468
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_469;

CREATE SEQUENCE juror.attend_audit_number_469
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_470;

CREATE SEQUENCE juror.attend_audit_number_470
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_471;

CREATE SEQUENCE juror.attend_audit_number_471
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_472;

CREATE SEQUENCE juror.attend_audit_number_472
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_473;

CREATE SEQUENCE juror.attend_audit_number_473
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_474;

CREATE SEQUENCE juror.attend_audit_number_474
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_475;

CREATE SEQUENCE juror.attend_audit_number_475
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_476;

CREATE SEQUENCE juror.attend_audit_number_476
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_477;

CREATE SEQUENCE juror.attend_audit_number_477
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_478;

CREATE SEQUENCE juror.attend_audit_number_478
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_479;

CREATE SEQUENCE juror.attend_audit_number_479
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_480;

CREATE SEQUENCE juror.attend_audit_number_480
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_626;

CREATE SEQUENCE juror.attend_audit_number_626
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.attend_audit_number_799;

CREATE SEQUENCE juror.attend_audit_number_799
    INCREMENT BY 1
    MINVALUE 10000000
    MAXVALUE 99999999
    START 10000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_401;

CREATE SEQUENCE juror.audit_number_401
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_402;

CREATE SEQUENCE juror.audit_number_402
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_403;

CREATE SEQUENCE juror.audit_number_403
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_404;

CREATE SEQUENCE juror.audit_number_404
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_406;

CREATE SEQUENCE juror.audit_number_406
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_407;

CREATE SEQUENCE juror.audit_number_407
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_408;

CREATE SEQUENCE juror.audit_number_408
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_409;

CREATE SEQUENCE juror.audit_number_409
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_410;

CREATE SEQUENCE juror.audit_number_410
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_411;

CREATE SEQUENCE juror.audit_number_411
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_412;

CREATE SEQUENCE juror.audit_number_412
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_413;

CREATE SEQUENCE juror.audit_number_413
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_414;

CREATE SEQUENCE juror.audit_number_414
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_415;

CREATE SEQUENCE juror.audit_number_415
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000120
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_416;

CREATE SEQUENCE juror.audit_number_416
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000001
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_417;

CREATE SEQUENCE juror.audit_number_417
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_418;

CREATE SEQUENCE juror.audit_number_418
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_419;

CREATE SEQUENCE juror.audit_number_419
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_420;

CREATE SEQUENCE juror.audit_number_420
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_421;

CREATE SEQUENCE juror.audit_number_421
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000031
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_422;

CREATE SEQUENCE juror.audit_number_422
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_423;

CREATE SEQUENCE juror.audit_number_423
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_424;

CREATE SEQUENCE juror.audit_number_424
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_425;

CREATE SEQUENCE juror.audit_number_425
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_426;

CREATE SEQUENCE juror.audit_number_426
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_427;

CREATE SEQUENCE juror.audit_number_427
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_428;

CREATE SEQUENCE juror.audit_number_428
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_429;

CREATE SEQUENCE juror.audit_number_429
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_430;

CREATE SEQUENCE juror.audit_number_430
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_431;

CREATE SEQUENCE juror.audit_number_431
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_432;

CREATE SEQUENCE juror.audit_number_432
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_433;

CREATE SEQUENCE juror.audit_number_433
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_434;

CREATE SEQUENCE juror.audit_number_434
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_435;

CREATE SEQUENCE juror.audit_number_435
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000075
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_436;

CREATE SEQUENCE juror.audit_number_436
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_437;

CREATE SEQUENCE juror.audit_number_437
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_439;

CREATE SEQUENCE juror.audit_number_439
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_440;

CREATE SEQUENCE juror.audit_number_440
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_441;

CREATE SEQUENCE juror.audit_number_441
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_442;

CREATE SEQUENCE juror.audit_number_442
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000091
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_443;

CREATE SEQUENCE juror.audit_number_443
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_444;

CREATE SEQUENCE juror.audit_number_444
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_445;

CREATE SEQUENCE juror.audit_number_445
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_446;

CREATE SEQUENCE juror.audit_number_446
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_447;

CREATE SEQUENCE juror.audit_number_447
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_448;

CREATE SEQUENCE juror.audit_number_448
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_449;

CREATE SEQUENCE juror.audit_number_449
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_450;

CREATE SEQUENCE juror.audit_number_450
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_451;

CREATE SEQUENCE juror.audit_number_451
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000063
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_452;

CREATE SEQUENCE juror.audit_number_452
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_453;

CREATE SEQUENCE juror.audit_number_453
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_454;

CREATE SEQUENCE juror.audit_number_454
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_455;

CREATE SEQUENCE juror.audit_number_455
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_456;

CREATE SEQUENCE juror.audit_number_456
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_457;

CREATE SEQUENCE juror.audit_number_457
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000048
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_458;

CREATE SEQUENCE juror.audit_number_458
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_459;

CREATE SEQUENCE juror.audit_number_459
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_460;

CREATE SEQUENCE juror.audit_number_460
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_461;

CREATE SEQUENCE juror.audit_number_461
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_462;

CREATE SEQUENCE juror.audit_number_462
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_463;

CREATE SEQUENCE juror.audit_number_463
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_464;

CREATE SEQUENCE juror.audit_number_464
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_465;

CREATE SEQUENCE juror.audit_number_465
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_466;

CREATE SEQUENCE juror.audit_number_466
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_467;

CREATE SEQUENCE juror.audit_number_467
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_468;

CREATE SEQUENCE juror.audit_number_468
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_469;

CREATE SEQUENCE juror.audit_number_469
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_470;

CREATE SEQUENCE juror.audit_number_470
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_471;

CREATE SEQUENCE juror.audit_number_471
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_472;

CREATE SEQUENCE juror.audit_number_472
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_473;

CREATE SEQUENCE juror.audit_number_473
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_474;

CREATE SEQUENCE juror.audit_number_474
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_475;

CREATE SEQUENCE juror.audit_number_475
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_476;

CREATE SEQUENCE juror.audit_number_476
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_477;

CREATE SEQUENCE juror.audit_number_477
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_478;

CREATE SEQUENCE juror.audit_number_478
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_479;

CREATE SEQUENCE juror.audit_number_479
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_480;

CREATE SEQUENCE juror.audit_number_480
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_626;

CREATE SEQUENCE juror.audit_number_626
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.audit_number_799;

CREATE SEQUENCE juror.audit_number_799
    INCREMENT BY 1
    MINVALUE 40000000
    MAXVALUE 99999999
    START 40000000
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.content_store_seq;

CREATE SEQUENCE juror.content_store_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 454
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE juror.data_file_no;

CREATE SEQUENCE juror.data_file_no
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999
    START 1040
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_401;

CREATE SEQUENCE juror.local_part_no_401
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_402;

CREATE SEQUENCE juror.local_part_no_402
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_403;

CREATE SEQUENCE juror.local_part_no_403
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_404;

CREATE SEQUENCE juror.local_part_no_404
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_406;

CREATE SEQUENCE juror.local_part_no_406
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_407;

CREATE SEQUENCE juror.local_part_no_407
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_408;

CREATE SEQUENCE juror.local_part_no_408
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 3
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_409;

CREATE SEQUENCE juror.local_part_no_409
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_410;

CREATE SEQUENCE juror.local_part_no_410
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_411;

CREATE SEQUENCE juror.local_part_no_411
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_412;

CREATE SEQUENCE juror.local_part_no_412
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_413;

CREATE SEQUENCE juror.local_part_no_413
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_414;

CREATE SEQUENCE juror.local_part_no_414
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_415;

CREATE SEQUENCE juror.local_part_no_415
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 393
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_416;

CREATE SEQUENCE juror.local_part_no_416
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 5
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_417;

CREATE SEQUENCE juror.local_part_no_417
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_418;

CREATE SEQUENCE juror.local_part_no_418
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 2
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_419;

CREATE SEQUENCE juror.local_part_no_419
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_420;

CREATE SEQUENCE juror.local_part_no_420
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_421;

CREATE SEQUENCE juror.local_part_no_421
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 25
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_422;

CREATE SEQUENCE juror.local_part_no_422
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_423;

CREATE SEQUENCE juror.local_part_no_423
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_424;

CREATE SEQUENCE juror.local_part_no_424
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 7
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_425;

CREATE SEQUENCE juror.local_part_no_425
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_426;

CREATE SEQUENCE juror.local_part_no_426
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_427;

CREATE SEQUENCE juror.local_part_no_427
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_428;

CREATE SEQUENCE juror.local_part_no_428
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_429;

CREATE SEQUENCE juror.local_part_no_429
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_430;

CREATE SEQUENCE juror.local_part_no_430
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_431;

CREATE SEQUENCE juror.local_part_no_431
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_432;

CREATE SEQUENCE juror.local_part_no_432
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_433;

CREATE SEQUENCE juror.local_part_no_433
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_434;

CREATE SEQUENCE juror.local_part_no_434
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_435;

CREATE SEQUENCE juror.local_part_no_435
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 421
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_436;

CREATE SEQUENCE juror.local_part_no_436
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_437;

CREATE SEQUENCE juror.local_part_no_437
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_439;

CREATE SEQUENCE juror.local_part_no_439
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_440;

CREATE SEQUENCE juror.local_part_no_440
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_441;

CREATE SEQUENCE juror.local_part_no_441
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_442;

CREATE SEQUENCE juror.local_part_no_442
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 131
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_443;

CREATE SEQUENCE juror.local_part_no_443
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_444;

CREATE SEQUENCE juror.local_part_no_444
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_445;

CREATE SEQUENCE juror.local_part_no_445
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_446;

CREATE SEQUENCE juror.local_part_no_446
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_447;

CREATE SEQUENCE juror.local_part_no_447
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_448;

CREATE SEQUENCE juror.local_part_no_448
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_449;

CREATE SEQUENCE juror.local_part_no_449
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_450;

CREATE SEQUENCE juror.local_part_no_450
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_451;

CREATE SEQUENCE juror.local_part_no_451
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 489
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_452;

CREATE SEQUENCE juror.local_part_no_452
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 4
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_453;

CREATE SEQUENCE juror.local_part_no_453
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_454;

CREATE SEQUENCE juror.local_part_no_454
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_455;

CREATE SEQUENCE juror.local_part_no_455
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_456;

CREATE SEQUENCE juror.local_part_no_456
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_457;

CREATE SEQUENCE juror.local_part_no_457
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_458;

CREATE SEQUENCE juror.local_part_no_458
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_459;

CREATE SEQUENCE juror.local_part_no_459
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_460;

CREATE SEQUENCE juror.local_part_no_460
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_461;

CREATE SEQUENCE juror.local_part_no_461
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_462;

CREATE SEQUENCE juror.local_part_no_462
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_463;

CREATE SEQUENCE juror.local_part_no_463
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_464;

CREATE SEQUENCE juror.local_part_no_464
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_465;

CREATE SEQUENCE juror.local_part_no_465
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_466;

CREATE SEQUENCE juror.local_part_no_466
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_467;

CREATE SEQUENCE juror.local_part_no_467
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_468;

CREATE SEQUENCE juror.local_part_no_468
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_469;

CREATE SEQUENCE juror.local_part_no_469
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_470;

CREATE SEQUENCE juror.local_part_no_470
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_471;

CREATE SEQUENCE juror.local_part_no_471
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_472;

CREATE SEQUENCE juror.local_part_no_472
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_473;

CREATE SEQUENCE juror.local_part_no_473
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_474;

CREATE SEQUENCE juror.local_part_no_474
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_475;

CREATE SEQUENCE juror.local_part_no_475
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_476;

CREATE SEQUENCE juror.local_part_no_476
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_477;

CREATE SEQUENCE juror.local_part_no_477
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_478;

CREATE SEQUENCE juror.local_part_no_478
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_479;

CREATE SEQUENCE juror.local_part_no_479
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_480;

CREATE SEQUENCE juror.local_part_no_480
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_626;

CREATE SEQUENCE juror.local_part_no_626
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.local_part_no_799;

CREATE SEQUENCE juror.local_part_no_799
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror.plxmlstatusseq;

CREATE SEQUENCE juror.plxmlstatusseq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999
    START 20777
    CACHE 1
    NO CYCLE;
-- juror.acct_pay_data definition

-- Drop table

-- DROP TABLE juror.acct_pay_data;

CREATE TABLE juror.acct_pay_data
(
    "owner"               varchar(3)     NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no               varchar(9)     NOT NULL,
    voter_reg             varchar(12)    NULL,
    days                  float8         NOT NULL,
    net_pay               numeric(22, 2) NULL,
    ssn                   varchar(11)    NULL,
    address               varchar(40)    NOT NULL,
    zip                   varchar(10)    NULL,
    pool_no               varchar(9)     NOT NULL,
    date_acct             timestamp(0)   NOT NULL,
    faudit                varchar(11)    NULL,
    state                 varchar(2)     NULL,
    start_date_of_service timestamp(0)   NULL,
    pool_type             varchar(3)     NULL,
    miles                 numeric(38)    NULL,
    no_mileage_days       numeric(38)    NULL,
    county_phone_no       varchar(12)    NULL,
    lname                 varchar(20)    NULL,
    fname                 varchar(20)    NULL,
    county_code           varchar(5)     NULL,
    CONSTRAINT acct_pay_data_pkey PRIMARY KEY (part_no)
);


-- juror.accused definition

-- Drop table

-- DROP TABLE juror.accused;

CREATE TABLE juror.accused
(
    "owner"  varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    trial_no varchar(16) NOT NULL,
    lname    varchar(20) NOT NULL,
    fname    varchar(20) NOT NULL,
    CONSTRAINT accused_pkey PRIMARY KEY (owner, trial_no, lname, fname)
);
CREATE INDEX accused_trial_idx ON juror.accused USING btree (owner, trial_no);


-- juror.aramis_payments definition

-- Drop table

-- DROP TABLE juror.aramis_payments;

CREATE TABLE juror.aramis_payments
(
    "owner"          varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    loc_code         varchar(3)   NOT NULL,
    unique_id        varchar(7)   NOT NULL,
    creation_date    timestamp(0) NOT NULL DEFAULT statement_timestamp(),
    expense_total    float8       NOT NULL,
    part_invoice     varchar(16)  NOT NULL,
    bank_sort_code   varchar(6)   NULL,
    bank_ac_name     varchar(18)  NULL,
    bank_ac_number   varchar(8)   NULL,
    build_soc_number varchar(18)  NULL,
    address_line1    varchar(35)  NULL,
    address_line2    varchar(35)  NULL,
    address_line3    varchar(35)  NULL,
    address_line4    varchar(35)  NULL,
    address_line5    varchar(35)  NULL,
    postcode         varchar(10)  NULL,
    aramis_auth_code varchar(9)   NOT NULL,
    "name"           varchar(50)  NOT NULL,
    loc_cost_centre  varchar(5)   NOT NULL,
    travel_total     float8       NULL,
    sub_total        float8       NULL,
    floss_total      float8       NULL,
    sub_date         timestamp(0) NOT NULL,
    con_file_ref     varchar(30)  NULL,
    CONSTRAINT aramis_payments_pkey PRIMARY KEY (loc_code, unique_id, owner)
);
CREATE INDEX aramis_payments_file_ref_fbi ON juror.aramis_payments USING btree ((
                                                                                    CASE
                                                                                        WHEN (con_file_ref IS NULL)
                                                                                            THEN 'N'::text
                                                                                        ELSE NULL::text
                                                                                        END));


-- juror.bioform definition

-- Drop table

-- DROP TABLE juror.bioform;

CREATE TABLE juror.bioform
(
    "owner"        varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no        varchar(9)   NOT NULL,
    county         varchar(18)  NULL,
    live_st        numeric(38)  NULL,
    live_ct        numeric(38)  NULL,
    educ_yrs       numeric(38)  NULL,
    birth_st       varchar(2)   NULL,
    birth_cit      varchar(16)  NULL,
    num_children   numeric(38)  NULL,
    child_boy      numeric(38)  NULL,
    age_boy        numeric(38)  NULL,
    child_gir      numeric(38)  NULL,
    age_gir        numeric(38)  NULL,
    marital        varchar(1)   NULL,
    can_drive      varchar(1)   NULL,
    crim_case      varchar(1)   NULL,
    convicted      varchar(1)   NULL,
    conv_exp       varchar(29)  NULL,
    emp_name       varchar(20)  NULL,
    emp_add        varchar(20)  NULL,
    emp_city       varchar(16)  NULL,
    years_worked   numeric(38)  NULL,
    spouse_oc      varchar(18)  NULL,
    lawsuit        varchar(1)   NULL,
    type_suit      varchar(20)  NULL,
    injuries       varchar(1)   NULL,
    p_bodily       varchar(1)   NULL,
    f_bodily       varchar(1)   NULL,
    was_juror      varchar(1)   NULL,
    civ_or_crim    varchar(1)   NULL,
    jury_date      timestamp(0) NULL,
    court          varchar(16)  NULL,
    spouse_name    varchar(40)  NULL,
    spouse_emp     varchar(20)  NULL,
    spouse_emp_yr  numeric(38)  NULL,
    religion       varchar(20)  NULL,
    jury_verdict   varchar(1)   NULL,
    friends_law    varchar(1)   NULL,
    boy_ages       varchar(14)  NULL,
    girl_ages      varchar(14)  NULL,
    jury_year      varchar(2)   NULL,
    was_juror_c    varchar(1)   NULL,
    jury_year_c    varchar(2)   NULL,
    jury_verdict_c varchar(10)  NULL,
    allstate       varchar(1)   NULL,
    statefarm      varchar(1)   NULL,
    otherins       varchar(20)  NULL,
    CONSTRAINT bioform_pkey PRIMARY KEY (part_no, owner)
);


-- juror.cert_lett definition

-- Drop table

-- DROP TABLE juror.cert_lett;

CREATE TABLE juror.cert_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT cert_lett_pkey PRIMARY KEY (part_no, owner)
);
CREATE INDEX cert_lett_own_date ON juror.cert_lett USING btree (owner, date_printed);


-- juror."condition" definition

-- Drop table

-- DROP TABLE juror."condition";

CREATE TABLE juror."condition"
(
    condition_id      int8         NOT NULL,
    description       varchar(150) NOT NULL,
    qual_literal      varchar(3)   NULL,
    ql_use_flag       varchar(1)   NULL,
    ql_modifier       varchar(1)   NULL,
    duration          varchar(3)   NULL,
    duration_modifier varchar(1)   NULL,
    end_date_period   varchar(3)   NULL,
    CONSTRAINT condition_pkey PRIMARY KEY (condition_id)
);


-- juror.confirm_lett definition

-- Drop table

-- DROP TABLE juror.confirm_lett;

CREATE TABLE juror.confirm_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT confirm_lett_pkey PRIMARY KEY (part_no, owner)
);


-- juror.contact_preference definition

-- Drop table

-- DROP TABLE juror.contact_preference;

CREATE TABLE juror.contact_preference
(
    id          int2        NULL,
    "type"      varchar(25) NULL,
    description varchar(50) NULL
);


-- juror.content_store definition

-- Drop table

-- DROP TABLE juror.content_store;

CREATE TABLE juror.content_store
(
    request_id         int8         NULL,
    document_id        varchar(50)  NOT NULL,
    date_on_q_for_send timestamp(0) NULL DEFAULT statement_timestamp(),
    file_type          varchar(10)  NOT NULL,
    date_sent          timestamp(0) NULL,
    "data"             text         NULL
);


-- juror.context_data definition

-- Drop table

-- DROP TABLE juror.context_data;

CREATE TABLE juror.context_data
(
    loc_code   varchar(3) NOT NULL,
    context_id varchar(3) NULL,
    CONSTRAINT context_data_pkey PRIMARY KEY (loc_code)
);


-- juror.coroner_pool definition

-- Drop table

-- DROP TABLE juror.coroner_pool;

CREATE TABLE juror.coroner_pool
(
    cor_pool_no      varchar(9)   NOT NULL,
    cor_name         varchar(35)  NOT NULL,
    cor_court_loc    varchar(3)   NOT NULL,
    cor_request_dt   timestamp(0) NOT NULL,
    cor_service_dt   timestamp(0) NOT NULL,
    cor_no_requested int4         NOT NULL,
    CONSTRAINT coroner_pool_pkey PRIMARY KEY (cor_pool_no)
);


-- juror.court_catchment_area definition

-- Drop table

-- DROP TABLE juror.court_catchment_area;

CREATE TABLE juror.court_catchment_area
(
    postcode varchar(4) NOT NULL,
    loc_code varchar(3) NOT NULL,
    CONSTRAINT court_catchment_area_pkey PRIMARY KEY (postcode, loc_code)
);


-- juror.court_group definition

-- Drop table

-- DROP TABLE juror.court_group;

CREATE TABLE juror.court_group
(
    "owner"  varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    group_no int2        NOT NULL,
    "name"   varchar(40) NULL,
    CONSTRAINT court_group_pkey PRIMARY KEY (group_no, owner)
);


-- juror.court_location definition

-- Drop table

-- DROP TABLE juror.court_location;

CREATE TABLE juror.court_location
(
    "owner"                       varchar(3)   NULL DEFAULT current_setting('juror_app.owner'::text, true),
    loc_code                      varchar(3)   NOT NULL,
    loc_name                      varchar(40)  NOT NULL,
    loc_address1                  varchar(35)  NOT NULL,
    loc_address2                  varchar(35)  NULL,
    loc_address3                  varchar(35)  NULL,
    loc_address4                  varchar(35)  NULL,
    loc_address5                  varchar(35)  NULL,
    loc_address6                  varchar(35)  NULL,
    loc_zip                       varchar(10)  NULL,
    loc_phone                     varchar(12)  NULL,
    call_in_phone                 varchar(12)  NULL,
    tdd_phone                     varchar(12)  NULL,
    loc_court_name                varchar(30)  NULL,
    loc_room_no                   varchar(6)   NULL,
    loc_attend_time               varchar(10)  NULL,
    loc_rate_mcycles              float8       NULL,
    loc_rate_mcars                float8       NULL,
    loc_rate_pcycles              float8       NULL,
    loc_standard_mcycles          float8       NULL,
    loc_standard_mcars            float8       NULL,
    loc_loss_lfour                float8       NULL,
    loc_loss_mfour                float8       NULL,
    loc_loss_mten                 float8       NULL,
    loc_subs_lfive                float8       NULL,
    loc_subs_mfive                float8       NULL,
    loc_loss_oten                 float8       NULL,
    loc_loss_overnight            float8       NULL,
    loc_child_care                float8       NULL,
    loc_misc_description          varchar(20)  NULL,
    loc_misc_amount               float8       NULL,
    check_for_bioform             varchar(1)   NULL,
    loc_default_room              varchar(6)   NULL,
    loc_ins_hour_rate             float8       NULL,
    loc_deferred_perc             int8         NULL,
    loc_signature                 varchar(30)  NULL,
    loc_timeout                   timestamp(0) NULL,
    loc_pet_months                numeric(38)  NULL,
    term_of_service               varchar(20)  NULL,
    county_code                   varchar(5)   NULL,
    loc_jury_rate_n               numeric(38)  NULL,
    voters_count                  int4         NULL,
    loc_rail_bus                  float8       NULL,
    loc_hired_vehicle             float8       NULL,
    loc_rate_mcycles_2            float8       NULL,
    loc_rate_mcars_2              float8       NULL,
    loc_rate_mcars_3              float8       NULL,
    loc_loss_on_half              float8       NULL,
    loc_ac_loss_of_earn           varchar(13)  NULL,
    loc_ac_subst                  varchar(13)  NULL,
    loc_ac_travel                 varchar(13)  NULL,
    loc_cost_centre               varchar(7)   NULL,
    loc_primary                   varchar(1)   NULL,
    group_no                      int2         NULL,
    voters_lock                   int2         NULL,
    last_update                   timestamp(0) NULL,
    yield                         float8       NULL,
    loc_rate_mcycles_alt          float8       NULL,
    loc_rate_mcars_alt            float8       NULL,
    loc_rate_pcycles_alt          float8       NULL,
    loc_loss_lfour_alt            float8       NULL,
    loc_loss_mfour_alt            float8       NULL,
    loc_loss_mten_alt             float8       NULL,
    loc_subs_lfive_alt            float8       NULL,
    loc_subs_mfive_alt            float8       NULL,
    loc_loss_oten_alt             float8       NULL,
    loc_loss_overnight_alt        float8       NULL,
    loc_child_care_alt            float8       NULL,
    loc_rail_bus_alt              float8       NULL,
    loc_hired_vehicle_alt         float8       NULL,
    loc_rate_mcycles_2_alt        float8       NULL,
    loc_rate_mcars_2_alt          float8       NULL,
    loc_rate_mcars_3_alt          float8       NULL,
    loc_loss_on_half_alt          float8       NULL,
    loc_rate_transition           timestamp(0) NULL,
    pnc_check_on                  varchar(1)   NULL,
    catering_scheme               varchar(1)   NULL DEFAULT 'N'::character varying,
    export_max_rows               int4         NULL,
    export_path                   varchar(200) NULL,
    pool_transfer_adjustment_days int8         NULL DEFAULT 0,
    location_address              varchar(230) NULL,
    jury_officer_phone            varchar(100) NULL,
    region_id                     varchar(5)   NULL,
    CONSTRAINT court_location_pkey PRIMARY KEY (loc_code)
);



-- juror.current_trans definition

-- Drop table

-- DROP TABLE juror.current_trans;

CREATE TABLE juror.current_trans
(
    "owner"          varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    login            varchar(20) NOT NULL,
    trial_no         varchar(16) NOT NULL,
    room_no          varchar(6)  NULL,
    judge            varchar(4)  NULL,
    panel_total      numeric(38) NULL,
    panel_remaining  numeric(38) NULL,
    scheduled_today  numeric(38) NULL,
    checked_in       numeric(38) NULL,
    record_type      varchar(1)  NULL,
    hdr_scheduled    numeric(38) NULL,
    hdr_checked_in   numeric(38) NULL,
    hdr_checked_out  numeric(38) NULL,
    hdr_in_court     numeric(38) NULL,
    hdr_available    numeric(38) NULL,
    hdr_never_served numeric(38) NULL,
    CONSTRAINT current_trans_pkey PRIMARY KEY (login, trial_no, owner)
);


-- juror.daily_trans definition

-- Drop table

-- DROP TABLE juror.daily_trans;

CREATE TABLE juror.daily_trans
(
    "owner"   varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    unique_id varchar(20) NOT NULL,
    trial     varchar(16) NOT NULL,
    room      varchar(6)  NULL,
    judge     varchar(4)  NULL,
    max_used  numeric(38) NULL,
    used_now  numeric(38) NULL,
    scheduled numeric(38) NULL,
    in_court  numeric(38) NULL,
    CONSTRAINT daily_trans_pkey PRIMARY KEY (unique_id, trial, owner)
);


-- juror.db_version definition

-- Drop table

-- DROP TABLE juror.db_version;

CREATE TABLE juror.db_version
(
    component      varchar(256) NOT NULL,
    latest_version varchar(8)   NOT NULL,
    min_version    varchar(8)   NULL
);


-- juror.def_denied definition

-- Drop table

-- DROP TABLE juror.def_denied;

CREATE TABLE juror.def_denied
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    date_def     timestamp(0) NOT NULL,
    exc_code     varchar(1)   NOT NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT def_denied_pkey PRIMARY KEY (part_no, owner)
);


-- juror.defer_dbf definition

-- Drop table

-- DROP TABLE juror.defer_dbf;

CREATE TABLE juror.defer_dbf
(
    "owner"  varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no  varchar(9)   NOT NULL,
    defer_to timestamp(0) NOT NULL,
    checked  varchar(1)   NULL,
    loc_code varchar(3)   NOT NULL,
    CONSTRAINT defer_dbf_pkey PRIMARY KEY (part_no, owner)
);


-- juror.dis_code definition

-- Drop table

-- DROP TABLE juror.dis_code;

CREATE TABLE juror.dis_code
(
    disq_code   varchar(1)  NOT NULL,
    description varchar(60) NOT NULL,
    enabled     varchar(1)  NULL,
    CONSTRAINT dis_code_pkey PRIMARY KEY (disq_code)
);


-- juror.disposition definition

-- Drop table

-- DROP TABLE juror.disposition;

CREATE TABLE juror.disposition
(
    disposition_code varchar(3)  NOT NULL,
    description      varchar(30) NOT NULL,
    CONSTRAINT disposition_pkey PRIMARY KEY (disposition_code)
);


-- juror.district definition

-- Drop table

-- DROP TABLE juror.district;

CREATE TABLE juror.district
(
    "owner" varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    tcode   varchar(3)  NOT NULL,
    tname   varchar(30) NULL,
    tcheck  numeric(38) NULL,
    CONSTRAINT district_pkey PRIMARY KEY (tcode, owner)
);


-- juror.error_log definition

-- Drop table

-- DROP TABLE juror.error_log;

CREATE TABLE juror.error_log
(
    "time_stamp" timestamp(0) NULL DEFAULT statement_timestamp(),
    job          varchar(50)  NULL,
    error_info   varchar(100) NULL
);


-- juror.exc_code definition

-- Drop table

-- DROP TABLE juror.exc_code;

CREATE TABLE juror.exc_code
(
    exc_code    varchar(1)  NOT NULL,
    description varchar(60) NOT NULL,
    by_right    varchar(1)  NULL,
    enabled     varchar(1)  NULL,
    CONSTRAINT exc_code_pkey PRIMARY KEY (exc_code)
);


-- juror.export_placeholders definition

-- Drop table

-- DROP TABLE juror.export_placeholders;

CREATE TABLE juror.export_placeholders
(
    placeholder_name   varchar(48)  NOT NULL,
    source_table_name  varchar(48)  NOT NULL,
    source_column_name varchar(48)  NOT NULL,
    "type"             varchar(12)  NOT NULL,
    description        varchar(100) NULL,
    default_value      varchar(200) NULL,
    editable           varchar(1)   NULL,
    validation_rule    varchar(600) NULL,
    validation_message varchar(200) NULL,
    validation_format  varchar(60)  NULL
);


-- juror.first_juror_digital_pool definition

-- Drop table

-- DROP TABLE juror.first_juror_digital_pool;

CREATE TABLE juror.first_juror_digital_pool
(
    loc_code     varchar(3)   NULL,
    pool_no      varchar(9)   NULL,
    date_created timestamp(0) NULL
);


-- juror.form_attr definition

-- Drop table

-- DROP TABLE juror.form_attr;

CREATE TABLE juror.form_attr
(
    form_type   varchar(6)  NOT NULL,
    dir_name    varchar(20) NOT NULL,
    max_rec_len int8        NULL
);


-- juror.forms_list definition

-- Drop table

-- DROP TABLE juror.forms_list;

CREATE TABLE juror.forms_list
(
    "owner"      varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    doc_name     varchar(8)  NOT NULL,
    description  varchar(30) NULL,
    path_name    varchar(40) NULL,
    word_program varchar(40) NULL,
    CONSTRAINT forms_list_pkey PRIMARY KEY (doc_name, owner)
);


-- juror.fta_lett definition

-- Drop table

-- DROP TABLE juror.fta_lett;

CREATE TABLE juror.fta_lett
(
    "owner"              varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no              varchar(9)   NOT NULL,
    date_fta             timestamp(0) NOT NULL,
    d_selected           timestamp(0) NOT NULL,
    printed              varchar(1)   NULL,
    date_printed         timestamp(0) NULL,
    no_show_printed      varchar(1)   NULL,
    no_show_date_printed timestamp(0) NULL,
    CONSTRAINT fta_lett_pkey PRIMARY KEY (part_no, date_fta, owner)
);
CREATE INDEX fta_lett_own_dt_idx ON juror.fta_lett USING btree (owner, date_printed);


-- juror.hk_run_log definition

-- Drop table

-- DROP TABLE juror.hk_run_log;

CREATE TABLE juror.hk_run_log
(
    seq_id         numeric(38)  NULL,
    start_time     timestamp(0) NULL,
    end_time       timestamp(0) NULL,
    jurors_deleted numeric(38)  NULL,
    jurors_error   numeric(38)  NULL,
    return_code    numeric(38)  NULL
);


-- juror.holidays definition

-- Drop table

-- DROP TABLE juror.holidays;

CREATE TABLE juror.holidays
(
    "owner"     varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    holiday     timestamp(0) NOT NULL,
    description varchar(30)  NOT NULL,
    CONSTRAINT holidays_pkey PRIMARY KEY (holiday, owner)
);


-- juror.ineligible_code definition

-- Drop table

-- DROP TABLE juror.ineligible_code;

CREATE TABLE juror.ineligible_code
(
    disposal_code int8         NOT NULL,
    description   varchar(150) NOT NULL,
    CONSTRAINT ineligible_code_pkey PRIMARY KEY (disposal_code)
);


-- juror.juror definition

-- Drop table

-- DROP TABLE juror.juror;

CREATE TABLE juror.juror
(
    id             varchar(9)   NOT NULL,
    surname        varchar(20)  NOT NULL,
    first_name     varchar(20)  NOT NULL,
    last_name      varchar(20)  NULL,
    dob            timestamp(0) NOT NULL,
    postcode       varchar(8)   NOT NULL,
    check_complete varchar(1)   NULL,
    disqualified   varchar(1)   NULL,
    try_count      int2         NULL,
    CONSTRAINT juror_chk_complete_chk CHECK (((check_complete)::text = ANY
                                              ((ARRAY ['Y'::character varying, 'N'::character varying])::text[]))),
    CONSTRAINT juror_disq_chk CHECK (((disqualified)::text = ANY
                                      ((ARRAY ['Y'::character varying, 'N'::character varying])::text[]))),
    CONSTRAINT juror_pkey PRIMARY KEY (id)
);
CREATE INDEX juror_index_02 ON juror.juror USING btree (surname);
CREATE INDEX juror_index_03 ON juror.juror USING btree (surname, first_name);
CREATE INDEX juror_index_04 ON juror.juror USING btree (surname, first_name, last_name);
CREATE INDEX juror_index_05 ON juror.juror USING btree (surname, first_name, last_name, dob);
CREATE INDEX juror_index_06 ON juror.juror USING btree (surname, first_name, last_name, dob, postcode);


-- juror.juror_court_police_check definition

-- Drop table

-- DROP TABLE juror.juror_court_police_check;

CREATE TABLE juror.juror_court_police_check
(
    "owner"        varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    id             varchar(9)   NOT NULL,
    surname        varchar(20)  NOT NULL,
    first_name     varchar(20)  NOT NULL,
    last_name      varchar(20)  NULL,
    dob            timestamp(0) NOT NULL,
    postcode       varchar(8)   NOT NULL,
    check_complete varchar(1)   NULL,
    disqualified   varchar(1)   NULL,
    try_count      int2         NULL,
    CONSTRAINT juror_court_pnc_disq_chk CHECK (((disqualified)::text = ANY
                                                ((ARRAY ['Y'::character varying, 'N'::character varying])::text[]))),
    CONSTRAINT juror_court_police_check_pkey PRIMARY KEY (id, owner),
    CONSTRAINT juror_cpnc_chk_complete_chk CHECK (((check_complete)::text = ANY
                                                   ((ARRAY ['Y'::character varying, 'N'::character varying])::text[])))
);


-- juror.juror_tables definition

-- Drop table

-- DROP TABLE juror.juror_tables;

CREATE TABLE juror.juror_tables
(
    "owner"             varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    pb_table_name       varchar(30) NOT NULL,
    description         varchar(30) NOT NULL,
    internal            varchar(1)  NOT NULL,
    pb_edit_dw_name     varchar(30) NOT NULL,
    pb_report_dw_name   varchar(30) NULL,
    pb_report_dw_orient varchar(1)  NULL,
    select_enable       varchar(1)  NULL,
    edit_enable         varchar(1)  NULL,
    insert_enable       varchar(1)  NULL,
    delete_enable       varchar(1)  NULL,
    CONSTRAINT juror_tables_pkey PRIMARY KEY (pb_table_name, owner)
);


-- juror."location" definition

-- Drop table

-- DROP TABLE juror."location";

CREATE TABLE juror."location"
(
    "owner"     varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    "location"  varchar(6)  NOT NULL,
    description varchar(30) NOT NULL,
    CONSTRAINT location_pkey PRIMARY KEY (location, owner)
);


-- juror.manuals_status definition

-- Drop table

-- DROP TABLE juror.manuals_status;

CREATE TABLE juror.manuals_status
(
    code        bpchar(1)   NOT NULL,
    description varchar(60) NOT NULL,
    CONSTRAINT manuals_status_pkey PRIMARY KEY (code)
);


-- juror.messages definition

-- Drop table

-- DROP TABLE juror.messages;

CREATE TABLE juror.messages
(
    part_no       varchar(9)    NOT NULL,
    file_datetime varchar(15)   NOT NULL,
    username      varchar(20)   NOT NULL,
    loc_code      varchar(3)    NOT NULL,
    phone         varchar(15)   NULL,
    email         varchar(254)  NULL,
    loc_name      varchar(100)  NULL,
    pool_no       varchar(9)    NULL,
    subject       varchar(50)   NULL,
    message_text  varchar(2000) NULL,
    message_id    int8          NOT NULL,
    message_read  varchar(2)    NULL DEFAULT 'NR'::character varying,
    CONSTRAINT messages_pkey PRIMARY KEY (part_no, file_datetime, username, loc_code)
);


-- juror.module_machine_xref definition

-- Drop table

-- DROP TABLE juror.module_machine_xref;

CREATE TABLE juror.module_machine_xref
(
    machine_name   varchar(24)  NOT NULL,
    juror_user     varchar(20)  NOT NULL,
    module_name    varchar(30)  NOT NULL,
    release_number varchar(40)  NULL,
    revision_no    varchar(24)  NULL,
    last_update    timestamp(0) NULL,
    CONSTRAINT module_machine_xref_pkey PRIMARY KEY (machine_name, juror_user, module_name)
);


-- juror.new_dob definition

-- Drop table

-- DROP TABLE juror.new_dob;

CREATE TABLE juror.new_dob
(
    "owner"   varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    snum      varchar(10)  NOT NULL,
    not_known varchar(10)  NULL,
    sin       varchar(11)  NULL,
    race      varchar(1)   NULL,
    gender    varchar(1)   NULL,
    dob       timestamp(0) NULL,
    finitial  varchar(1)   NULL,
    sinitial  varchar(1)   NULL,
    CONSTRAINT new_dob_pkey PRIMARY KEY (snum, owner)
);


-- juror.panel definition

-- Drop table

-- DROP TABLE juror.panel;

CREATE TABLE juror.panel
(
    "owner"       varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no       varchar(9)   NOT NULL,
    trial_no      varchar(16)  NOT NULL,
    rand_no       numeric(38)  NULL,
    date_selected timestamp(0) NOT NULL,
    "result"      bpchar(2)    NULL,
    complete      varchar(1)   NULL,
    CONSTRAINT panel_pkey PRIMARY KEY (part_no, trial_no, owner)
);
CREATE INDEX panel_trial_no ON juror.panel USING btree (trial_no, owner);


-- juror.panel_group definition

-- Drop table

-- DROP TABLE juror.panel_group;

CREATE TABLE juror.panel_group
(
    "owner"  varchar(3) NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    pool_no  varchar(9) NOT NULL,
    group_no varchar(3) NOT NULL,
    CONSTRAINT panel_group_pkey PRIMARY KEY (pool_no, group_no, owner)
);


-- juror.part_amendments definition

-- Drop table

-- DROP TABLE juror.part_amendments;

CREATE TABLE juror.part_amendments
(
    "owner"          varchar(3)   NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no          bpchar(9)    NOT NULL,
    edit_date        timestamp(0) NOT NULL,
    edit_userid      varchar(20)  NOT NULL,
    title            varchar(10)  NULL,
    fname            varchar(20)  NULL,
    lname            varchar(20)  NULL,
    dob              timestamp(0) NULL,
    address          varchar(175) NULL,
    zip              varchar(10)  NULL,
    sort_code        bpchar(6)    NULL,
    bank_acct_name   varchar(18)  NULL,
    bank_acct_no     bpchar(8)    NULL,
    bldg_soc_roll_no varchar(18)  NULL,
    pool_no          bpchar(9)    NULL
);
CREATE INDEX part_amendments_edit_date ON juror.part_amendments USING btree (edit_date, owner);
CREATE INDEX part_amendments_part_no ON juror.part_amendments USING btree (part_no, owner);
CREATE INDEX part_amendments_pool_no ON juror.part_amendments USING btree (pool_no, owner);


-- juror.part_expenses definition

-- Drop table

-- DROP TABLE juror.part_expenses;

CREATE TABLE juror.part_expenses
(
    "owner"              varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no              varchar(9)   NOT NULL,
    att_date             timestamp(0) NOT NULL,
    number_atts          int2         NOT NULL,
    end_date             timestamp(0) NULL,
    mcycles_pass         int2         NULL,
    mcars_pass           int2         NULL,
    mileage              int2         NULL,
    mileage_rate         float8       NULL,
    total_mileage        float8       NULL,
    public_parking       float8       NULL,
    public_parking_days  float8       NULL,
    public_parking_total float8       NULL,
    public_trans         float8       NULL,
    child_care           float8       NULL,
    child_care_rate      float8       NULL,
    child_care_days      int2         NULL,
    misc_description     varchar(20)  NULL,
    misc_amount          float8       NULL,
    total_amount         float8       NULL,
    rail_bus_days        int2         NULL,
    rail_bus             float8       NULL,
    rail_bus_total       float8       NULL,
    hired_vehicle_days   int2         NULL,
    hired_vehicle        float8       NULL,
    hired_vehicle_total  float8       NULL,
    mcycles_days         int2         NULL,
    rate_mcycles         float8       NULL,
    mcycles_total        float8       NULL,
    mcars_days           int2         NULL,
    rate_mcars           float8       NULL,
    mcars_total          float8       NULL,
    pcycles_days         int2         NULL,
    rate_pcycles         float8       NULL,
    pcycles_total        float8       NULL,
    std_mcycles_days     int2         NULL,
    std_mcycles          float8       NULL,
    std_mcycles_total    float8       NULL,
    std_mcars_days       int2         NULL,
    std_mcars            float8       NULL,
    std_mcars_total      float8       NULL,
    los_lfour_days       int2         NULL,
    loss_lfour           float8       NULL,
    los_lfour_total      float8       NULL,
    los_mfour_days       int2         NULL,
    loss_mfour           float8       NULL,
    los_mfour_total      float8       NULL,
    loss_mten_days       int2         NULL,
    loss_mten            float8       NULL,
    loss_mten_total      float8       NULL,
    subs_lfive_days      int2         NULL,
    subs_lfive           float8       NULL,
    subs_lfive_total     float8       NULL,
    subs_mfive_days      int2         NULL,
    subs_mfive           float8       NULL,
    subs_mfive_total     float8       NULL,
    loss_oten_days       int2         NULL,
    loss_oten            float8       NULL,
    loss_oten_total      float8       NULL,
    loss_oten_h_total    float8       NULL,
    loss_overnight_days  float8       NULL,
    loss_overnight       float8       NULL,
    loss_overnight_total float8       NULL,
    amt_spent            float8       NULL,
    travel_time          float4       NULL,
    daily_time           float4       NULL,
    total_time           float4       NULL,
    non_attendance       varchar(1)   NULL,
    date_aramis_created  timestamp(0) NULL,
    aramis_created       varchar(1)   NULL,
    exp_subs_date        timestamp(0) NULL,
    pay_cash             varchar(1)   NULL,
    pay_accepted         varchar(1)   NULL,
    user_id              varchar(20)  NULL,
    CONSTRAINT part_expenses_pkey PRIMARY KEY (part_no, att_date, number_atts, owner)
)
    WITH (
        fillfactor = 40
        );


-- juror."password" definition

-- Drop table

-- DROP TABLE juror."password";

CREATE TABLE juror."password"
(
    "owner"               varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    login                 varchar(20)  NOT NULL,
    "password"            varchar(20)  NOT NULL,
    last_used             timestamp(0) NULL,
    user_level            varchar(1)   NOT NULL,
    aramis_auth_code      varchar(9)   NULL,
    aramis_max_auth       float8       NULL,
    password_changed_date timestamp(0) NULL,
    login_enabled_yn      varchar(1)   NULL     DEFAULT 'Y'::character varying,
    CONSTRAINT password_pkey PRIMARY KEY (login, owner)
);


-- juror.password_export_placeholders definition

-- Drop table

-- DROP TABLE juror.password_export_placeholders;

CREATE TABLE juror.password_export_placeholders
(
    "owner"          varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    login            varchar(20) NOT NULL,
    placeholder_name varchar(48) NOT NULL,
    use              varchar(1)  NULL
);


-- juror.peak_usage definition

-- Drop table

-- DROP TABLE juror.peak_usage;

CREATE TABLE juror.peak_usage
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    vd_date      timestamp(0) NOT NULL,
    no_attend    numeric(38)  NULL,
    peak_no      numeric(38)  NULL,
    no_scheduled numeric(38)  NULL,
    no_panels    numeric(38)  NULL,
    CONSTRAINT peak_usage_pkey PRIMARY KEY (vd_date, owner)
);


-- juror.phoenix_temp definition

-- Drop table

-- DROP TABLE juror.phoenix_temp;

CREATE TABLE juror.phoenix_temp
(
    part_no       varchar(9)   NOT NULL,
    last_name     varchar(20)  NOT NULL,
    first_name    varchar(20)  NOT NULL,
    postcode      varchar(10)  NOT NULL,
    date_of_birth timestamp(0) NOT NULL,
    "result"      varchar(1)   NULL,
    checked       varchar(1)   NULL
);


-- juror.phone_log definition

-- Drop table

-- DROP TABLE juror.phone_log;

CREATE TABLE juror.phone_log
(
    "owner"     varchar(3)    NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no     varchar(9)    NOT NULL,
    user_id     varchar(20)   NOT NULL,
    start_call  timestamp(0)  NOT NULL,
    end_call    timestamp(0)  NULL,
    phone_code  varchar(2)    NOT NULL,
    notes       varchar(2000) NULL,
    last_update timestamp(0)  NULL,
    CONSTRAINT phone_log_pkey PRIMARY KEY (part_no, start_call, owner)
);
CREATE INDEX phone_log_part_no ON juror.phone_log USING btree (part_no, start_call, last_update);


-- juror.pl_xml_status definition

-- Drop table

-- DROP TABLE juror.pl_xml_status;

CREATE TABLE juror.pl_xml_status
(
    sequence_no      int8         NOT NULL,
    circuit_name     varchar(100) NOT NULL,
    shell_circuit_id varchar(277) NOT NULL,
    date_time_stamp  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status           varchar(1)   NOT NULL,
    CONSTRAINT pl_xml_status_chk CHECK (((status)::text = ANY
                                         ((ARRAY ['S'::character varying, 'H'::character varying, 'F'::character varying, 'X'::character varying])::text[]))),
    CONSTRAINT pl_xml_status_pkey PRIMARY KEY (sequence_no)
);


-- juror.pnc_proxy definition

-- Drop table

-- DROP TABLE juror.pnc_proxy;

CREATE TABLE juror.pnc_proxy
(
    juror_id        int4         NOT NULL,
    disposal_code   int2         NULL,
    effective_date  timestamp(0) NULL,
    qual_literal    varchar(3)   NULL,
    sentence_amount int8         NULL,
    sentence_period varchar(3)   NULL,
    on_bail         varchar(1)   NULL
);


-- juror.pool_stats definition

-- Drop table

-- DROP TABLE juror.pool_stats;

CREATE TABLE juror.pool_stats
(
    "owner"          varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    pool_no          varchar(9)   NOT NULL,
    att_date         timestamp(0) NOT NULL,
    no_summoned      numeric(38)  NOT NULL,
    no_responded     numeric(38)  NOT NULL,
    no_attended      numeric(38)  NOT NULL,
    no_excused       numeric(38)  NOT NULL,
    no_disqualified  numeric(38)  NOT NULL,
    no_deferred      numeric(38)  NOT NULL,
    no_reassigned    numeric(38)  NOT NULL,
    no_undeliverable numeric(38)  NOT NULL,
    no_transferred   numeric(38)  NOT NULL,
    no_fta           numeric(38)  NOT NULL,
    CONSTRAINT pool_stats_pkey PRIMARY KEY (pool_no, owner)
);


-- juror.pool_status definition

-- Drop table

-- DROP TABLE juror.pool_status;

CREATE TABLE juror.pool_status
(
    status      numeric(38) NOT NULL,
    status_desc varchar(15) NOT NULL,
    active      varchar(1)  NULL,
    CONSTRAINT pool_status_pkey PRIMARY KEY (status)
);


-- juror.pool_transfer_weekday definition

-- Drop table

-- DROP TABLE juror.pool_transfer_weekday;

CREATE TABLE juror.pool_transfer_weekday
(
    transfer_day varchar(3) NULL,
    run_day      varchar(3) NULL,
    adjustment   int2       NULL
);


-- juror.pool_type definition

-- Drop table

-- DROP TABLE juror.pool_type;

CREATE TABLE juror.pool_type
(
    pool_type      varchar(3)  NOT NULL,
    pool_type_desc varchar(20) NOT NULL,
    CONSTRAINT pool_type_pkey PRIMARY KEY (pool_type)
);


-- juror.postpone_lett definition

-- Drop table

-- DROP TABLE juror.postpone_lett;

CREATE TABLE juror.postpone_lett
(
    "owner"       varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no       varchar(9)   NOT NULL,
    date_postpone timestamp(0) NOT NULL,
    printed       varchar(1)   NULL,
    date_printed  timestamp(0) NULL,
    CONSTRAINT postpone_lett_pkey PRIMARY KEY (part_no, owner)
);
CREATE INDEX postpone_lett_own_dt_idx ON juror.postpone_lett USING btree (owner, date_printed);


-- juror.print_files definition

-- Drop table

-- DROP TABLE juror.print_files;

CREATE TABLE juror.print_files
(
    printfile_name varchar(15)   NOT NULL,
    creation_date  timestamp(0)  NOT NULL DEFAULT statement_timestamp(),
    form_type      varchar(6)    NOT NULL,
    detail_rec     varchar(1260) NOT NULL,
    extracted_flag varchar(1)    NULL,
    part_no        varchar(9)    NOT NULL,
    digital_comms  varchar(1)    NULL     DEFAULT 'N'::character varying,
    CONSTRAINT print_files_pkey PRIMARY KEY (part_no, printfile_name, creation_date)
);
CREATE INDEX print_files_flag_fbi ON juror.print_files USING btree ((
                                                                        CASE
                                                                            WHEN (extracted_flag IS NULL) THEN 'N'::text
                                                                            ELSE NULL::text
                                                                            END));


-- juror.race_records definition

-- Drop table

-- DROP TABLE juror.race_records;

CREATE TABLE juror.race_records
(
    "owner"       varchar(3) NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    bf_no         float8     NULL,
    bm_no         float8     NULL,
    of_no         float8     NULL,
    om_no         float8     NULL,
    wf_no         float8     NULL,
    wm_no         float8     NULL,
    record_number float8     NOT NULL,
    CONSTRAINT race_records_pkey PRIMARY KEY (record_number, owner)
);


-- juror.release_lett definition

-- Drop table

-- DROP TABLE juror.release_lett;

CREATE TABLE juror.release_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    date_release timestamp(0) NOT NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT release_lett_pkey PRIMARY KEY (part_no, owner)
);
CREATE INDEX rel_lett_own_dt_idx ON juror.release_lett USING btree (owner, date_printed);


-- juror.request_lett definition

-- Drop table

-- DROP TABLE juror.request_lett;

CREATE TABLE juror.request_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    req_info     varchar(210) NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT request_lett_pkey PRIMARY KEY (part_no, owner)
);
CREATE INDEX req_lett_own_dt_idx ON juror.request_lett USING btree (owner, date_printed);


-- juror.seating_plan definition

-- Drop table

-- DROP TABLE juror.seating_plan;

CREATE TABLE juror.seating_plan
(
    "owner"      varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    seating_plan numeric(38) NOT NULL,
    description  varchar(30) NULL,
    seats_deep   numeric(38) NOT NULL,
    seats_wide   numeric(38) NOT NULL,
    CONSTRAINT seating_plan_pkey PRIMARY KEY (seating_plan, owner)
);


-- juror.security_level definition

-- Drop table

-- DROP TABLE juror.security_level;

CREATE TABLE juror.security_level
(
    "owner"      varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    login        varchar(20) NOT NULL,
    menu_id      numeric(38) NOT NULL,
    description  varchar(80) NOT NULL,
    allow_use    varchar(1)  NOT NULL,
    module_code  varchar(1)  NULL,
    menu_heading varchar(20) NULL,
    CONSTRAINT security_level_pkey PRIMARY KEY (login, menu_id, owner)
);


-- juror.stat_standards definition

-- Drop table

-- DROP TABLE juror.stat_standards;

CREATE TABLE juror.stat_standards
(
    "owner"           varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    "location"        varchar(10) NOT NULL,
    pbi               numeric(38) NULL,
    voir_dire_attend  float8      NULL,
    zero_days         float8      NULL,
    unused_juror_days numeric(38) NULL,
    CONSTRAINT stat_standards_pkey PRIMARY KEY (location, owner)
);


-- juror.states definition

-- Drop table

-- DROP TABLE juror.states;

CREATE TABLE juror.states
(
    "owner"    varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    state_id   varchar(2)  NOT NULL,
    state_name varchar(24) NULL,
    state_capi varchar(24) NULL,
    country    varchar(3)  NULL,
    CONSTRAINT states_pkey PRIMARY KEY (state_id, owner)
);


-- juror.system_file definition

-- Drop table

-- DROP TABLE juror.system_file;

CREATE TABLE juror.system_file
(
    "owner"              varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    record_no            numeric(38) NOT NULL,
    pool_rate1           float8      NOT NULL,
    pool_rate2           float8      NOT NULL,
    after_p1             float8      NOT NULL,
    travel_rate          float8      NOT NULL,
    public_trans         float8      NOT NULL,
    child_care_rate      float8      NOT NULL,
    jury_rate1           float8      NOT NULL,
    jury_rate2           float8      NOT NULL,
    after_j1             float8      NOT NULL,
    pool_audit_no        float8      NOT NULL,
    jury_audit_no        float8      NOT NULL,
    financial_audit_no   float8      NOT NULL,
    acct_pay_audit_no    float8      NOT NULL,
    word_processor       varchar(20) NULL,
    pay_state_empl       varchar(1)  NOT NULL,
    check_for_bioform    varchar(1)  NOT NULL,
    ins_hour_rate        float8      NULL,
    voters_count         numeric(38) NULL,
    create_new_pool      numeric(38) NULL,
    gather_fta           varchar(1)  NULL,
    exc_letter           varchar(1)  NULL,
    denied_letter        varchar(1)  NULL,
    total_source         float8      NULL,
    available_recs       int8        NULL,
    no_months_fta        float8      NULL,
    batch_no             int8        NULL,
    print_sequence       varchar(4)  NULL,
    print_index          varchar(4)  NULL,
    print_transmission   varchar(2)  NULL,
    print_excused        varchar(4)  NULL,
    allow_multi_defer    varchar(1)  NULL,
    start_offset         int8        NULL,
    no_days_to_process   int8        NULL,
    confirm_start_offset int8        NULL,
    confirm_day_range    int8        NULL,
    phoenix_delay        numeric(38) NULL,
    cor_max_pool_total   int4        NULL,
    CONSTRAINT system_file_pkey PRIMARY KEY (record_no, owner)
);


-- juror.system_parameter definition

-- Drop table

-- DROP TABLE juror.system_parameter;

CREATE TABLE juror.system_parameter
(
    sp_id        int8         NOT NULL,
    sp_desc      varchar(80)  NOT NULL,
    sp_value     varchar(200) NOT NULL,
    created_by   varchar(20)  NULL,
    created_date timestamp(0) NULL,
    updated_by   varchar(20)  NULL,
    updated_date timestamp(0) NULL,
    CONSTRAINT system_parameter_pkey PRIMARY KEY (sp_id)
);



-- juror.t_donation definition

-- Drop table

-- DROP TABLE juror.t_donation;

CREATE TABLE juror.t_donation
(
    "owner"       varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    donation_code varchar(4)  NOT NULL,
    description   varchar(40) NULL,
    CONSTRAINT t_donation_pkey PRIMARY KEY (donation_code, owner)
);


-- juror.t_history_code definition

-- Drop table

-- DROP TABLE juror.t_history_code;

CREATE TABLE juror.t_history_code
(
    history_code varchar(4)  NOT NULL,
    description  varchar(40) NOT NULL,
    CONSTRAINT t_history_code_pkey PRIMARY KEY (history_code)
);


-- juror.t_id_check definition

-- Drop table

-- DROP TABLE juror.t_id_check;

CREATE TABLE juror.t_id_check
(
    id_check    varchar(1)  NOT NULL,
    description varchar(20) NOT NULL,
    CONSTRAINT t_id_check_pkey PRIMARY KEY (id_check)
);


-- juror.t_jurisdiction definition

-- Drop table

-- DROP TABLE juror.t_jurisdiction;

CREATE TABLE juror.t_jurisdiction
(
    "owner"           varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    jurisdiction_code varchar(1)  NOT NULL,
    description       varchar(12) NOT NULL,
    CONSTRAINT t_jurisdiction_pkey PRIMARY KEY (jurisdiction_code, owner)
);


-- juror.t_loc_juris_type definition

-- Drop table

-- DROP TABLE juror.t_loc_juris_type;

CREATE TABLE juror.t_loc_juris_type
(
    "owner"           varchar(3) NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    loc_code          varchar(3) NOT NULL,
    jurisdiction_code varchar(1) NULL,
    pool_type         varchar(3) NULL,
    CONSTRAINT t_loc_juris_type_pkey PRIMARY KEY (loc_code, owner)
);


-- juror.t_message_template definition

-- Drop table

-- DROP TABLE juror.t_message_template;

CREATE TABLE juror.t_message_template
(
    message_scope   varchar(6)    NOT NULL,
    message_id      numeric(38)   NOT NULL,
    message_title   varchar(27)   NOT NULL,
    message_subject varchar(100)  NOT NULL,
    message_text    varchar(2000) NULL,
    display_order   numeric(38)   NULL
);


-- juror.t_mileage definition

-- Drop table

-- DROP TABLE juror.t_mileage;

CREATE TABLE juror.t_mileage
(
    "owner" varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    zip     varchar(10) NOT NULL,
    city    varchar(20) NOT NULL,
    mileage float8      NULL,
    CONSTRAINT t_mileage_pkey PRIMARY KEY (zip, city, owner)
);


-- juror.t_occupation definition

-- Drop table

-- DROP TABLE juror.t_occupation;

CREATE TABLE juror.t_occupation
(
    "owner"             varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    occupation_code     varchar(4)  NOT NULL,
    occupation_descript varchar(30) NULL,
    CONSTRAINT t_occupation_pkey PRIMARY KEY (occupation_code, owner)
);


-- juror.t_phone definition

-- Drop table

-- DROP TABLE juror.t_phone;

CREATE TABLE juror.t_phone
(
    phone_code  varchar(2)  NOT NULL,
    description varchar(60) NOT NULL,
    CONSTRAINT t_phone_pkey PRIMARY KEY (phone_code)
);


-- juror.t_police definition

-- Drop table

-- DROP TABLE juror.t_police;

CREATE TABLE juror.t_police
(
    police_check varchar(1)  NOT NULL,
    description  varchar(20) NULL
);


-- juror.t_race_code definition

-- Drop table

-- DROP TABLE juror.t_race_code;

CREATE TABLE juror.t_race_code
(
    "owner"    varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    race_code  varchar(1)  NOT NULL,
    race_descp varchar(30) NULL,
    CONSTRAINT t_race_code_pkey PRIMARY KEY (race_code, owner)
);


-- juror.t_special definition

-- Drop table

-- DROP TABLE juror.t_special;

CREATE TABLE juror.t_special
(
    spec_need   varchar(1)  NOT NULL,
    description varchar(60) NOT NULL,
    CONSTRAINT t_special_pkey PRIMARY KEY (spec_need)
);


-- juror.t_type definition

-- Drop table

-- DROP TABLE juror.t_type;

CREATE TABLE juror.t_type
(
    t_type      varchar(3)  NOT NULL,
    description varchar(30) NOT NULL,
    CONSTRAINT t_type_pkey PRIMARY KEY (t_type)
);


-- juror.temp_missing_payment_data definition

-- Drop table

-- DROP TABLE juror.temp_missing_payment_data;

CREATE TABLE juror.temp_missing_payment_data
(
    "owner"          varchar(3)   NULL,
    loc_code         varchar(3)   NULL,
    total_amount     int8         NULL,
    part_no          varchar(9)   NULL,
    sort_code        varchar(6)   NULL,
    bank_acct_name   varchar(18)  NULL,
    bank_acct_no     varchar(8)   NULL,
    bldg_soc_roll_no varchar(18)  NULL,
    address          varchar(35)  NOT NULL,
    address2         varchar(35)  NULL,
    address3         varchar(35)  NULL,
    address4         varchar(35)  NULL,
    address5         varchar(35)  NULL,
    postcode         varchar(10)  NULL,
    aramis_auth_code varchar(9)   NULL,
    "name"           varchar(52)  NULL,
    loc_cost_centre  varchar(7)   NULL,
    loe_tot          int8         NULL,
    travel_tot       int8         NULL,
    subs_tot         int8         NULL,
    exp_subs_date    timestamp(0) NULL
);


-- juror.tempjuror definition

-- Drop table

-- DROP TABLE juror.tempjuror;

CREATE TABLE juror.tempjuror
(
    id             varchar(9)   NULL,
    surname        varchar(20)  NOT NULL,
    first_name     varchar(20)  NOT NULL,
    last_name      varchar(20)  NULL,
    dob            timestamp(0) NOT NULL,
    postcode       varchar(8)   NOT NULL,
    check_complete varchar(1)   NULL,
    disqualified   varchar(1)   NULL,
    try_count      int2         NULL
);


-- juror.tempphoenixtemp definition

-- Drop table

-- DROP TABLE juror.tempphoenixtemp;

CREATE TABLE juror.tempphoenixtemp
(
    part_no       varchar(9)   NOT NULL,
    last_name     varchar(20)  NOT NULL,
    first_name    varchar(20)  NOT NULL,
    postcode      varchar(10)  NOT NULL,
    date_of_birth timestamp(0) NOT NULL,
    "result"      varchar(1)   NULL,
    checked       varchar(1)   NULL
);


-- juror.trial_can_reas definition

-- Drop table

-- DROP TABLE juror.trial_can_reas;

CREATE TABLE juror.trial_can_reas
(
    cancel_reason varchar(4)  NOT NULL,
    description   varchar(30) NOT NULL,
    CONSTRAINT trial_can_reas_pkey PRIMARY KEY (cancel_reason)
);


-- juror.trial_charge_code definition

-- Drop table

-- DROP TABLE juror.trial_charge_code;

CREATE TABLE juror.trial_charge_code
(
    final_charge_code varchar(6)  NOT NULL,
    description       varchar(30) NOT NULL,
    CONSTRAINT trial_charge_code_pkey PRIMARY KEY (final_charge_code)
);


-- juror.undelivr definition

-- Drop table

-- DROP TABLE juror.undelivr;

CREATE TABLE juror.undelivr
(
    "owner"     varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no     varchar(9)  NOT NULL,
    loc_code    varchar(3)  NULL,
    poll_number varchar(5)  NULL,
    title       varchar(10) NULL,
    lname       varchar(20) NULL,
    fname       varchar(20) NULL,
    address     varchar(35) NULL,
    address2    varchar(35) NULL,
    address3    varchar(35) NULL,
    address4    varchar(35) NULL,
    address5    varchar(35) NULL,
    address6    varchar(35) NULL,
    zip         varchar(10) NULL,
    CONSTRAINT undelivr_pkey PRIMARY KEY (part_no, owner)
);


-- juror.user_level_menus definition

-- Drop table

-- DROP TABLE juror.user_level_menus;

CREATE TABLE juror.user_level_menus
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    menu_id      int4         NOT NULL,
    module_code  varchar(1)   NULL,
    menu_heading varchar(20)  NULL,
    description  varchar(100) NULL,
    allow_use    varchar(1)   NULL,
    level1       varchar(1)   NULL,
    level2       varchar(1)   NULL,
    level3       varchar(1)   NULL,
    level4       varchar(1)   NULL,
    level5       varchar(1)   NULL,
    level9       varchar(1)   NULL     DEFAULT 'N'::character varying,
    CONSTRAINT user_level_menus_pkey PRIMARY KEY (menu_id, owner)
);


-- juror.venire definition

-- Drop table

-- DROP TABLE juror.venire;

CREATE TABLE juror.venire
(
    "owner"           varchar(3)   NULL DEFAULT current_setting('juror_app.owner'::text, true),
    login             varchar(20)  NOT NULL,
    part_no           varchar(9)   NOT NULL,
    lname             varchar(20)  NULL,
    fname             varchar(20)  NULL,
    address           varchar(40)  NULL,
    city              varchar(20)  NULL,
    state             varchar(2)   NULL,
    zip               varchar(10)  NULL,
    loc_code          varchar(3)   NULL,
    jurisdiction_code varchar(1)   NULL,
    voter_reg         varchar(11)  NULL,
    ret_date          timestamp(0) NULL,
    CONSTRAINT venire_pkey PRIMARY KEY (login, part_no)
);
-- juror.abaccus definition

-- Drop table

-- DROP TABLE juror.abaccus;

CREATE TABLE juror.abaccus
(
    form_type       varchar(6)   NOT NULL,
    loc_code        varchar(3)   NOT NULL,
    creation_date   timestamp(0) NOT NULL,
    number_of_items int4         NULL,
    CONSTRAINT abccs_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code)
);
CREATE UNIQUE INDEX abaccus_pk ON juror.abaccus USING btree (form_type, loc_code, creation_date);


-- juror.appearances definition

-- Drop table

-- DROP TABLE juror.appearances;

CREATE TABLE juror.appearances
(
    "owner"        varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no        varchar(9)   NOT NULL,
    att_date       timestamp(0) NOT NULL,
    faudit         varchar(11)  NULL,
    cheque_no      varchar(10)  NULL,
    court_emp      varchar(1)   NULL,
    timein         timestamp(0) NULL,
    timeout        timestamp(0) NULL,
    pool_trial_no  varchar(16)  NULL,
    audits         varchar(11)  NULL,
    amount         float8       NULL,
    reason         varchar(1)   NULL,
    app_stage      int2         NULL,
    pay_county_emp varchar(1)   NULL,
    loc_code       varchar(3)   NOT NULL,
    pay_expenses   varchar(1)   NULL,
    date_paid      timestamp(0) NULL,
    donation_code  varchar(4)   NULL,
    non_attendance varchar(1)   NULL,
    CONSTRAINT appearances_pkey PRIMARY KEY (part_no, att_date, loc_code, owner),
    CONSTRAINT app_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code)
)
    WITH (
        fillfactor = 60
        );
CREATE INDEX app_appstg_own ON juror.appearances USING btree (app_stage, owner);
CREATE INDEX app_audit ON juror.appearances USING btree (part_no, audits, owner);


-- juror.attendance definition

-- Drop table

-- DROP TABLE juror.attendance;

CREATE TABLE juror.attendance
(
    "owner"         varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    month_start     timestamp(0) NOT NULL,
    loc_code        varchar(3)   NOT NULL,
    available_days  int4         NULL,
    attendance_days int4         NULL,
    sitting_days    int4         NULL,
    no_trials       int4         NULL,
    last_update     timestamp(0) NULL,
    CONSTRAINT attendance_pkey PRIMARY KEY (month_start, loc_code, owner),
    CONSTRAINT attendance_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code)
);
CREATE INDEX attendance_locde_mths_own ON juror.attendance USING btree (loc_code, month_start, owner);


-- juror.audit_f_report definition

-- Drop table

-- DROP TABLE juror.audit_f_report;

CREATE TABLE juror.audit_f_report
(
    "owner"                varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    faudit                 varchar(11)  NOT NULL,
    line_no                int8         NOT NULL,
    part_no                varchar(9)   NOT NULL,
    pool_trial_no          varchar(16)  NULL,
    reason                 varchar(1)   NULL,
    total_amount           float8       NULL,
    amount                 float8       NULL,
    att_date               timestamp(0) NULL,
    lname                  varchar(20)  NULL,
    is_active              varchar(1)   NULL,
    fname                  varchar(20)  NULL,
    cheque_no              varchar(10)  NULL,
    date_paid              timestamp(0) NULL,
    address                varchar(40)  NULL,
    city                   varchar(20)  NULL,
    state                  varchar(2)   NULL,
    zip                    varchar(10)  NULL,
    pay_county             varchar(1)   NULL,
    donation_code          varchar(4)   NULL,
    address1               varchar(35)  NULL,
    address2               varchar(35)  NULL,
    address3               varchar(35)  NULL,
    address4               varchar(35)  NULL,
    address5               varchar(35)  NULL,
    address6               varchar(35)  NULL,
    postcode               varchar(10)  NULL,
    total_travel           float8       NULL,
    total_subsistance      float8       NULL,
    total_loss_of_earnings float8       NULL,
    travel_code            varchar(13)  NULL,
    subsistence_code       varchar(13)  NULL,
    loss_of_earnings_code  varchar(13)  NULL,
    amt_spent              float8       NULL,
    user_id                varchar(20)  NULL,
    loc_code               varchar(3)   NULL,
    loc_rate_mcycles       float8       NULL,
    loc_rate_mcars         float8       NULL,
    loc_rate_pcycles       float8       NULL,
    loc_standard_mcycles   float8       NULL,
    loc_standard_mcars     float8       NULL,
    loc_loss_lfour         float8       NULL,
    loc_loss_mfour         float8       NULL,
    loc_loss_mten          float8       NULL,
    loc_subs_lfive         float8       NULL,
    loc_subs_mfive         float8       NULL,
    loc_loss_oten          float8       NULL,
    loc_loss_overnight     float8       NULL,
    loc_child_care         float8       NULL,
    loc_misc_amount        float8       NULL,
    loc_rail_bus           float8       NULL,
    loc_hired_vehicle      float8       NULL,
    loc_rate_mcycles_2     float8       NULL,
    loc_rate_mcars_2       float8       NULL,
    loc_rate_mcars_3       float8       NULL,
    loc_loss_on_half       float8       NULL,
    loc_ac_loss_of_earn    varchar(13)  NULL,
    loc_ac_subst           varchar(13)  NULL,
    loc_ac_travel          varchar(13)  NULL,
    loc_cost_centre        varchar(7)   NULL,
    report_type            varchar(30)  NULL,
    default_mileage        numeric(38)  NULL,
    default_paid_cash      varchar(1)   NULL,
    default_travel_time    float4       NULL,
    default_financial_loss float8       NULL,
    default_amt_spent      float8       NULL,
    smart_card             varchar(20)  NULL,
    sort_code              varchar(6)   NULL,
    bank_acct_name         varchar(18)  NULL,
    bank_acct_no           varchar(8)   NULL,
    bldg_soc_roll_no       varchar(18)  NULL,
    CONSTRAINT audit_f_report_pkey PRIMARY KEY (faudit, owner, line_no, part_no),
    CONSTRAINT adt_f_rpt_loc_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code)
);
CREATE INDEX adt_f_rpt_faudit_lr ON juror.audit_f_report USING btree (ltrim(rtrim((faudit)::text)), owner);
CREATE INDEX adt_f_rpt_faudit_r ON juror.audit_f_report USING btree (rtrim((faudit)::text), loc_code, owner);


-- juror.audit_report definition

-- Drop table

-- DROP TABLE juror.audit_report;

CREATE TABLE juror.audit_report
(
    "owner"              varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    audits               varchar(11)  NOT NULL,
    line_no              numeric(38)  NOT NULL,
    part_no              varchar(9)   NOT NULL,
    timein               timestamp(0) NULL,
    timeout              timestamp(0) NULL,
    pool_trial           varchar(16)  NULL,
    reason               varchar(1)   NULL,
    pay_county           varchar(1)   NULL,
    mileage              numeric(38)  NULL,
    mileage_rate         float8       NULL,
    total_mileage        float8       NULL,
    public_parking       float8       NULL,
    public_trans         float8       NULL,
    child_care           float8       NULL,
    misc_description     varchar(20)  NULL,
    misc_amount          float8       NULL,
    total_amount         float8       NULL,
    amount               float8       NULL,
    court_emp            varchar(1)   NULL,
    att_date             timestamp(0) NULL,
    lname                varchar(20)  NULL,
    is_active            varchar(1)   NULL,
    loc_code             varchar(3)   NULL,
    fname                varchar(20)  NULL,
    app_stage            numeric(38)  NULL,
    donation_code        varchar(4)   NULL,
    public_parking_total float8       NULL,
    rail_bus_total       float8       NULL,
    hired_vehicle_total  float8       NULL,
    rate_mcycles         float8       NULL,
    mcycles_pass         int2         NULL,
    mcycles_total        float8       NULL,
    mcars_pass           int2         NULL,
    rate_mcars           float8       NULL,
    mcars_total          float8       NULL,
    rate_pcycles         float8       NULL,
    pcycles_total        float8       NULL,
    los_lfour_total      float8       NULL,
    los_mfour_total      float8       NULL,
    loss_mten_total      float8       NULL,
    subs_lfive_total     float8       NULL,
    subs_mfive_total     float8       NULL,
    loss_oten_total      float8       NULL,
    loss_oten_h_total    float8       NULL,
    loss_overnight_total float8       NULL,
    amt_spent            float8       NULL,
    travel_time          float4       NULL,
    daily_time           float4       NULL,
    total_time           float4       NULL,
    non_attendance       varchar(1)   NULL,
    date_aramis_created  timestamp(0) NULL,
    aramis_created       varchar(1)   NULL,
    exp_subs_date        timestamp(0) NULL,
    mod_date             timestamp(0) NULL,
    difference           float8       NULL,
    new_total            float8       NULL,
    pay_cash             varchar(1)   NULL,
    user_id              varchar(20)  NULL,
    CONSTRAINT audit_report_pkey PRIMARY KEY (audits, owner, line_no, part_no),
    CONSTRAINT adt_rpt_loc_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code)
);
CREATE INDEX adt_rpt_date_aramis ON juror.audit_report USING btree (date_aramis_created, owner);
CREATE INDEX agtest ON juror.audit_report USING btree (loc_code, date_aramis_created);


-- juror.coroner_pool_detail definition

-- Drop table

-- DROP TABLE juror.coroner_pool_detail;

CREATE TABLE juror.coroner_pool_detail
(
    cor_pool_no varchar(9)  NOT NULL,
    part_no     varchar(9)  NOT NULL,
    title       varchar(10) NULL,
    fname       varchar(20) NOT NULL,
    lname       varchar(20) NOT NULL,
    address1    varchar(35) NOT NULL,
    address2    varchar(35) NULL,
    address3    varchar(35) NULL,
    address4    varchar(35) NULL,
    address5    varchar(35) NULL,
    address6    varchar(35) NULL,
    postcode    varchar(10) NULL,
    CONSTRAINT coroner_pool_detail_pkey PRIMARY KEY (cor_pool_no, part_no),
    CONSTRAINT cor_pool_fk FOREIGN KEY (cor_pool_no) REFERENCES juror.coroner_pool (cor_pool_no)
);


-- juror.def_lett definition

-- Drop table

-- DROP TABLE juror.def_lett;

CREATE TABLE juror.def_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    date_def     timestamp(0) NOT NULL,
    exc_code     varchar(1)   NOT NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT def_lett_pkey PRIMARY KEY (part_no, owner),
    CONSTRAINT def_lett_exc_cd_fk FOREIGN KEY (exc_code) REFERENCES juror.exc_code (exc_code)
);
CREATE INDEX def_lett_own_date ON juror.def_lett USING btree (owner, date_printed);


-- juror.disposal_condition definition

-- Drop table

-- DROP TABLE juror.disposal_condition;

CREATE TABLE juror.disposal_condition
(
    condition_id int8 NOT NULL,
    disposal_id  int8 NOT NULL,
    CONSTRAINT disposal_condition_pkey PRIMARY KEY (disposal_id, condition_id),
    CONSTRAINT confk FOREIGN KEY (condition_id) REFERENCES juror."condition" (condition_id)
);


-- juror.disq_lett definition

-- Drop table

-- DROP TABLE juror.disq_lett;

CREATE TABLE juror.disq_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    disq_code    varchar(1)   NOT NULL,
    date_disq    timestamp(0) NOT NULL,
    date_printed timestamp(0) NULL,
    printed      varchar(1)   NULL,
    CONSTRAINT disq_lett_pkey PRIMARY KEY (part_no, owner),
    CONSTRAINT disq_lett_fk FOREIGN KEY (disq_code) REFERENCES juror.dis_code (disq_code)
);
CREATE INDEX disq_lett_own_date ON juror.disq_lett USING btree (owner, date_printed);


-- juror.exc_denied_lett definition

-- Drop table

-- DROP TABLE juror.exc_denied_lett;

CREATE TABLE juror.exc_denied_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    date_excused timestamp(0) NOT NULL,
    exc_code     varchar(1)   NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT exc_denied_lett_pkey PRIMARY KEY (part_no, owner),
    CONSTRAINT exc_denied_lett_fk FOREIGN KEY (exc_code) REFERENCES juror.exc_code (exc_code)
);
CREATE INDEX exc_den_lett_own_dt ON juror.exc_denied_lett USING btree (owner, date_printed);


-- juror.exc_lett definition

-- Drop table

-- DROP TABLE juror.exc_lett;

CREATE TABLE juror.exc_lett
(
    "owner"      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no      varchar(9)   NOT NULL,
    date_excused timestamp(0) NOT NULL,
    exc_code     varchar(1)   NULL,
    printed      varchar(1)   NULL,
    date_printed timestamp(0) NULL,
    CONSTRAINT exc_lett_pkey PRIMARY KEY (part_no, owner),
    CONSTRAINT exc_lett_fk FOREIGN KEY (exc_code) REFERENCES juror.exc_code (exc_code)
);
CREATE INDEX exc_lett_own_dt_idx ON juror.exc_lett USING btree (owner, date_printed);


-- juror.judge definition

-- Drop table

-- DROP TABLE juror.judge;

CREATE TABLE juror.judge
(
    "owner"      varchar(3)  NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    judge        varchar(4)  NOT NULL,
    description  varchar(30) NOT NULL,
    seating_plan numeric(38) NULL,
    tel_no       varchar(16) NULL,
    room         bpchar(6)   NULL,
    CONSTRAINT judge_pkey PRIMARY KEY (judge, owner),
    CONSTRAINT judge_seat_plan_fk FOREIGN KEY (seating_plan, "owner") REFERENCES juror.seating_plan (seating_plan, "owner")
);


-- juror.manuals definition

-- Drop table

-- DROP TABLE juror.manuals;

CREATE TABLE juror.manuals
(
    "owner"            varchar(3)    NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no            bpchar(9)     NOT NULL,
    pool_no            bpchar(9)     NOT NULL,
    lname              varchar(20)   NOT NULL,
    fname              varchar(20)   NOT NULL,
    title              varchar(10)   NULL,
    dob                timestamp(0)  NULL,
    address            varchar(35)   NOT NULL,
    address2           varchar(35)   NULL,
    address3           varchar(35)   NULL,
    address4           varchar(35)   NULL,
    address5           varchar(35)   NULL,
    address6           varchar(35)   NULL,
    zip                varchar(10)   NULL,
    h_phone            varchar(15)   NULL,
    w_phone            varchar(15)   NULL,
    m_phone            varchar(15)   NULL,
    w_ph_local         bpchar(4)     NULL,
    mileage            numeric(38)   NULL,
    pool_seq           bpchar(4)     NULL,
    pool_status        numeric(38)   NULL,
    is_active          bpchar(1)     NULL,
    pool_type          bpchar(3)     NULL,
    loc_code           bpchar(3)     NULL,
    ret_date           timestamp(0)  NOT NULL,
    responded          bpchar(1)     NOT NULL,
    reg_spc            bpchar(1)     NOT NULL,
    next_date          timestamp(0)  NULL,
    on_call            bpchar(1)     NULL,
    notes              varchar(2000) NULL,
    status             bpchar(1)     NULL,
    date_added         timestamp(0)  NOT NULL,
    added_by           varchar(20)   NULL,
    h_email            varchar(254)  NULL,
    contact_preference int2          NULL     DEFAULT 0,
    CONSTRAINT manuals_pkey PRIMARY KEY (part_no, owner),
    CONSTRAINT manuals_status_fk FOREIGN KEY (status) REFERENCES juror.manuals_status (code)
);


-- juror.part_hist definition

-- Drop table

-- DROP TABLE juror.part_hist;

CREATE TABLE juror.part_hist
(
    "owner"           varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no           varchar(9)   NOT NULL,
    date_part         timestamp(0) NOT NULL,
    history_code      varchar(4)   NOT NULL,
    user_id           varchar(20)  NOT NULL,
    other_information varchar(27)  NULL,
    pool_no           varchar(9)   NULL,
    last_update       timestamp(0) NULL,
    CONSTRAINT part_hist_hist_code_fk FOREIGN KEY (history_code) REFERENCES juror.t_history_code (history_code)
);
CREATE INDEX part_hist_part_date ON juror.part_hist USING btree (part_no, date_part, owner);



-- juror.pool definition

-- Drop table

-- DROP TABLE juror.pool;

CREATE TABLE juror.pool
(
    "owner"                   varchar(3)    NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    part_no                   varchar(9)    NOT NULL,
    pool_no                   varchar(9)    NOT NULL,
    poll_number               varchar(5)    NULL,
    title                     varchar(10)   NULL,
    lname                     varchar(20)   NOT NULL,
    fname                     varchar(20)   NOT NULL,
    dob                       timestamp(0)  NULL,
    address                   varchar(35)   NOT NULL,
    address2                  varchar(35)   NULL,
    address3                  varchar(35)   NULL,
    address4                  varchar(35)   NULL,
    address5                  varchar(35)   NULL,
    address6                  varchar(35)   NULL,
    zip                       varchar(10)   NULL,
    h_phone                   varchar(15)   NULL,
    w_phone                   varchar(15)   NULL,
    w_ph_local                varchar(4)    NULL,
    times_sel                 numeric(38)   NULL,
    trial_no                  varchar(16)   NULL,
    juror_no                  numeric(38)   NULL,
    reg_spc                   varchar(1)    NOT NULL,
    ret_date                  timestamp(0)  NOT NULL,
    def_date                  timestamp(0)  NULL,
    responded                 varchar(1)    NOT NULL,
    date_excus                timestamp(0)  NULL,
    exc_code                  varchar(1)    NULL,
    acc_exc                   varchar(1)    NULL,
    date_disq                 timestamp(0)  NULL,
    disq_code                 varchar(1)    NULL,
    mileage                   numeric(38)   NULL,
    "location"                varchar(6)    NULL,
    user_edtq                 varchar(20)   NULL,
    status                    numeric(38)   NULL,
    notes                     varchar(2000) NULL,
    no_attendances            numeric(38)   NULL,
    is_active                 varchar(1)    NULL,
    no_def_pos                numeric(38)   NULL,
    no_attended               numeric(38)   NULL,
    no_fta                    numeric(38)   NULL,
    no_awol                   numeric(38)   NULL,
    pool_seq                  varchar(4)    NULL,
    edit_tag                  varchar(1)    NULL,
    pool_type                 varchar(3)    NULL,
    loc_code                  varchar(3)    NULL,
    next_date                 timestamp(0)  NULL,
    on_call                   varchar(1)    NULL     DEFAULT 'N'::character varying,
    perm_disqual              varchar(1)    NULL,
    pay_county_emp            varchar(1)    NULL,
    pay_expenses              varchar(1)    NULL,
    spec_need                 varchar(1)    NULL,
    spec_need_msg             varchar(60)   NULL,
    smart_card                varchar(20)   NULL,
    amt_spent                 float8        NULL,
    completion_flag           varchar(1)    NULL     DEFAULT 'N'::character varying,
    completion_date           timestamp(0)  NULL,
    sort_code                 varchar(6)    NULL,
    bank_acct_name            varchar(18)   NULL,
    bank_acct_no              varchar(8)    NULL,
    bldg_soc_roll_no          varchar(18)   NULL,
    was_deferred              varchar(1)    NULL,
    id_checked                varchar(1)    NULL,
    postpone                  varchar(1)    NULL,
    welsh                     varchar(1)    NULL,
    paid_cash                 varchar(1)    NULL,
    travel_time               float4        NULL,
    scan_code                 varchar(9)    NULL,
    financial_loss            float8        NULL,
    police_check              varchar(1)    NULL,
    last_update               timestamp(0)  NULL,
    read_only                 varchar(1)    NULL     DEFAULT 'N'::character varying,
    summons_file              varchar(20)   NULL,
    reminder_sent             varchar(1)    NULL,
    phoenix_date              timestamp(0)  NULL,
    phoenix_checked           varchar(1)    NULL,
    m_phone                   varchar(15)   NULL,
    h_email                   varchar(254)  NULL,
    contact_preference        int2          NULL     DEFAULT 0,
    notifications             int2          NULL     DEFAULT 0,
    transfer_date             timestamp(0)  NULL,
    service_comp_comms_status varchar(10)   NULL,
    CONSTRAINT pool_pkey PRIMARY KEY (part_no, pool_no, owner),
    CONSTRAINT pool_disq_code_fk FOREIGN KEY (disq_code) REFERENCES juror.dis_code (disq_code),
    CONSTRAINT pool_exc_code_fk FOREIGN KEY (exc_code) REFERENCES juror.exc_code (exc_code)
);
CREATE INDEX i_pool_no ON juror.pool USING btree (pool_no);
CREATE INDEX i_zip ON juror.pool USING btree (zip);
CREATE INDEX lname ON juror.pool USING btree (lname);
CREATE INDEX next_date ON juror.pool USING btree (next_date);
CREATE INDEX pool_comp_date ON juror.pool USING btree (completion_date);
CREATE INDEX pool_lcode_own ON juror.pool USING btree (loc_code, owner);
CREATE INDEX pool_trial_owner ON juror.pool USING btree (trial_no, owner);
CREATE INDEX pool_upper_lname ON juror.pool USING btree (upper((lname)::text), owner);
CREATE INDEX pooltest ON juror.pool USING btree (part_no, is_active, loc_code, owner);


-- juror.pool_hist definition

-- Drop table

-- DROP TABLE juror.pool_hist;

CREATE TABLE juror.pool_hist
(
    "owner"           varchar(3)   NULL DEFAULT current_setting('juror_app.owner'::text, true),
    pool_no           varchar(9)   NOT NULL,
    date_part         timestamp(0) NOT NULL,
    history_code      varchar(4)   NOT NULL,
    user_id           varchar(20)  NOT NULL,
    other_information varchar(24)  NULL,
    last_update       timestamp(0) NULL,
    CONSTRAINT pool_hist_hist_code_fk FOREIGN KEY (history_code) REFERENCES juror.t_history_code (history_code)
);
CREATE INDEX pool_hist_pool_no_idx ON juror.pool_hist USING btree (pool_no);


-- juror.trial definition

-- Drop table

-- DROP TABLE juror.trial;

CREATE TABLE juror.trial
(
    "owner"                      varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    trial_no                     varchar(16)  NOT NULL,
    descript                     varchar(30)  NOT NULL,
    room_no                      varchar(6)   NOT NULL,
    judge                        varchar(4)   NOT NULL,
    t_type                       varchar(3)   NOT NULL,
    no_jury                      float8       NULL,
    no_alt                       float8       NULL,
    voir_end_date                timestamp(0) NULL,
    trial_end_date               timestamp(0) NULL,
    deliberation_date            timestamp(0) NULL,
    deliberation_time            timestamp(0) NULL,
    sequestered_date             timestamp(0) NULL,
    judgement_amount             float8       NULL,
    final_charge_code            varchar(6)   NULL,
    total_def_premptory          float8       NULL,
    total_pla_premptory          float8       NULL,
    trial_dte                    timestamp(0) NULL,
    voir_dte                     timestamp(0) NULL,
    date_sele                    timestamp(0) NULL,
    cancel_reason                varchar(4)   NULL,
    jurisdiction_code            varchar(1)   NULL,
    prosecutor_plantiff_attorney varchar(20)  NULL,
    defence_attorney             varchar(20)  NULL,
    jury_trial                   varchar(1)   NULL,
    jurors_requested             float8       NULL,
    jurors_sent                  float8       NULL,
    sent_date                    timestamp(0) NULL,
    sent_time                    timestamp(0) NULL,
    event_disposition            varchar(3)   NULL,
    total_pros_plan_cause        float8       NULL,
    total_def_cause              float8       NULL,
    select_date_end              timestamp(0) NULL,
    seating_plan                 numeric(38)  NULL,
    pay_upon_complete            varchar(1)   NULL,
    anonymous                    varchar(1)   NULL,
    CONSTRAINT trial_pkey PRIMARY KEY (trial_no, owner),
    CONSTRAINT trial_cancel_reas_fk FOREIGN KEY (cancel_reason) REFERENCES juror.trial_can_reas (cancel_reason),
    CONSTRAINT trial_evnt_dispo_fk FOREIGN KEY (event_disposition) REFERENCES juror.disposition (disposition_code),
    CONSTRAINT trial_final_chg_cd_fk FOREIGN KEY (final_charge_code) REFERENCES juror.trial_charge_code (final_charge_code),
    CONSTRAINT trial_judge_fk FOREIGN KEY (judge, "owner") REFERENCES juror.judge (judge, "owner"),
    CONSTRAINT trial_room_no_fk FOREIGN KEY (room_no, "owner") REFERENCES juror."location" ("location", "owner"),
    CONSTRAINT trial_t_type_fk FOREIGN KEY (t_type) REFERENCES juror.t_type (t_type)
)
    WITH (
        fillfactor = 70
        );


-- juror.unique_pool definition

-- Drop table

-- DROP TABLE juror.unique_pool;

CREATE TABLE juror.unique_pool
(
    "owner"            varchar(3)   NOT NULL DEFAULT current_setting('juror_app.owner'::text, true),
    pool_no            varchar(9)   NOT NULL,
    jurisdiction_code  varchar(1)   NULL,
    return_date        timestamp(0) NOT NULL,
    next_date          timestamp(0) NOT NULL,
    no_requested       numeric(38)  NULL     DEFAULT 0,
    pool_total         numeric(38)  NOT NULL DEFAULT 0,
    reg_spc            varchar(1)   NOT NULL,
    pool_type          varchar(3)   NULL,
    loc_code           varchar(3)   NULL,
    new_request        varchar(1)   NULL     DEFAULT 'Y'::character varying,
    last_update        timestamp(0) NULL,
    read_only          varchar(1)   NULL     DEFAULT 'N'::character varying,
    deferrals_used     int8         NULL,
    additional_summons int8         NULL,
    attend_time        timestamp(0) NULL,
    CONSTRAINT unq_pool_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code),
    CONSTRAINT unq_pool_pool_type_fk FOREIGN KEY (pool_type) REFERENCES juror.pool_type (pool_type)
);
CREATE INDEX unique_pool_rtndate_loccd ON juror.unique_pool USING btree (return_date, loc_code);
CREATE UNIQUE INDEX unq_pool_pk ON juror.unique_pool USING btree (pool_no, owner);


-- juror.welsh_location definition

-- Drop table

-- DROP TABLE juror.welsh_location;

CREATE TABLE juror.welsh_location
(
    "owner"          varchar(3)   NULL DEFAULT current_setting('juror_app.owner'::text, true),
    loc_code         varchar(3)   NOT NULL,
    loc_name         varchar(40)  NOT NULL,
    loc_address1     varchar(35)  NOT NULL,
    loc_address2     varchar(35)  NULL,
    loc_address3     varchar(35)  NULL,
    loc_address4     varchar(35)  NULL,
    loc_address5     varchar(35)  NULL,
    loc_address6     varchar(35)  NULL,
    last_update      timestamp(0) NULL,
    location_address varchar(230) NULL,
    CONSTRAINT welsh_location_pkey PRIMARY KEY (loc_code),
    CONSTRAINT welsh_location_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror.court_location (loc_code)
);
CREATE INDEX welsh_location_last_update ON juror.welsh_location USING btree (last_update);


-- juror.pool_comments definition

-- Drop table

-- DROP TABLE juror.pool_comments;

CREATE TABLE juror.pool_comments
(
    "owner"      varchar(3)   NULL DEFAULT current_setting('juror_app.owner'::text, true),
    pool_no      varchar(9)   NOT NULL,
    user_id      varchar(20)  NOT NULL,
    last_update  timestamp(0) NULL,
    pcomment     varchar(80)  NOT NULL,
    no_requested numeric(38)  NULL DEFAULT 0,
    CONSTRAINT pool_comments_pool_no FOREIGN KEY (pool_no, "owner") REFERENCES juror.unique_pool (pool_no, "owner") ON DELETE CASCADE
);
CREATE INDEX pool_comments_pool_idx ON juror.pool_comments USING btree (pool_no);


-- juror.last_visit source

CREATE OR REPLACE VIEW juror.last_visit
AS
SELECT max(appearances.att_date) AS att_date,
       appearances.part_no
FROM juror.appearances
WHERE appearances.owner::text = current_setting('JUROR_APP.OWNER'::text, true)
GROUP BY appearances.part_no;

-- DROP FUNCTION juror.aramis_invoice_number_nextval_atx(text);

CREATE OR REPLACE FUNCTION juror.aramis_invoice_number_nextval_atx(p_owner text DEFAULT current_setting('JUROR_APP.OWNER'::text, true))
    RETURNS bigint
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE
    id bigint;

BEGIN
    EXECUTE 'select ARAMIS_INVOICE_NUMBER_' || p_owner || '.nextval from dual ' into STRICT id;
    return id;
END;
$function$
;


-- DROP FUNCTION juror.aramis_unique_id_nextval_atx(text);

CREATE OR REPLACE FUNCTION juror.aramis_unique_id_nextval_atx(p_owner text DEFAULT current_setting('JUROR_APP.OWNER'::text, true))
    RETURNS bigint
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE
    id bigint;

BEGIN
    EXECUTE 'select aramis_unique_id_' || p_owner || '.nextval from dual ' into STRICT id;
    return id;
END;
$function$
;


-- DROP FUNCTION juror.attend_audit_number_nextval_atx(text);

CREATE OR REPLACE FUNCTION juror.attend_audit_number_nextval_atx(p_owner text DEFAULT current_setting('JUROR_APP.OWNER'::text, true))
    RETURNS bigint
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE
    id bigint;

BEGIN
    EXECUTE 'select attend_audit_number_' || p_owner || '.nextval from dual ' into STRICT id;
    return id;
END;
$function$
;

-- DROP FUNCTION juror.audit_number_nextval(text);



-- DROP FUNCTION juror.audit_number_nextval_atx(text);

CREATE OR REPLACE FUNCTION juror.audit_number_nextval_atx(p_owner text DEFAULT current_setting('JUROR_APP.OWNER'::text, true))
    RETURNS bigint
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE
    id bigint;

BEGIN
    EXECUTE 'select audit_number_' || p_owner || '.nextval from dual ' into STRICT id;
    return id;
END;
$function$
;

-- DROP PROCEDURE juror.auto_generate_confirmation_letter();

CREATE OR REPLACE PROCEDURE juror.auto_generate_confirmation_letter()
    LANGUAGE plpgsql
AS
$procedure$
BEGIN

    PERFORM set_config('auto_generate_lc_job_type', 'AUTO CONFIRMATION LETTER GENERATION', false);
    PERFORM set_config('auto_generate_lc_englishformtype', '5224A', false);
    PERFORM set_config('auto_generate_lc_welshformtype', '5224AC', false);
    PERFORM set_config('auto_generate_lc_otherinformation', 'Confirmation Letter Auto', false);
    PERFORM set_config('auto_generate_lc_historycode', 'RRES', false);


    -- RFS 3681 following Insert statement now takes the attendance time from UNIQUE_POOL.ATTEND_TIME if present,
-- otherwise continues to use the default from COURT_LOCATION.LOC_ATTEND_TIME
-- UNIQUE_POOL.ATTEND_TIME is converted to a string using a 12 hour clock with the addition of the Meridian suffix AM or PM
--pool.police_check would be null of procdure PHOENIX has not run
--pool.police_check would be 'E' if a police check has been requested but has not yet been completed (PHOENIX has run but PHOENIXINTERFACE has not run
--Treat phoenix_date of Saturday as Monday (phoenix_date+2) and phoenix_date of Sunday as Monday (phoenix_date+1)
    INSERT into juror.temp_auto_generate_lett(part_no, pool_no, loc_code, row_id, lang, details)
    SELECT p.part_no,
           p.pool_no,
           u.loc_code,
           c.ROWID,
           CASE WHEN upper(p.welsh) = 'Y' THEN 'W' WHEN upper(p.welsh) IS NULL THEN 'E' ELSE 'E' END,
           current_setting('auto_generate_lc_date_part_text')::varchar(30)
               || cc.loc_code
               || CASE
                      WHEN CASE WHEN upper(p.welsh) = 'Y' THEN 'W' WHEN upper(p.welsh) IS NULL THEN 'E' ELSE 'E' END =
                           'W' THEN RPAD(upper(cc.loc_name), 40)
                      WHEN CASE WHEN upper(p.welsh) = 'Y' THEN 'W' WHEN upper(p.welsh) IS NULL THEN 'E' ELSE 'E' END =
                           'E' THEN CASE
                                        WHEN cc.loc_code = '626' THEN RPAD(upper(cc.loc_name), 59)
                                        ELSE RPAD('The Crown Court at ' || upper(cc.loc_name), 59) END END
               || upper(current_setting('auto_generate_lc_bureau_part_text')::varchar(300))
               || RPAD(Rtrim(to_char(p.next_date, 'DAY')) || ' ' ||
                       to_char(p.next_date, 'DD') || ' ' ||
                       Rtrim(to_char(p.next_date, 'MONTH')) || ', ' ||
                       to_char(p.next_date, 'YYYY'), 32, ' ')
               || Rpad(CASE
                           WHEN u.attend_time IS NULL THEN coalesce(CASE
                                                                        WHEN sign(position('AM' in cc.loc_attend_time)) = 0
                                                                            THEN cc.loc_attend_time || ' AM'
                                                                        ELSE cc.loc_attend_time END, '9.00AM')
                           ELSE to_char(u.attend_time, 'hh:miAM') END,
                       8, ' ')
               || RPAD(coalesce(p.title, ' '), 10, ' ') ||
           RPAD(coalesce(p.fname, ' '), 20, ' ') ||
           RPAD(coalesce(p.lname, ' '), 20, ' ') ||
           RPAD(RPAD(coalesce(p.address, ' '), 35) ||
                RPAD(p.address2, 35) ||
                RPAD(p.address3, 35) ||
                RPAD(p.address4, 35) ||
                RPAD(p.address5, 35) ||
                RPAD(p.address6, 35) ||
                RPAD(p.zip, 10), 220) ||
           RPAD(coalesce(p.part_no, ' '), 9, ' ')
               || upper(current_setting('auto_generate_lc_bureau_signature')::varchar(30))
    FROM POOL p,
         UNIQUE_POOL u,
         CONFIRM_LETT c,
         COURT_LOCATION cc
    WHERE p.POOL_NO = u.POOL_NO
      AND p.OWNER = '400'
      AND u.OWNER = '400'
      AND c.OWNER = '400'
      AND u.LOC_CODE = cc.LOC_CODE
      AND p.IS_ACTIVE = 'Y'
      AND p.RESPONDED = 'Y'
      AND p.STATUS = 2
      AND p.NEXT_DATE >= p.PHOENIX_DATE
      AND CASE
              WHEN to_char(p.PHOENIX_DATE, 'D') = 6 THEN date_trunc('day', p.PHOENIX_DATE) + '2 days'::interval
              WHEN to_char(p.PHOENIX_DATE, 'D') = 7 THEN date_trunc('day', p.phoenix_date) + '1 days'::interval
              ELSE date_trunc('day', p.phoenix_date) END <= date_trunc('day', clock_timestamp() - CASE
                                                                                                      WHEN coalesce(p.police_check, 'E') = 'E'
                                                                                                          THEN current_setting('auto_generate_ld_phoenix_delay')::integer
                                                                                                      ELSE 0 END)
      AND p.PHOENIX_DATE is NOT NULL
      AND p.PART_NO = c.part_no
      AND (c.PRINTED <> 'Y' or c.PRINTED is null);

    CALL auto_generate_populate_abaccus();
    CALL auto_generate_populate_part_hist();
    CALL auto_generate_populate_print_files();

    UPDATE CONFIRM_LETT c
    SET c.PRINTED      = 'Y',
        c.DATE_PRINTED = current_setting('auto_generate_ld_begin_time')::timestamp(0)
    FROM juror.temp_auto_generate_lett t
    WHERE c.ROWID = t.ROW_ID
      AND c.OWNER = '400';

    commit;

    EXECUTE 'truncate table temp_auto_generate_lett';
    -- This bit is added so that print files are not created twice
    -- if the package is run twice in same session
EXCEPTION
    when others then
        CALL auto_generate_write_error(sqlerrm);
        rollback;
        raise;
END;

$procedure$
;

-- DROP PROCEDURE juror.auto_generate_get_print_file_name();

CREATE OR REPLACE PROCEDURE juror.auto_generate_get_print_file_name()
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
BEGIN
    --  lc_Job_Type	  := current_setting('');
    --  lc_EnglishFormType := current_setting('');
    --  lc_WelshFormType	  := current_setting('');
    --  lc_OtherInformation:= current_setting('');
    --  lc_HistoryCode	  := current_setting('');

    SELECT sum(number_of_items)
    FROM abaccus
    WHERE form_type = current_setting('auto_generate_lc_englishformtype', false)::form_attr.form_type % type
      AND date_trunc('day', creation_date) =
          date_trunc('day', current_setting('auto_generate_ld_begin_time', false)::timestamp(0));

    SELECT sum(number_of_items)
    FROM abaccus
    WHERE form_type = current_setting('auto_generate_lc_welshformtype', false)::form_attr.form_type % type
      AND date_trunc('day', creation_date) =
          date_trunc('day', current_setting('auto_generate_ld_begin_time', false)::timestamp(0));


-- Get nextval from sequence data_file_no only if records exist for English letters
    IF current_setting('auto_generate_ln_english_processed_count', false)::integer > 0 THEN
        PERFORM 'JURY' || RPAD(nextval('data_file_no'), 4, '0') ||
                current_setting('auto_generate_lc_fileextension', false)::varchar(30);
    END IF;


-- Get nextval from sequence data_file_no only if records exist for Welsh letters
    IF current_setting('auto_generate_ln_welsh_processed_count', false)::integer > 0 THEN
        PERFORM 'JURY' || RPAD(nextval('data_file_no'), 4, '0') ||
                current_setting('auto_generate_lc_fileextension', false)::varchar(30);
    END IF;


END;

$procedure$
;

-- DROP PROCEDURE juror.auto_generate_populate_part_hist();

CREATE OR REPLACE PROCEDURE juror.auto_generate_populate_part_hist()
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
BEGIN

    INSERT INTO part_hist(owner, part_no, date_part, history_code, user_id, other_information, pool_no)
    SELECT '400',
           part_no,
           clock_timestamp(),
           current_setting('auto_generate_lc_historycode')::part_hist.history_code % type,
           'SYSTEM',
           current_setting('auto_generate_lc_otherinformation')::part_hist.other_information % type,
           pool_no
    FROM temp_auto_generate_lett;


END;

$procedure$
;

-- DROP PROCEDURE juror.auto_generate_populate_print_files();

CREATE OR REPLACE PROCEDURE juror.auto_generate_populate_print_files()
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
BEGIN

    -- Get print file names
    CALL auto_generate_get_print_file_name();

    -- Now populate print_files table
    INSERT into print_files(printfile_name, creation_date, form_type, detail_rec)
    SELECT CASE
               WHEN lang = 'E' THEN current_setting('auto_generate_lc_englishfile')::print_files.printfile_name % type
               ELSE current_setting('auto_generate_lc_welshfile')::print_files.printfile_name % type END,
           current_setting('auto_generate_ld_begin_time')::timestamp(0),
           CASE
               WHEN lang = 'E' THEN current_setting('auto_generate_lc_englishformtype')::form_attr.form_type % type
               ELSE current_setting('auto_generate_lc_welshformtype')::form_attr.form_type % type END,
           details
    FROM temp_auto_generate_lett;


END;

$procedure$
;

-- DROP PROCEDURE juror.auto_generate_withdrawal_letter();

CREATE OR REPLACE PROCEDURE juror.auto_generate_withdrawal_letter()
    LANGUAGE plpgsql
AS
$procedure$
BEGIN

    PERFORM set_config('auto_generate_lc_job_type', 'AUTO WITHDRAWAL LETTER GENERATION', false);
    PERFORM set_config('auto_generate_lc_englishformtype', '5224', false);
    PERFORM set_config('auto_generate_lc_welshformtype', '5224C', false);
    PERFORM set_config('auto_generate_lc_otherinformation', 'Withdrawal Letter Auto', false);
    PERFORM set_config('auto_generate_lc_historycode', 'RDIS', false);

    INSERT into temp_auto_generate_lett(part_no, pool_no, loc_code, row_id, lang, details)
    SELECT p.part_no,
           p.pool_no,
           p.loc_code,
           d.ROWID,
           CASE WHEN upper(p.welsh) = 'Y' THEN 'W' WHEN upper(p.welsh) IS NULL THEN 'E' ELSE 'E' END,
           current_setting('auto_generate_lc_date_part_text')::varchar(30)
               || CASE
                      WHEN CASE WHEN upper(p.welsh) = 'Y' THEN 'W' WHEN upper(p.welsh) IS NULL THEN 'E' ELSE 'E' END =
                           'W' THEN RPAD(upper(cc.loc_name), 40)
                      WHEN CASE WHEN upper(p.welsh) = 'Y' THEN 'W' WHEN upper(p.welsh) IS NULL THEN 'E' ELSE 'E' END =
                           'E' THEN CASE
                                        WHEN cc.loc_code = '626' THEN RPAD(upper(cc.loc_name), 59)
                                        ELSE RPAD('The Crown Court at ' || upper(cc.loc_name), 59) END END
               || upper(current_setting('auto_generate_lc_bureau_part_text')::varchar(300))
               || RPAD(coalesce(p.title, ' '), 10, ' ') ||
           RPAD(coalesce(p.fname, ' '), 20, ' ') ||
           RPAD(coalesce(p.lname, ' '), 20, ' ') ||
           RPAD(RPAD(coalesce(p.address, ' '), 35) ||
                RPAD(p.address2, 35) ||
                RPAD(p.address3, 35) ||
                RPAD(p.address4, 35) ||
                RPAD(p.address5, 35) ||
                RPAD(p.address6, 35) ||
                RPAD(p.zip, 10), 220) ||
           RPAD(coalesce(p.part_no, ' '), 9, ' ')
               || upper(current_setting('auto_generate_lc_bureau_signature')::varchar(30))
    FROM POOL p,
         DISQ_LETT d,
         COURT_LOCATION cc
    WHERE p.owner = '400'
      AND d.owner = '400'
      AND d.disq_code = 'E'
      AND p.loc_code = cc.loc_code
      AND p.is_active = 'Y'
      AND p.status = 6
      AND p.part_no = d.part_no
      AND (d.printed <> 'Y' or d.printed is null);

    CALL auto_generate_populate_abaccus();
    CALL auto_generate_populate_part_hist();
    CALL auto_generate_populate_print_files();

    UPDATE DISQ_LETT d
    SET d.PRINTED      = 'Y',
        d.DATE_PRINTED = current_setting('auto_generate_ld_begin_time')::timestamp(0)
    FROM temp_auto_generate_lett t
    WHERE d.ROWID = t.ROW_ID
      AND d.OWNER = '400';

    delete
    from phoenix_temp
    where part_no in (SELECT part_no
                      from temp_auto_generate_lett);

    commit;

    EXECUTE 'truncate table temp_auto_generate_lett';
    -- This bit is added so that print files are not created twice
    -- if the package is run twice in same session
EXCEPTION
    when others then
        CALL auto_generate_write_error(sqlerrm);
        rollback;
        raise;

END;

$procedure$
;

-- DROP PROCEDURE juror.auto_generate_write_error_atx(text);

CREATE OR REPLACE PROCEDURE juror.auto_generate_write_error_atx(IN p_info text)
    LANGUAGE plpgsql
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info)
    values (current_setting('auto_generate_lc_job_type')::error_log.JOB % type, p_info);
    commit;
END;

$procedure$
;

-- DROP FUNCTION juror.bureau_only(text, text);

CREATE OR REPLACE FUNCTION juror.bureau_only(p_schema text, p_object text)
    RETURNS character varying
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
BEGIN

    return ' (case  when nvl(sys_context(''JUROR_APP'',''OWNER''),''400'')=''400'' then 1 else 2 end ) = 1';

end;
$function$
;

-- DROP PROCEDURE juror.copy_history_main(text, text, text);

CREATE OR REPLACE PROCEDURE juror.copy_history_main(IN l_from_court text, IN l_to_court text, IN l_to_pool_no text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    lc_Job_Type text := 'copy_history_main()';
BEGIN


    /** Copy part_hist avoiding duplicating history already at receiving court */

    INSERT INTO part_hist(Owner, part_no, date_part, history_code, user_id, other_information, pool_no)

    SELECT c_to.context_id, h.part_no, date_part, history_code, user_id, other_information, h.pool_no
    FROM part_hist h,
         context_data c_from,
         context_data c_to,
         pool p
    WHERE h.owner = c_from.context_id
      and c_from.loc_code = l_from_court
      and c_to.loc_code = l_to_court
      AND h.part_no = p.part_no
      and p.owner = c_to.context_id
      and p.pool_no = l_to_pool_no
      and is_active = 'Y'

    EXCEPT

    SELECT context_id, h.part_no, date_part, history_code, user_id, other_information, h.pool_no
    FROM part_hist h,
         context_data c,
         pool p
    WHERE h.owner = c.context_id
      and c.loc_code = l_to_court
      AND h.part_no = p.part_no
      and p.owner = c.context_id
      and p.pool_no = l_to_pool_no
      and is_active = 'Y';

    /** Copy phone_log avoiding duplicating entries already at receiving court */

    INSERT INTO phone_log(owner, part_no, start_call, user_id, end_call, phone_code, notes)

    SELECT c_to.context_id, h.part_no, start_call, user_id, end_call, phone_code, h.notes
    FROM phone_log h,
         context_data c_from,
         context_data c_to,
         pool p
    WHERE h.owner = c_from.context_id
      and c_from.loc_code = l_from_court
      and c_to.loc_code = l_to_court
      AND h.part_no = p.part_no
      and p.owner = c_to.context_id
      and p.pool_no = l_to_pool_no
      and is_active = 'Y'

    EXCEPT

    SELECT context_id, h.part_no, start_call, user_id, end_call, phone_code, h.notes
    FROM phone_log h,
         context_data c,
         pool p
    WHERE h.owner = c.context_id
      and c.loc_code = l_to_court
      AND h.part_no = p.part_no
      and p.owner = c.context_id
      and p.pool_no = l_to_pool_no
      and is_active = 'Y';

    /** Copy part_amendments avoiding duplicating entries already at receiving court */

    INSERT INTO part_amendments(owner, part_no, edit_date, edit_userid, title, fname, lname, dob, address, zip,
                                sort_code, bank_acct_name, bank_acct_no, bldg_soc_roll_no, pool_no)

    SELECT c_to.context_id,
           h.part_no,
           edit_date,
           edit_userid,
           h.title,
           h.fname,
           h.lname,
           h.dob,
           h.address,
           h.zip,
           h.sort_code,
           h.bank_acct_name,
           h.bank_acct_no,
           h.bldg_soc_roll_no,
           h.pool_no
    FROM part_amendments h,
         context_data c_from,
         context_data c_to,
         pool p
    WHERE h.owner = c_from.context_id
      and c_from.loc_code = l_from_court
      and c_to.loc_code = l_to_court
      AND h.part_no = p.part_no
      and p.owner = c_to.context_id
      and p.pool_no = l_to_pool_no
      and is_active = 'Y'

    EXCEPT

    SELECT context_id,
           h.part_no,
           edit_date,
           edit_userid,
           h.title,
           h.fname,
           h.lname,
           h.dob,
           h.address,
           h.zip,
           h.sort_code,
           h.bank_acct_name,
           h.bank_acct_no,
           h.bldg_soc_roll_no,
           h.pool_no
    FROM part_amendments h,
         context_data c,
         pool p
    WHERE h.owner = c.context_id
      and c.loc_code = l_to_court
      AND h.part_no = p.part_no
      and p.owner = c.context_id
      and p.pool_no = l_to_pool_no
      and is_active = 'Y';


    -- No COMMIT here as app will perform the commit
EXCEPTION
    when others then
        CALL juror.copy_history_write_error(sqlerrm);
        rollback;
        raise;
END;

$procedure$
;


-- DROP PROCEDURE juror.copy_history_write_error_atx(text);

CREATE OR REPLACE PROCEDURE juror.copy_history_write_error_atx(IN p_info text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    lc_Job_Type text := 'Copy_history.main';
BEGIN
    INSERT INTO juror.ERROR_LOG(job, error_info) values (lc_Job_Type, p_info);
    commit;
END;

$procedure$
;

-- DROP PROCEDURE juror.court_phoenix_finalise();

CREATE OR REPLACE PROCEDURE juror.court_phoenix_finalise()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    court_police_check CURSOR FOR
        SELECT t.owner cpnc_owner,
               t.id,
               t.last_name,
               t.first_name,
               t.postcode,
               t.dob,
               t.disqualified,
               t.check_complete,
               t.try_count,
               p.owner pool_owner,
               p.pool_no,
               p.read_only,
               p.loc_code,
               p.oid   row_idd
        from juror_court_police_check t,
             pool p
        where (t.try_count > 1 or t.check_complete = 'Y')
          and p.owner = t.owner
          and p.part_no = t.id
          and p.is_active = 'Y';
    lc_Job_Type text;

BEGIN
    lc_Job_Type := 'court_phoenix_FINALISE()';

    for each_participant in court_police_check
        loop

            update pool
            set phoenix_checked = CASE
                                      WHEN each_participant.try_count = 0 THEN 'C'
                                      WHEN each_participant.try_count = 1 THEN 'C'
                                      WHEN each_participant.try_count IS NULL THEN 'C'
                                      ELSE 'U' END,
                police_check    = CASE
                                      WHEN each_participant.disqualified = 'N' THEN 'P'
                                      WHEN each_participant.disqualified = 'Y' THEN 'F' END,
                status          = CASE
                                      WHEN each_participant.disqualified = 'N' THEN status
                                      WHEN each_participant.disqualified = 'Y' THEN '6' END,
                disq_code       = CASE WHEN each_participant.disqualified = 'Y' THEN 'E' ELSE NULL END,
                date_disq       = CASE WHEN each_participant.disqualified = 'Y' THEN clock_timestamp() ELSE NULL END
            where rowid = each_participant.row_idd;

            if each_participant.disqualified = 'N' then
                -- RFS 3681 Changed value for other_information
                insert into part_hist(owner,
                                      part_no,
                                      date_part,
                                      history_code,
                                      user_id,
                                      other_information,
                                      pool_no)
                values (each_participant.cpnc_owner,
                        each_participant.id,
                        clock_timestamp(),
                        'POLG',
                        'SYSTEM',
                        CASE
                            WHEN each_participant.try_count = '0' THEN 'Passed'
                            WHEN each_participant.try_count = '1' THEN 'Passed'
                            WHEN each_participant.try_count IS NULL THEN 'Unchecked - timed out'
                            ELSE 'Unchecked - timed out' END,
                        each_participant.pool_no);
            else
                insert into disq_lett(owner,
                                      part_no,
                                      disq_code,
                                      date_disq,
                                      date_printed,
                                      printed)
                values (each_participant.cpnc_owner,
                        each_participant.id,
                        'E',
                        clock_timestamp(),
                        null,
                        null);
                -- RFS 3681 Changed value for other_information
                insert into part_hist(owner,
                                      part_no,
                                      date_part,
                                      history_code,
                                      user_id,
                                      other_information,
                                      pool_no)
                values (each_participant.cpnc_owner,
                        each_participant.id,
                        clock_timestamp(),
                        'POLF',
                        'SYSTEM',
                        'Failed',
                        each_participant.pool_no);

                insert into part_hist(owner,
                                      part_no,
                                      date_part,
                                      history_code,
                                      user_id,
                                      other_information,
                                      pool_no)
                values (each_participant.cpnc_owner,
                        each_participant.id,
                        clock_timestamp() + (1 / 86400),
                        'PDIS',
                        'SYSTEM',
                        'Disqualify - E',
                        each_participant.pool_no);

                -- delete from defer.dbf table on owner and part_no
                delete
                from defer_dbf
                where defer_dbf.owner = each_participant.cpnc_owner
                  and defer_dbf.part_no = each_participant.id;

            end if;

        end loop;

    delete from juror_court_police_check where try_count > 1 or check_complete = 'Y';

    commit;
    --return(0);
exception
    when others then
        CALL court_phoenix_write_error(sqlerrm);
        raise;
    --return(1);
END;

$procedure$
;

-- DROP FUNCTION juror.court_phoenix_jurorcleanname(text);

CREATE OR REPLACE FUNCTION juror.court_phoenix_jurorcleanname(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    s_name varchar(20);
    n_pos1 bigint;
    n_pos2 bigint;


BEGIN
    -- Replace all full stops and commas with a space
    s_name := REPLACE(name_in, '.', ' ');
    s_name := REPLACE(s_name, ',', ' ');

    -- Remove anything within '(' or ')'
    n_pos1 := position('(' in s_name);
    n_pos2 := position(')' in s_name);

    IF (n_pos1 > 0 AND n_pos2 > 0) THEN
        s_name := Substr(s_name, 1, n_pos1 - 1) || ' ' || Substr(s_name, n_pos2 + 1);
    END IF;

    -- Remove anything within '[' or ']'
    n_pos1 := position('[' in s_name);
    n_pos2 := position(']' in s_name);

    IF (n_pos1 > 0 AND n_pos2 > 0) THEN
        s_name := Substr(s_name, 1, n_pos1 - 1) || ' ' || Substr(s_name, n_pos2 + 1);
    END IF;

    return trim(both s_name);

EXCEPTION
    WHEN OTHERS THEN
        CALL court_phoenix_write_error('jurorCleanName ');

END;

$function$
;

-- DROP FUNCTION juror.court_phoenix_jurorfirstname(text);

CREATE OR REPLACE FUNCTION juror.court_phoenix_jurorfirstname(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    n_pos  bigint;
    s_name varchar(20);


BEGIN

    -- Replace all full stops and commas with a space and
    -- Remove anything within brackets () or [] and
    -- Remove leading and trailing spaces...
    s_name := court_phoenix_jurorCleanName(name_in);

    -- If there is a 'split' in the name, keep Firstname
    n_pos := position(' ' in s_name);

    IF n_pos > 0 THEN
        s_name := Substr(s_name, 1, n_pos - 1);
    END IF;

    -- Check for NULL and return firstname in uppercase
    s_name := coalesce(s_name, '~');
    --Trac3897 If null supply tilde
    --Return UPPER(Trim(s_name));
    Return UPPER(s_name);

EXCEPTION
    WHEN OTHERS THEN
        CALL court_phoenix_write_error('Error in jurorFirstName ');

END;

$function$
;

-- DROP FUNCTION juror.court_phoenix_jurormiddlename(text);

CREATE OR REPLACE FUNCTION juror.court_phoenix_jurormiddlename(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    s_name varchar(20);
    n_pos  bigint;


BEGIN
    -- Replace all full stops and commas with a space and
    -- Remove anything within brackets () or [] and
    -- Remove leading and trailing spaces...
    --s_name := court_phoenix_jurorCleanName(name_in); --Moved below for Trac3897 - ensure use middle name
    -- If there is a 'split' in the name, remove Firstname, otherwise there
    -- is no middle name to return.
    --n_pos := INSTR(s_name, ' ' ); Trac3897
    n_pos := position(' ' in name_in);

    IF n_pos > 0 THEN
        s_name := Substr(name_in, n_pos + 1); --Trac3897
    ELSE
        s_name := NULL;
    END IF;

    s_name := court_phoenix_jurorCleanName(s_name);
    --Trac3897
    -- Remove leading and trailing spaces and
    -- Replace all 1 or more occurances of space with a '/'
    s_name := REGEXP_REPLACE(trim(both s_name), '( ){1,}', '/', 'g');

    Return UPPER(trim(both s_name));

EXCEPTION
    WHEN OTHERS THEN
        CALL court_phoenix_write_error('Error in jurorMiddleName ');

END;

$function$
;

-- DROP FUNCTION juror.court_phoenix_jurorsurname(text);

CREATE OR REPLACE FUNCTION juror.court_phoenix_jurorsurname(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    --space_pos            NUMBER;
    s_name    varchar(20);
    sur_name  varchar(20);
    temp_name varchar(20);


BEGIN
    -- Replace all full stops and commas with a space and
    -- Remove anything within brackets () or [] and
    -- Remove leading and trailing spaces...
    s_name := court_phoenix_jurorCleanName(name_in);

    s_name := coalesce(s_name, '~');
    --Trac3897 If null supply tilde
    --s_name = Trim(s_name); --Trac3897 - remove spaces before and after
    --temp_name := REPLACE( name_in, '`', '''' ); //Trac3697
    temp_name := REPLACE(s_name, '`', '''');
    sur_name := REPLACE(temp_name, ',', '''');
    temp_name := replace(sur_name, ' ', '');
    return UPPER(replace(temp_name, '.', ''));

EXCEPTION
    WHEN OTHERS THEN
        CALL court_phoenix_write_error('Error in jurorSurname ');

END;

$function$
;





-- DROP PROCEDURE juror.court_phoenix_write_error_atx(text);

CREATE OR REPLACE PROCEDURE juror.court_phoenix_write_error_atx(IN p_info text)
    LANGUAGE plpgsql
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info) values (lc_Job_Type, p_info);
    commit;
END;

$procedure$
;

-- DROP FUNCTION juror.get_pool_comments(timestamp, timestamp, text);

CREATE OR REPLACE FUNCTION juror.get_pool_comments(pd_startdate timestamp without time zone,
                                                   pd_enddate timestamp without time zone, pc_loccode text)
    RETURNS character varying
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE


/******************************************************************************
   NAME:       GET_POOL_COMMENTS
   PURPOSE:    To concatenate the comments for a given location

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        28-05-2003   Jeeva Konar       1. Created this function.

   PARAMETERS:
   INPUT	  		:	  pd_StartDate Date, pd_EndDate Date, pc_LocCode Varchar2
   OUTPUT:
   RETURNED VALUE	:	  Varchar2
   CALLED BY		:	  yield_performance_report in PowerBuilder application


   ******************************************************************************/
    c1 CURSOR (cd_StartDate timestamp(0), cd_EndDate timestamp(0), cc_PoolNo text)
        IS
        SELECT POOL_NO, PCOMMENT
        FROM POOL_COMMENTS pc
        WHERE POOL_NO IN (SELECT POOL_NO
                          FROM UNIQUE_POOL up
                          WHERE RETURN_DATE BETWEEN cd_StartDate AND cd_EndDate
                            AND POOL_NO LIKE cc_PoolNo)
        ORDER BY POOL_NO, LAST_UPDATE;
    lc_Comments     varchar(2000);
    lc_TempComments varchar(100);

BEGIN
    FOR i IN c1(pd_StartDate, pd_EndDate, pc_LocCode || '%')
        LOOP
            lc_TempComments := word_wrap(i.pool_no || ' : ' || i.pcomment, 28);
            lc_Comments := lc_Comments || lc_TempComments || CHR(13);
        END LOOP;

    RETURN coalesce(lc_Comments, '');

EXCEPTION
    WHEN OTHERS THEN
        RETURN '' ;

END;
$function$
;

-- DROP FUNCTION juror.get_pool_transfer_return_date_get_return_date();

CREATE OR REPLACE FUNCTION juror.get_pool_transfer_return_date_get_return_date()
    RETURNS timestamp without time zone
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE

    ln_Day              varchar(3);
    ln_DeadLine         bigint := 10;
    ld_EffectiveDate    timestamp(0);
    pd_LatestReturnDate timestamp(0);

BEGIN
    ld_EffectiveDate := clock_timestamp();
    IF (((TO_CHAR(ld_EffectiveDate, 'sssss'))::numeric <= 64800)
        -- KSO v1.3 >>
        -- AND (TO_NUMBER(TO_CHAR(ld_EffectiveDate,'d'))) = 6)   THEN -- Set the effective date to previous day
        AND (TO_CHAR(ld_EffectiveDate, 'dy') = 'fri')) THEN -- Set the effective date to previous day
    -- KSO v1.3 <<
        ld_EffectiveDate := date_trunc('day', ld_EffectiveDate) -
                            '1 days'::interval; -- if the procedure runs before 6 pm on a Friday
    END IF;
    ln_Day := TO_CHAR(ld_EffectiveDate, 'dy');
    CASE ln_Day
        WHEN 'mon' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate - 3); -- Monday
        WHEN 'tue' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate - 4); -- Tuesday
        WHEN 'wed' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate - 5); -- Wednesday
        WHEN 'thu' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate - 6); --Thursday
        WHEN 'fri' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate); -- Friday
        WHEN 'sat' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate - 1); -- Saturday
        WHEN 'sun' THEN ld_EffectiveDate := date_trunc('day', ld_EffectiveDate - 2); --Sunday
        END CASE;
    pd_LatestReturnDate := ld_EffectiveDate + ln_DeadLine + 5;
    return pd_LatestReturnDate;
END;

$function$
;

-- DROP FUNCTION juror.get_voters(int8, timestamp, timestamp, text, text, text, text);

CREATE OR REPLACE FUNCTION juror.get_voters(p_required bigint, p_mindate timestamp without time zone,
                                            p_maxdate timestamp without time zone, p_loccode text, p_areacode_list text,
                                            p_areacode_all_yn text, p_pool_type text)
    RETURNS juror.votersrowidtable
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
    -- 'C'ORONER OR 'N'ON CORONER POOLS (REGULAR POOL)
DECLARE

    l_data          juror.VOTERSROWIDTABLE := juror.VotersRowidTable();
    l_cnt           bigint           := 0;
    l_cursor        REFCURSOR;

    --t_flags text is table of varchar(2) index by integer;
    tab_flags       juror.t_flags[];

    --type t_rowid is table of varchar(30) index by integer;
    tab_rowid       juror.t_rowid[];
    l_julian_min_dt bigint           := (to_char(p_minDate, 'J'))::numeric; -- Since the input date format cannot be guaranteed  across varios systems,
    l_julian_max_dt bigint           := (to_char(p_maxDate, 'J'))::numeric; -- I am changing it to Julian Format.
    l_found         boolean          := false;


BEGIN

    -- Open dynamic cursor against the required table
    -- RFC1731 non coroner pool rules...other rules apply for coroner pools....
    open l_cursor for EXECUTE ' select rowidtochar(rowid) row_id, flags ' ||
                              ' from voters' || p_LocCode ||
                              ' where date_selected1 is null ' ||
                              ' and ((DOB is null) OR ' ||
                              ' (( to_number(to_char(DOB,''J'')) > ' || l_julian_min_dt || ' ) AND ' ||
                              '  ( to_number(to_char(DOB,''J'')) < ' || l_julian_max_dt || ' )))' ||
                              ' and PERM_DISQUAL is null ' ||
                              ' and ( Decode(substr(zip,1, instr(zip, '' '') -1 ), NULL, '' '', Trim(substr(zip,1, instr(zip, '' '') -1 ))) IN (' ||
                              p_areacode_list || ')' || -- v1.1 >> RFC 1731 specified postcode areas V1.2 <<
                              ' or ''' || p_areacode_all_yn || '''  = ''Y'' )' || -- or if ALL areas specified
                              ' and ( FLAGS is null OR ''' || p_pool_type ||
                              '''  = ''N'' )' || -- only coronor pools check flag v1.1 <<
                              ' order by dbms_random.value';

    loop
        for i in 1..p_required * 1.2
            loop
                fetch next from l_cursor into tab_rowid, tab_flags;

                -- I am using BULK COLLECT  and  LIMIT clauses, so that if the required participants fall short due to
                -- disqualified on selection categories, then there is no need to revisit the table once again
                for i in 1..ARRAY_LENGTH(tab_rowid.count, 1)
                    loop
                        IF tab_flags(i) IS NULL THEN

                            l_cnt := l_cnt + 1; -- Count only people with null values in Flagscolumn
                            l_found := l_cnt > (p_required)::numeric;
                        END IF;

                        exit when l_found; -- exit the inner loop when required number is found
                        l_data[l_data.count] := VotersRowidType(tab_rowid(i));

                    end loop;
                EXIT WHEN NOT FOUND; /* apply on l_found or l_cursor */
            end loop;
        EXIT;
    end loop;

    close l_cursor;
    IF l_cnt = 0 then
        raise exception using
            errcode = '45001'; -- raise this exception when list is empty
    END IF;

    IF l_cnt < p_required then
        raise exception using
            errcode = '45002'; -- Raise this exception when number of participants found is less than required
    END IF;

    return l_data; -- return rowid list
exception
    when sqlstate '45001' then
        RAISE EXCEPTION '%', 'Voters list is low' USING ERRCODE = '45001';
    when sqlstate '45002' then
        RAISE EXCEPTION '%', 'Voters list is empty' USING ERRCODE = '45002';
end;
$function$
;


-- DROP FUNCTION juror.local_part_no_nextval_atx(text);

CREATE OR REPLACE FUNCTION juror.local_part_no_nextval_atx(p_owner text DEFAULT current_setting('JUROR_APP.OWNER'::text, true))
    RETURNS bigint
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE
    id bigint;

BEGIN
    EXECUTE 'select local_part_no_' || p_owner || '.nextval from dual ' into STRICT id;
    return id;
END;
$function$
;

-- DROP PROCEDURE juror.payment_files_to_clob_extract();

CREATE OR REPLACE PROCEDURE juror.payment_files_to_clob_extract()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    ora2pg_rowcount   int;
    tab_creation_date juror.t_creation_date[];
    tab_header        juror.t_header[];
    tab_file_name     juror.t_file_name[];
BEGIN

    -- Check to remove duplicate payment to jurors
-- Update conf_file_ref to prevent payment from being included in the next payment file
    Update ARAMIS_PAYMENTS a1
    set con_file_ref = to_char(date_trunc('day', clock_timestamp()), 'DDMONYYYY') || 'DuplicateRemoved'
    where
-- Restrict to payments yet to be extracted.
-- This also avoids updating a payment that has already had it's corresponding duplicate flagged
        CASE WHEN con_file_ref IS NULL THEN 'N' ELSE null END = 'N'
-- Restrict to duplicates identified by audit_report vs part_hist
      and date_trunc('day', a1.creation_date) || substr(a1.part_invoice, 1, 9) ||
          ltrim(to_char(a1.expense_total, '99999.00'))
        in (SELECT creation_date || part_no || total
            from (select p.owner,
                         p.part_no,
                         substr(other_information, 10, 9)            audit_no,
                         substr(p.pool_no, 2, length(p.pool_no) - 1) total,
                         sum(a.total_amount),
                         max(date_trunc('day', date_part))           creation_date
                  from audit_report a,
                       part_hist p
                  where a.date_aramis_created >= (select min(date_trunc('day', creation_date))
                                                  from ARAMIS_PAYMENTS
                                                  where CASE WHEN con_file_ref IS NULL THEN 'N' ELSE null END = 'N')
                    and a.app_stage = 10
                    and a.owner = p.owner
                    and a.part_no = p.part_no
                    and a.audits = substr(p.other_information, 10, 9)
                    and p.history_code = 'AEDF'
                    and p.pool_no <> '#0'
                    and p.owner <> '400'
                  group by p.owner, p.part_no, substr(other_information, 10, 9), p.pool_no
                  having coalesce(sum(a.total_amount), 0) = 0) alias20)
-- select rows to update from aramis_payments i.e. exclude the first row of each set of duplicate rows
      and unique_id <> (select min(a2.unique_id)
                        from aramis_payments a2
                        where a2.owner = a1.owner
                          and substr(part_invoice, 1, 9) = substr(a1.part_invoice, 1, 9)
                          and CASE WHEN a2.con_file_ref IS NULL THEN 'N' ELSE null END = 'N');

    SELECT creation_date,
           to_char(creation_date, 'FMDDMONTHYYYY') || lpad(nextval('aramis_count'), 9, 0) || '.dat',
           'HEADER' || '|' || lpad(currval('aramis_count'), 9, 0) || '|' || lpad(to_char(total, '9999990.90'), 11)
    INTO STRICT tab_creation_date, tab_file_name, tab_header
    FROM (SELECT date_trunc('day', CREATION_DATE) creation_date, sum(expense_total) total
          FROM ARAMIS_PAYMENTS
          WHERE date_trunc('day', creation_date) <=
                date_trunc('day', current_setting('payment_files_to_clob_ext_date')::timestamp(0))
            AND CASE WHEN con_file_ref IS NULL THEN 'N' ELSE NULL END = 'N'
          GROUP BY date_trunc('day', creation_date)) alias14;

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;
    IF ora2pg_rowcount > 0 THEN
        FOR x in tab_creation_date.first..tab_creation_date.last
            loop
                begin
                    PERFORM write_to_clob(tab_creation_date(x), tab_header(x), tab_file_name(x));
                    UPDATE aramis_payments
                    SET con_file_ref = tab_file_name(x)
                    WHERE date_trunc('day', CREATION_DATE) = tab_creation_date(x);
                end;
            END LOOP;
    END IF;

    commit;

EXCEPTION
    WHEN OTHERS THEN
        rollback;
        raise;

END;

$procedure$
;


-- DROP FUNCTION juror.phoenix_checking_finalise();

CREATE OR REPLACE FUNCTION juror.phoenix_checking_finalise()
    RETURNS integer
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    police_check CURSOR FOR
        SELECT t.part_no,
               t.last_name,
               t.first_name,
               t.postcode,
               t.date_of_birth,
               t.result,
               t.checked,
               p.pool_no,
               p.read_only,
               p.loc_code,
               p.oid row_idd
        from phoenix_temp t,
             pool p
        where (t.result = 'P' or t.result = 'F')
          and p.owner = '400'
          and p.part_no = t.part_no
          and p.is_active = 'Y';
    l_owner     varchar(3);
    lc_Job_Type text;

BEGIN

    lc_Job_Type := 'phoenix_checking_FINALISE()';

    for each_participant in police_check
        loop

            update pool
            set phoenix_checked = CASE
                                      WHEN each_participant.checked = 'C' THEN 'C'
                                      WHEN each_participant.checked = 'U' THEN 'U'
                                      WHEN each_participant.checked IS NULL THEN 'U' END,
                police_check    = CASE
                                      WHEN each_participant.result = 'P' THEN 'P'
                                      WHEN each_participant.result = 'F' THEN 'F' END,
                status          = CASE
                                      WHEN each_participant.result = 'P' THEN status
                                      WHEN each_participant.result = 'F' THEN '6' END,
                disq_code       = CASE WHEN each_participant.result = 'F' THEN 'E' ELSE NULL END,
                date_disq       = CASE WHEN each_participant.result = 'F' THEN clock_timestamp() ELSE NULL END
            where rowid = each_participant.row_idd;

            if each_participant.result = 'P' then
                -- RFS 3681 Changed value for other_information
                insert into part_hist(owner,
                                      part_no,
                                      date_part,
                                      history_code,
                                      user_id,
                                      other_information,
                                      pool_no)
                values ('400',
                        each_participant.part_no,
                        clock_timestamp(),
                        'POLG',
                        'SYSTEM',
                        CASE
                            WHEN each_participant.checked = 'C' THEN 'Passed'
                            WHEN each_participant.checked = 'U' THEN 'Unchecked - timed out'
                            WHEN each_participant.checked IS NULL THEN 'Unchecked - timed out' END,
                        each_participant.pool_no);
            else
                insert into disq_lett(owner,
                                      part_no,
                                      disq_code,
                                      date_disq,
                                      date_printed,
                                      printed)
                values ('400',
                        each_participant.part_no,
                        'E',
                        clock_timestamp(),
                        null,
                        null);

                -- RFS 3681 Changed value for other_information
                insert into part_hist(owner,
                                      part_no,
                                      date_part,
                                      history_code,
                                      user_id,
                                      other_information,
                                      pool_no)
                values ('400',
                        each_participant.part_no,
                        clock_timestamp(),
                        'POLF',
                        'SYSTEM',
                        'Failed',
                        each_participant.pool_no);

                insert into part_hist(owner,
                                      part_no,
                                      date_part,
                                      history_code,
                                      user_id,
                                      other_information,
                                      pool_no)
                values ('400',
                        each_participant.part_no,
                        clock_timestamp() + (1 / 86400),
                        'PDIS',
                        'SYSTEM',
                        'Disqualify - E',
                        each_participant.pool_no);

            end if;


            -- RFS 3681 Update/insert court copy of juror record if juror has already transfered to the court
            if each_participant.read_only = 'Y' then

                select context_id
                into STRICT l_owner
                from context_data
                where loc_code = each_participant.loc_code;

                update pool
                set phoenix_checked = CASE
                                          WHEN each_participant.checked = 'C' THEN 'C'
                                          WHEN each_participant.checked = 'U' THEN 'U'
                                          WHEN each_participant.checked IS NULL THEN 'U' END,
                    police_check    = CASE
                                          WHEN each_participant.result = 'P' THEN 'P'
                                          WHEN each_participant.result = 'F' THEN 'F' END,
                    status          = CASE
                                          WHEN each_participant.result = 'P' THEN status
                                          WHEN each_participant.result = 'F' THEN '6' END,
                    disq_code       = CASE WHEN each_participant.result = 'F' THEN 'E' ELSE NULL END,
                    date_disq       = CASE WHEN each_participant.result = 'F' THEN clock_timestamp() ELSE NULL END
                where owner = l_owner
                  and part_no = each_participant.part_no
                  and is_active = 'Y';

                if each_participant.result = 'P' then
                    insert into part_hist(owner,
                                          part_no,
                                          date_part,
                                          history_code,
                                          user_id,
                                          other_information,
                                          pool_no)
                    values (l_owner,
                            each_participant.part_no,
                            clock_timestamp(),
                            'POLG',
                            'SYSTEM',
                            CASE
                                WHEN each_participant.checked = 'C' THEN 'Passed'
                                WHEN each_participant.checked = 'U' THEN 'Unchecked - timed out'
                                WHEN each_participant.checked IS NULL THEN 'Unchecked - timed out' END,
                            each_participant.pool_no);
                else
                    insert into part_hist(owner,
                                          part_no,
                                          date_part,
                                          history_code,
                                          user_id,
                                          other_information,
                                          pool_no)
                    values (l_owner,
                            each_participant.part_no,
                            clock_timestamp(),
                            'POLF',
                            'SYSTEM',
                            'Failed',
                            each_participant.pool_no);

                    insert into part_hist(owner,
                                          part_no,
                                          date_part,
                                          history_code,
                                          user_id,
                                          other_information,
                                          pool_no)
                    values (l_owner,
                            each_participant.part_no,
                            clock_timestamp() + (1 / 86400),
                            'PDIS',
                            'SYSTEM',
                            'Disqualify - E',
                            each_participant.pool_no);
                end if;

            end if;

        end loop;

    delete from phoenix_temp where result = 'P' or result = 'F';

    return (0);

exception
    when others then
        CALL phoenix_checking_write_error(sqlerrm);
        raise;
        return (1);

END;

$function$
;


-- DROP PROCEDURE juror.phoenix_checking_write_error_atx(text);

CREATE OR REPLACE PROCEDURE juror.phoenix_checking_write_error_atx(IN p_info text)
    LANGUAGE plpgsql
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info) values (lc_Job_Type, p_info);
    commit;
END;

$procedure$
;

-- DROP PROCEDURE juror.phoenix_no_police_check();

CREATE OR REPLACE PROCEDURE juror.phoenix_no_police_check()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    l_check_on  varchar(1);
    lc_Job_Type text := 'phoenix_NO_POLICE_CHECK()';
    no_police_check CURSOR FOR
        SELECT part_no,
               pool_no,
               phoenix_date,
               phoenix_checked,
               lname,
               fname,
               zip,
               dob,
               loc_code,
               police_check
        from pool
        where (dob is null
            or zip is null)
          and status = 2
          and police_check is null
          and phoenix_date is not null
          and phoenix_checked is null
          and is_active = 'Y'
          and owner = '400';


BEGIN


    for each_participant in no_police_check
        loop
            update pool
            set police_check = 'I'
            where pool_no = each_participant.pool_no
              and part_no = each_participant.part_no
              and is_active = 'Y'
              and owner = '400';

            insert into part_hist(owner,
                                  part_no,
                                  date_part,
                                  history_code,
                                  user_id,
                                  other_information,
                                  pool_no)
            values ('400',
                    each_participant.part_no,
                    clock_timestamp(),
                    'POLI',
                    'SYSTEM',
                    'Insufficient Information',
                    each_participant.pool_no);
        end loop;

EXCEPTION
    when others then
        CALL phoenix_write_error(sqlerrm);
        rollback;
        raise;

END;

$procedure$
;

-- DROP PROCEDURE juror.phoenix_police_check();

CREATE OR REPLACE PROCEDURE juror.phoenix_police_check()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    l_check_on     varchar(1);
    l_lett_printed varchar(1);
    lc_Job_Type    text := 'phoenix_POLICE_CHECK()';
    police_check CURSOR FOR
        SELECT part_no,
               pool_no,
               phoenix_date,
               phoenix_checked,
               lname,
               fname,
               zip,
               dob,
               loc_code,
               police_check
        from pool
        where dob is not null
          and (read_only is null or read_only = 'N')
          and zip is not null
          and status = 2
          and coalesce(police_check, '^') != 'E'
          and phoenix_date is not null
          and phoenix_checked is null
          and is_active = 'Y'
          and owner = '400';

BEGIN

    for each_participant in police_check
        loop
            BEGIN
                select printed
                into STRICT l_lett_printed
                from confirm_lett
                where confirm_lett.part_no = each_participant.part_no;
            EXCEPTION
                when no_data_found then
                    l_lett_printed := null;
            END;

            if (l_lett_printed is null) then
                BEGIN
                    update pool
                    set phoenix_date = date_trunc('day', clock_timestamp()),
                        police_check = 'E'
                    where pool_no = each_participant.pool_no
                      and part_no = each_participant.part_no
                      and is_active = 'Y'
                      and owner = '400';


                    -- RFS 3681 Changed value for Other_information column
                    insert into part_hist(owner,
                                          part_no,
                                          date_part,
                                          history_code,
                                          user_id,
                                          other_information,
                                          pool_no)
                    values ('400',
                            each_participant.part_no,
                            clock_timestamp(),
                            'POLE',
                            'SYSTEM',
                            'Check requested',
                            each_participant.pool_no);

                    insert into phoenix_temp(part_no,
                                             last_name,
                                             first_name,
                                             postcode,
                                             date_of_birth,
                                             result,
                                             checked)
                    values (each_participant.part_no,
                            each_participant.lname,
                            each_participant.fname,
                            each_participant.zip,
                            each_participant.dob,
                            null,
                            null);
                END;
            end if;

        end loop;

EXCEPTION
    when others then
        CALL phoenix_write_error(sqlerrm);
        rollback;
        raise;

END;

$procedure$
;



-- DROP PROCEDURE juror.phoenix_write_error_atx(text);

CREATE OR REPLACE PROCEDURE juror.phoenix_write_error_atx(IN p_info text)
    LANGUAGE plpgsql
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info) values (lc_Job_Type, p_info);
    commit;
END;

$procedure$
;

-- DROP PROCEDURE juror.phoenixinterface_checkforcompletedchecks();

CREATE OR REPLACE PROCEDURE juror.phoenixinterface_checkforcompletedchecks()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    juror_part_number varchar(13);
    disqualified      varchar(1);
    result_str        varchar(1);
    check_str         varchar(1);
    jms_result        bigint;
    retry_count       bigint;
    c_read_juror CURSOR FOR SELECT id, disqualified, try_count
                            FROM juror
                            WHERE try_count > 1
                               or check_complete = 'Y';


BEGIN
    -- The various parts of this procedure are contained
    -- in seperate blocks to enable an error with one
    -- record to not stop the processing of others.
    PERFORM set_config('phoenixinterface_lc_job_type', 'phoenixinterface_CHECKFORCOMPLETEDCHECKS()', false);

    OPEN c_read_juror;

    LOOP
        BEGIN
            FETCH c_read_juror INTO juror_part_number, disqualified, retry_count;

            IF disqualified = 'Y' THEN
                result_str := 'F';
            ELSE
                result_str := 'P';
            END IF;

            IF retry_count > 1 THEN
                check_str := 'U';
            ELSE
                check_str := 'C';
            END IF;

            BEGIN
                UPDATE phoenix_temp
                SET result  = result_str,
                    checked = check_str
                WHERE part_no = juror_part_number;

                BEGIN
                    DELETE FROM juror WHERE id = juror_part_number;
                EXCEPTION
                    WHEN OTHERS THEN
                        CALL phoenixinterface_write_error('Error on deleting juror from juror table ');
                        ROLLBACK;
                        RAISE;
                END;

            EXCEPTION
                WHEN OTHERS THEN
                    CALL phoenixinterface_write_error('Error on writing check result to JMS for ' || juror_part_number);
                    ROLLBACK;
                    RAISE;
            END;

            COMMIT;

        EXCEPTION
            WHEN OTHERS THEN
                CALL phoenixinterface_write_error('Error on fetching a juror to write result ');
                ROLLBACK;
                RAISE;
        END;

        EXIT WHEN NOT FOUND; /* apply on c_read_juror */
    END LOOP;

    CLOSE c_read_juror;

    jms_result := phoenix_checking.finalise();

    IF jms_result > 0 THEN
        CALL phoenixinterface_write_error('Error on finalising results in JMS ' || jms_result);
        ROLLBACK;
        RAISE EXCEPTION '%', 'phoenix_checking.finalise has errored' USING ERRCODE = '45903';
    ELSE
        COMMIT;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('Error on opening cursor on JMS juror table ');
        ROLLBACK;
        RAISE;

END;

$procedure$
;

-- DROP FUNCTION juror.phoenixinterface_jurorcleanname(text);

CREATE OR REPLACE FUNCTION juror.phoenixinterface_jurorcleanname(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    s_name varchar(20);
    n_pos1 bigint;
    n_pos2 bigint;


BEGIN
    -- Replace all full stops and commas with a space
    s_name := REPLACE(name_in, '.', ' ');
    s_name := REPLACE(s_name, ',', ' ');

    -- Remove anything within '(' or ')'
    n_pos1 := position('(' in s_name);
    n_pos2 := position(')' in s_name);

    IF (n_pos1 > 0 AND n_pos2 > 0) THEN
        s_name := Substr(s_name, 1, n_pos1 - 1) || ' ' || Substr(s_name, n_pos2 + 1);
    END IF;

    -- Remove anything within '[' or ']'
    n_pos1 := position('[' in s_name);
    n_pos2 := position(']' in s_name);

    IF (n_pos1 > 0 AND n_pos2 > 0) THEN
        s_name := Substr(s_name, 1, n_pos1 - 1) || ' ' || Substr(s_name, n_pos2 + 1);
    END IF;

    return trim(both s_name);

EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('jurorCleanName ');

END;

$function$
;

-- DROP FUNCTION juror.phoenixinterface_jurorfirstname(text);

CREATE OR REPLACE FUNCTION juror.phoenixinterface_jurorfirstname(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    n_pos  bigint;
    s_name varchar(20);


BEGIN

    -- Replace all full stops and commas with a space and
    -- Remove anything within brackets () or [] and
    -- Remove leading and trailing spaces...
    s_name := phoenixinterface_jurorCleanName(name_in);

    -- If there is a 'split' in the name, keep Firstname
    n_pos := position(' ' in s_name);

    IF n_pos > 0 THEN
        s_name := Substr(s_name, 1, n_pos - 1);
    END IF;

    -- Check for NULL and return firstname in uppercase
    s_name := coalesce(s_name, '~');
    --Trac3897 If null supply tilde
    --Return UPPER(Trim(s_name));
    Return UPPER(s_name);

EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('Error in jurorFirstName ');

END;

$function$
;

-- DROP FUNCTION juror.phoenixinterface_jurormiddlename(text);

CREATE OR REPLACE FUNCTION juror.phoenixinterface_jurormiddlename(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    s_name varchar(20);
    n_pos  bigint;


BEGIN
    -- Replace all full stops and commas with a space and
    -- Remove anything within brackets () or [] and
    -- Remove leading and trailing spaces...
    --s_name := phoenixinterface_jurorCleanName(name_in); --Moved below for Trac3897 - ensure use middle name
    -- If there is a 'split' in the name, remove Firstname, otherwise there
    -- is no middle name to return.
    --n_pos := INSTR(s_name, ' ' ); Trac3897
    n_pos := position(' ' in name_in);

    IF n_pos > 0 THEN
        s_name := Substr(name_in, n_pos + 1); --Trac3897
    ELSE
        s_name := NULL;
    END IF;

    s_name := phoenixinterface_jurorCleanName(s_name);
    --Trac3897
    -- Remove leading and trailing spaces and
    -- Replace all 1 or more occurances of space with a '/'
    s_name := REGEXP_REPLACE(trim(both s_name), '( ){1,}', '/', 'g');

    Return UPPER(trim(both s_name));

EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('Error in jurorMiddleName ');

END;

$function$
;

-- DROP FUNCTION juror.phoenixinterface_jurorsurname(text);

CREATE OR REPLACE FUNCTION juror.phoenixinterface_jurorsurname(name_in text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
DECLARE


    space_pos bigint;
    s_name    varchar(20);
    sur_name  varchar(20);
    temp_name varchar(20);


BEGIN
    -- Replace all full stops and commas with a space and
    -- Remove anything within brackets () or [] and
    -- Remove leading and trailing spaces...
    s_name := phoenixinterface_jurorCleanName(name_in);

    s_name := coalesce(s_name, '~');
    --Trac3897 If null supply tilde
    --s_name = Trim(s_name); --Trac3897 - remove spaces before and after
    --temp_name := REPLACE( name_in, '`', '''' ); //Trac3697
    temp_name := REPLACE(s_name, '`', '''');

    sur_name := REPLACE(temp_name, ',', '''');

    temp_name := replace(sur_name, ' ', '');

    return UPPER(replace(temp_name, '.', ''));

EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('Error in jurorSurname ');

END;

$function$
;

-- DROP PROCEDURE juror.phoenixinterface_preventdups();

CREATE OR REPLACE PROCEDURE juror.phoenixinterface_preventdups()
    LANGUAGE plpgsql
AS
$procedure$
BEGIN

    PERFORM set_config('phoenixinterface_lc_job_type', 'phoenixinterface_PREVENTDUPS()', false);

    DELETE
    from phoenix_temp
    WHERE rowid NOT in (SELECT MIN(rowid)
                        FROM phoenix_temp
                        GROUP by part_no);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('Error on deleting duplicates from phoenix_temp table');
        ROLLBACK;
        RAISE;

END;

$procedure$
;

-- DROP PROCEDURE juror.phoenixinterface_readjurorrecords();

CREATE OR REPLACE PROCEDURE juror.phoenixinterface_readjurorrecords()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    first_name  varchar(20);
    sur_name    varchar(20);
    space_pos   bigint;
    middle_name varchar(20);
    c_read_juror CURSOR FOR SELECT part_no,
                                   first_name,
                                   last_name,
                                   postcode,
                                   date_of_birth
                            FROM phoenix_temp
                            WHERE result is null
                              and part_no not in (SELECT id from juror);


BEGIN
    -- The various parts of this procedure are contained
    -- in seperate blocks to enable an error with one
    -- record to not stop the processing of others.
    PERFORM set_config('phoenixinterface_lc_job_type', 'phoenixinterface_READJURORRECORDS()', false);

    FOR juror_rec IN c_read_juror
        LOOP
            BEGIN
                IF ((juror_rec.first_name IS NULL) OR (juror_rec.last_name IS NULL)) THEN
                    CALL phoenixinterface_write_error('Juror ' || juror_rec.part_no || 'contains null data');
                ELSE
                    BEGIN
                        first_name := phoenixinterface_jurorFirstName(replace(
                                replace(regexp_replace(juror_rec.first_name, '[ ]+', ' ', 'g'), ' -', '-'), '- ', '-'));

                        middle_name := phoenixinterface_jurorMiddleName(replace(
                                replace(regexp_replace(juror_rec.first_name, '[ ]+', ' ', 'g'), ' -', '-'), '- ', '-'));

                        sur_name := phoenixinterface_jurorSurname(replace(
                                replace(regexp_replace(juror_rec.last_name, '[ ]+', ' ', 'g'), ' -', '-'), '- ', '-'));

                        INSERT INTO juror(id, first_name, last_name, surname, postcode,
                                          dob, check_complete, disqualified,
                                          try_count)
                        VALUES (juror_rec.part_no,
                                first_name,
                                middle_name,
                                sur_name,
                                UPPER(replace(juror_rec.postcode, ' ', '')),
                                juror_rec.date_of_birth,
                                'N', 'N', 0);

                        COMMIT;
                    EXCEPTION
                        WHEN OTHERS THEN
                            CALL phoenixinterface_write_error('Error on writing ' || juror_rec.part_no ||
                                                              ' to juror table for checking ');
                            ROLLBACK;
                            RAISE;
                    END;
                END IF;
            END;

        END LOOP;

EXCEPTION
    WHEN OTHERS THEN
        CALL phoenixinterface_write_error('Error on opening cursor on JMS juror table ');
        ROLLBACK;
        RAISE;

END;

$procedure$
;



-- DROP PROCEDURE juror.phoenixinterface_write_error_atx(text);

CREATE OR REPLACE PROCEDURE juror.phoenixinterface_write_error_atx(IN p_info text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info)
    values (current_setting('phoenixinterface_lc_job_type')::error_log.JOB % type, p_info);

END;

$procedure$
;

-- DROP FUNCTION juror.pkg_crypto_get_hash(text);

CREATE OR REPLACE FUNCTION juror.pkg_crypto_get_hash(pin_string text)
    RETURNS character varying
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    Return pkg_crypto_get_hash(pin_string, 200);
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION '%', 'pck_crypto.get_hash()  SQLcode = ' || SQLSTATE || ' SQLErrm = ' || SQLERRM USING ERRCODE = '45000' /* code was: -2000 */;
        RETURN NULL;
END;

$function$
;

-- DROP PROCEDURE juror.pkg_pwd_rules_password_rules(text, text, text);

CREATE OR REPLACE PROCEDURE juror.pkg_pwd_rules_password_rules(IN jurorusername text, IN jurorpassword text, IN old_jurorpassword text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
DECLARE

    n            boolean;
    m            integer;
    isdigit      boolean;
    islowchar    boolean;
    isupchar     boolean;
    ispunct      boolean;
    digitarray   varchar(10);
    punctarray   varchar(25);
    lowchararray varchar(26);
    upchararray  varchar(26);
BEGIN
    digitarray := '0123456789';
    lowchararray := 'abcdefghijklmnopqrstuvwxyz';
    upchararray := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    punctarray := '!#$%^&()`*+,-/:;<=>?_?';

    -- Check if the password is same as the username
    IF NLS_LOWER(jurorpassword) = NLS_LOWER(jurorusername) THEN
        RAISE EXCEPTION '%', 'Password same as Login Name' USING ERRCODE = '45001';
    END IF;

    --Check for the minimum length of the password
    IF length(jurorpassword) < 8 THEN
        RAISE EXCEPTION '%', 'Password length less than 8' USING ERRCODE = '45002';
    END IF;

    -- Check if the password is too simple. A dictionary of words may be
    -- maintained and a check may be made so as not to allow the words
    -- that are too simple for the password.
--   IF NLS_LOWER(jurorpassword) IN ('welcome', 'database', 'account',  'user', 'password', 'oracle', 'computer', 'abcd')
--THEN
--      raise_application_error(-20002, 'Password too simple');
--   END IF;
    -- Check if the password contains at least one lower-case letter, one upper-case letter, one digit and one
    -- punctuation mark.
    -- 1. Check for the digit
    isdigit := FALSE;
    m := length(jurorpassword);
    <<digitcheck>>
    FOR i IN 1..10
        LOOP
            FOR j IN 1..m
                LOOP
                    IF substr(jurorpassword, j, 1) = substr(digitarray, i, 1) THEN
                        isdigit := TRUE;
                        EXIT digitcheck;
                    END IF;
                END LOOP;
        END LOOP;
    IF isdigit = FALSE THEN
        RAISE EXCEPTION '%',
            'Password should contain at least one digit, one uppercase character, one lowercase character and one special character from ' ||
            punctarray USING ERRCODE = '45003';
    END IF;
    -- 2. Check for the lower-case character
    islowchar := FALSE;
    <<lowchar>>
    FOR i IN 1..length(lowchararray)
        LOOP
            FOR j IN 1..m
                LOOP
                    IF substr(jurorpassword, j, 1) = substr(lowchararray, i, 1) THEN
                        islowchar := TRUE;
                        EXIT lowchar;
                    END IF;
                END LOOP;
        END LOOP;
    IF islowchar = FALSE THEN
        RAISE EXCEPTION '%',
            'Password should contain at least one digit, one uppercase character, one lowercase character and one special character from ' ||
            punctarray USING ERRCODE = '45003';
    END IF;
    -- 3. Check for the upper-case character
    isupchar := FALSE;
    <<findupper>>
    FOR i IN 1..length(upchararray)
        LOOP
            FOR j IN 1..m
                LOOP
                    IF substr(jurorpassword, j, 1) = substr(upchararray, i, 1) THEN
                        isupchar := TRUE;
                        EXIT findupper;
                    END IF;
                END LOOP;
        END LOOP;
    IF isupchar = FALSE THEN
        RAISE EXCEPTION '%',
            'Password should contain at least one digit, one uppercase character, one lowercase character and one special character from ' ||
            punctarray USING ERRCODE = '45003';
    END IF;
    -- 4. Check for the punctuation

    ispunct := FALSE;
    <<findpunct>>
    FOR i IN 1..length(punctarray)
        LOOP
            FOR j IN 1..m
                LOOP
                    IF substr(jurorpassword, j, 1) = substr(punctarray, i, 1) THEN
                        ispunct := TRUE;
                        EXIT findpunct;
                    END IF;
                END LOOP;
        END LOOP;
    IF ispunct = FALSE THEN
        RAISE EXCEPTION '%',
            'Password should contain at least one digit, one uppercase character, one lowercase character and one special character from ' ||
            punctarray USING ERRCODE = '45003';
    END IF;

END;

$procedure$
;

-- DROP PROCEDURE juror.pkg_single_pool_transfer_transfer_court_unique_pool(text, text);

CREATE OR REPLACE PROCEDURE juror.pkg_single_pool_transfer_transfer_court_unique_pool(IN p_pool_no text, IN p_location_code text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    ora2pg_rowcount   int;
    ln_up_ins_records bigint := 0;
    ln_up_found       bigint := 0;
    ln_debug_no_rows  bigint := 0;
    C5_unique_pool CURSOR (p_pool_no text) FOR
        SELECT pool_no,
               jurisdiction_code,
               date_trunc('day', return_date) return_date,
               next_date,
               pool_total,
               no_requested,
               reg_spc,
               pool_type,
               loc_code,
               new_request,
               read_only
        FROM unique_pool
        WHERE owner = '400'
          AND read_only = 'N'
          AND pool_no = p_pool_no;

BEGIN

    -- For debug only.
    select count(*)
    into STRICT ln_debug_no_rows
    FROM unique_pool
    WHERE owner = '400'
      AND read_only = 'N'
      AND pool_no = p_pool_no;

    For unique_pool_recs in c5_unique_pool(p_pool_no)
        Loop
            EXIT WHEN NOT FOUND; /* apply on c5_unique_pool */

            SELECT count(1)
            INTO STRICT ln_up_found
            FROM unique_pool
            WHERE OWNER = p_location_code
              AND pool_no = unique_pool_recs.pool_no;

            IF ln_up_found = 0 THEN

                INSERT INTO unique_pool(owner,
                                        pool_no,
                                        jurisdiction_code,
                                        return_date,
                                        next_date,
                                        pool_total,
                                        no_requested,
                                        reg_spc,
                                        pool_type,
                                        loc_code,
                                        new_request,
                                        read_only)
                VALUES (p_location_code,
                        unique_pool_recs.pool_no,
                        unique_pool_recs.jurisdiction_code,
                        unique_pool_recs.return_date,
                        unique_pool_recs.next_date,
                        unique_pool_recs.pool_total,
                        unique_pool_recs.no_requested,
                        unique_pool_recs.reg_spc,
                        unique_pool_recs.pool_type,
                        unique_pool_recs.loc_code,
                        'N',
                        'N');
                GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;

                ln_up_ins_records := ln_up_ins_records + ora2pg_rowcount;


            Else
                UPDATE unique_pool
                SET jurisdiction_code = unique_pool_recs.jurisdiction_code,
                    return_date       = unique_pool_recs.return_date,
                    next_date         = unique_pool_recs.next_date,
                    pool_total        = unique_pool_recs.pool_total,
                    no_requested      = unique_pool_recs.no_requested,
                    reg_spc           = unique_pool_recs.reg_spc,
                    pool_type         = unique_pool_recs.pool_type,
                    loc_code          = unique_pool_recs.loc_code,
                    new_request       = 'N',
                    read_only         = CASE WHEN 'OWNER' = '400' THEN 'Y' ELSE 'N' END
                WHERE pool_no = unique_pool_recs.pool_no;

            End If;

            -- update unique_pool read_only flag in Bureau
            UPDATE unique_pool
            SET read_only   ='Y'
              , new_request = 'N'
            WHERE pool_no = unique_pool_recs.pool_no
              AND owner = '400';

        End loop;

Exception
    when others then
        CALL pkg_single_pool_transfer_write_error_message('POOL TRANSFER',
                                                          'Error in TRANSFER_COURT_UNIQUE_POOL Package. ' ||
                                                          SUBSTR(SQLERRM, 1, 100));
        rollback;
        raise;
END;

$procedure$
;

-- DROP PROCEDURE juror.pkg_single_pool_transfer_transfer_pool(text);

CREATE OR REPLACE PROCEDURE juror.pkg_single_pool_transfer_transfer_pool(IN p_pool_no text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    ln_debug_no_rows bigint := 0;

-- Cursor for pool records
    C2_pool_records CURSOR (p_pool_no text) FOR
        SELECT p.oid row_id, p.*
        FROM pool p
        WHERE p.status IN (1, 2)
          AND p.owner = '400'
          and (p.read_only = 'N' or p.read_only is null)
          and p.pool_no = p_pool_no;

BEGIN

    -- For debug only.
    select count(*)
    into STRICT ln_debug_no_rows
    FROM pool p
    WHERE p.status IN (1, 2)
      AND p.owner = '400'
      and (p.read_only = 'N' or p.read_only is null)
      and p.pool_no = p_pool_no;


    For Pool_records in C2_pool_records(p_pool_no)
        Loop
            EXIT WHEN NOT FOUND; /* apply on C2_pool_records */

            INSERT INTO pool(owner, part_no,
                             pool_no,
                             poll_number,
                             title,
                             lname,
                             fname,
                             dob,
                             address,
                             address2,
                             address3,
                             address4,
                             address5,
                             address6,
                             zip,
                             h_phone,
                             w_phone,
                             w_ph_local,
                             times_sel,
                             trial_no,
                             juror_no,
                             reg_spc,
                             ret_date,
                             def_date,
                             responded,
                             date_excus,
                             exc_code,
                             acc_exc,
                             date_disq,
                             disq_code,
                             mileage,
                             location,
                             user_edtq,
                             status,
                             notes,
                             no_attendances,
                             is_active,
                             no_def_pos,
                             no_attended,
                             no_fta,
                             no_awol,
                             pool_seq,
                             edit_tag,
                             pool_type,
                             loc_code,
                             next_date,
                             on_call,
                             perm_disqual,
                             pay_county_emp,
                             pay_expenses,
                             spec_need,
                             spec_need_msg,
                             smart_card,
                             amt_spent,
                             completion_flag,
                             completion_date,
                             sort_code,
                             bank_acct_name,
                             bank_acct_no,
                             bldg_soc_roll_no,
                             was_deferred,
                             id_checked,
                             postpone,
                             welsh,
                             paid_cash,
                             travel_time,
                             scan_code,
                             financial_loss,
                             police_check,
                             last_update,
                             read_only,
                             summons_file,
                             reminder_sent,
                             phoenix_date,
                             phoenix_checked)
            VALUES (current_setting('pkg_single_pool_transfer_ld_location_code')::varchar(9),
                    pool_records.part_no,
                    pool_records.pool_no,
                    pool_records.poll_number,
                    pool_records.title,
                    pool_records.lname,
                    pool_records.fname,
                    pool_records.dob,
                    pool_records.address,
                    pool_records.address2,
                    pool_records.address3,
                    pool_records.address4,
                    pool_records.address5,
                    pool_records.address6,
                    pool_records.zip,
                    pool_records.h_phone,
                    pool_records.w_phone,
                    pool_records.w_ph_local,
                    pool_records.times_sel,
                    pool_records.trial_no,
                    pool_records.juror_no,
                    pool_records.reg_spc,
                    pool_records.ret_date,
                    pool_records.def_date,
                    pool_records.responded,
                    pool_records.date_excus,
                    pool_records.exc_code,
                    pool_records.acc_exc,
                    pool_records.date_disq,
                    pool_records.disq_code,
                    pool_records.mileage,
                    pool_records.location,
                    pool_records.user_edtq,
                    pool_records.status,
                    pool_records.notes,
                    pool_records.no_attendances,
                    pool_records.is_active,
                    pool_records.no_def_pos,
                    pool_records.no_attended,
                    pool_records.no_fta,
                    pool_records.no_awol,
                    pool_records.pool_seq,
                    pool_records.edit_tag,
                    pool_records.pool_type,
                    pool_records.loc_code,
                    pool_records.next_date,
                    pool_records.on_call,
                    pool_records.perm_disqual,
                    pool_records.pay_county_emp,
                    pool_records.pay_expenses,
                    pool_records.spec_need,
                    pool_records.spec_need_msg,
                    pool_records.smart_card,
                    pool_records.amt_spent,
                    pool_records.completion_flag,
                    pool_records.completion_date,
                    pool_records.sort_code,
                    pool_records.bank_acct_name,
                    pool_records.bank_acct_no,
                    pool_records.bldg_soc_roll_no,
                    pool_records.was_deferred,
                    pool_records.id_checked,
                    pool_records.postpone,
                    pool_records.welsh,
                    pool_records.paid_cash,
                    pool_records.travel_time,
                    pool_records.scan_code,
                    pool_records.financial_loss,
                    pool_records.police_check,
                    pool_records.last_update,
                    'N',
                    pool_records.summons_file,
                    pool_records.reminder_sent,
                    pool_records.phoenix_date,
                    pool_records.phoenix_checked);

            -- Update the read_only flag in the bureau side
            UPDATE pool
            SET read_only ='Y'
            WHERE rowid = pool_records.row_id;

            -- Insert into the part_hist details
            INSERT INTO part_hist(Owner, part_no, date_part, history_code, user_id, other_information, pool_no)
            SELECT current_setting('pkg_single_pool_transfer_ld_location_code')::varchar(9),
                   part_no,
                   date_part,
                   history_code,
                   user_id,
                   other_information,
                   pool_no
            FROM part_hist
            WHERE owner = '400'
              AND part_no = pool_records.part_no;

            -- Insert into the  phone_log table
            INSERT INTO phone_log(owner, part_no, start_call, user_id, end_call, phone_code, notes)
            SELECT current_setting('pkg_single_pool_transfer_ld_location_code')::varchar(9),
                   part_no,
                   start_call,
                   user_id,
                   end_call,
                   phone_code,
                   notes
            FROM phone_log
            WHERE owner = '400'
              AND part_no = pool_records.part_no;

        End Loop;

Exception
    WHEN OTHERS THEN
        CALL pkg_single_pool_transfer_write_error_message('POOL TRANSFER',
                                                          'Error in TRANSFER_POOL Package. ' || SUBSTR(SQLERRM, 1, 100));
        rollback;
        raise;

END;

$procedure$
;

-- DROP PROCEDURE juror.pkg_single_pool_transfer_transfer_pool_details(text);

CREATE OR REPLACE PROCEDURE juror.pkg_single_pool_transfer_transfer_pool_details(IN p_pool_no text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    ln_no_pool_recs bigint := 0;
    ln_no_unip_recs bigint := 0;
    ln_no_part_recs bigint := 0;
    ln_no_plog_recs bigint := 0;


BEGIN

    PERFORM set_config('pkg_single_pool_transfer_ld_location_code', SUBSTR(p_pool_no, 1, 3), false);
    RAISE NOTICE 'Pool: %', p_pool_no;

    Begin
        -- Transfer the POOL records.
        CALL pkg_single_pool_transfer_transfer_pool(p_pool_no);

        -- Transfer the UNIQUE_POOL records.
        CALL pkg_single_pool_transfer_transfer_court_unique_pool(p_pool_no,
                                                                 current_setting('pkg_single_pool_transfer_ld_location_code')::varchar(9));

        -- This block for debug info only.
        select count(*) into STRICT ln_no_unip_recs from unique_pool;
        select count(*)
        into STRICT ln_no_pool_recs
        from pool
        where pool_no in (SELECT pool_no
                          from unique_pool
                          where pool_no like
                                (current_setting('pkg_single_pool_transfer_ld_location_code')::varchar(9) || '%'));
        select count(*) into STRICT ln_no_plog_recs from phone_log;
        select count(*) into STRICT ln_no_part_recs from part_hist;

        commit;
        -- commit the transaction for each court.
        --rollback;
        RAISE NOTICE 'Rollback %', p_pool_no;

    EXCEPTION
        WHEN OTHERS THEN
            CALL pkg_single_pool_transfer_write_error_message('POOL TRANSFER',
                                                              'LOC_CODE :' || p_pool_no || ' : ' || SQLERRM);
            rollback;
            PERFORM set_config('pkg_single_pool_transfer_g_job_status', false, false);
    End;

    IF NOT current_setting('pkg_single_pool_transfer_g_job_status')::boolean THEN
        RAISE EXCEPTION '%', 'Error in Pool Transfer Procedure. Not all pools are transferred.' USING ERRCODE = '45001';
        RAISE EXCEPTION '%', 'Check ERROR_LOG table for failed Locations.' USING ERRCODE = '45001';
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        CALL pkg_single_pool_transfer_write_error_message('POOL TRANSFER', SQLERRM);
        rollback;
        raise;

END;

$procedure$
;



-- DROP PROCEDURE juror.pkg_single_pool_transfer_write_error_message_atx(text, text);

CREATE OR REPLACE PROCEDURE juror.pkg_single_pool_transfer_write_error_message_atx(IN p_job text, IN p_message text)
    LANGUAGE plpgsql
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info) values (p_job, p_Message);
    commit;
END;

$procedure$
;

-- DROP PROCEDURE juror.pool_request_transfer_transfer_pool_request();

CREATE OR REPLACE PROCEDURE juror.pool_request_transfer_transfer_pool_request()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    g_job_status   boolean;
    location_codes varchar(2);
    err_code       text;
    exc_message    text;
    exc_detail     text;
    exc_hint       text;
    ret            boolean := false;

    --declare the variable used in the procedure
    C1_sups_courts CURSOR FOR SELECT distinct(owner) owner
                              from UNIQUE_POOL
                              where owner <> '400';

BEGIN
    OPEN C1_sups_courts;
    Loop
        FETCH C1_sups_courts into location_codes;
        Begin
            CALL juror.pool_request_transfer_transfer_unique_pool(location_codes.owner);
            commit; -- commit the transaction for each court.
        EXCEPTION
            WHEN OTHERS THEN
                GET STACKED DIAGNOSTICS err_code = RETURNED_SQLSTATE,
                    exc_message = MESSAGE_TEXT,
                    exc_detail = PG_EXCEPTION_DETAIL,
                    exc_hint = PG_EXCEPTION_HINT;
                call juror.pool_request_transfer_write_error_message('POOL REQUEST TRANSFER',
                                                                     'LOC_CODE :' || location_codes.owner || ' : ' ||
                                                                     'Error code ' || err_code || ' - ' ||
                                                                     exc_message || ':' || exc_detail || ': ' ||
                                                                     exc_hint, ret);
                rollback;
                g_job_status := false;
        End;
    End loop;

    IF NOT g_job_status THEN
        RAISE EXCEPTION '%', 'Error in Pool Request Procedure. Not all pools are transferred. Check ERROR_LOG table for failed Locations' USING ERRCODE = '45001';
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS err_code = RETURNED_SQLSTATE,
            exc_message = MESSAGE_TEXT,
            exc_detail = PG_EXCEPTION_DETAIL,
            exc_hint = PG_EXCEPTION_HINT;

        call juror.pool_request_transfer_write_error_message('POOL REQUEST TRANSFER',
                                                             'Error code ' || err_code || ' - ' || exc_message || ':' ||
                                                             exc_detail || ': ' || exc_hint, ret);
        rollback;
        raise;
END;

$procedure$
;

-- DROP PROCEDURE juror.pool_request_transfer_transfer_unique_pool(text);

CREATE OR REPLACE PROCEDURE juror.pool_request_transfer_transfer_unique_pool(IN location_code text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
DECLARE

    ora2pg_rowcount  int;
    ln_rows_inserted bigint := 0;

    -- RFS 3681 Include attend_time
    c_unique_pool CURSOR (location_code text) FOR
        SELECT owner,
               pool_no,
               jurisdiction_code,
               return_date,
               next_date,
               no_requested,
               pool_total,
               reg_spc,
               pool_type,
               loc_code,
               new_request,
               read_only,
               attend_time
        FROM juror.unique_pool
        WHERE New_request = 'Y'
          AND owner = location_code;
    l_cnt            bigint;


BEGIN

    FOR pr IN c_unique_pool(location_code)
        loop

            SELECT count(1)
            INTO STRICT l_cnt
            FROM juror.unique_pool
            WHERE owner = '400'
              AND pool_no = pr.pool_no;


            IF l_cnt = 0 THEN

                -- RFS 3681 Included attend_time
                INSERT INTO juror.unique_pool( owner,
                                               pool_no
                                             , jurisdiction_code
                                             , return_date
                                             , next_date
                                             , no_requested
                                             , pool_total
                                             , reg_spc
                                             , pool_type
                                             , loc_code
                                             , new_request
                                             , read_only
                                             , attend_time)
                VALUES ('400',
                        pr.pool_no,
                        pr.jurisdiction_code,
                        pr.return_date,
                        pr.next_date,
                        CASE WHEN Sign(pr.no_requested) = -1 THEN 0 ELSE pr.no_requested END,
                        pr.pool_total,
                        pr.reg_spc,
                        pr.pool_type,
                        pr.loc_code,
                        CASE WHEN pr.new_request = 'Y' THEN 'T' ELSE 'N' END,
                        CASE WHEN pr.new_request = 'N' THEN 'Y' ELSE 'N' END,
                        pr.attend_time);
            END IF;

            GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


            ln_rows_inserted := ln_rows_inserted + ora2pg_rowcount;
            IF (pr.new_request = 'Y') THEN
                UPDATE juror.unique_pool
                SET new_request = 'T',
                    read_only   = 'Y'
                WHERE pool_no = pr.pool_no
                  AND owner = location_code;

            END IF;

        END loop;
END;

$procedure$
;





-- DROP PROCEDURE juror.pool_request_transfer_write_error_message_atx(text, text);

CREATE OR REPLACE PROCEDURE juror.pool_request_transfer_write_error_message_atx(IN p_job text, IN p_message text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
BEGIN
    INSERT INTO juror.ERROR_LOG(job, error_info) values (p_job, p_Message);

END;

$procedure$
;

-- DROP PROCEDURE juror.pool_transfer_get_return_date(in int8, inout timestamp);

CREATE OR REPLACE PROCEDURE juror.pool_transfer_get_return_date(IN pn_deadline bigint,
                                                                INOUT pd_latestreturndate timestamp without time zone)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
DECLARE

    ln_Day varchar(3);

BEGIN
    PERFORM set_config('pool_transfer_ld_effectivedate', clock_timestamp(), false);
    IF (((TO_CHAR(current_setting('pool_transfer_ld_effectivedate')::timestamp(0), 'sssss'))::numeric <= 64800)
        AND (TO_CHAR(current_setting('pool_transfer_ld_effectivedate')::timestamp(0), 'dy') =
             'fri')) THEN -- Set the effective date to previous day
        PERFORM set_config('pool_transfer_ld_effectivedate',
                           date_trunc('day', current_setting('pool_transfer_ld_effectivedate')::timestamp(0)) - 1,
                           false); -- if the procedure runs before 6 pm on a Friday
    END IF;
    ln_Day := TO_CHAR(current_setting('pool_transfer_ld_effectivedate')::timestamp(0), 'dy');
    CASE ln_Day
        WHEN 'mon' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0) -
                                                                                        3), false); -- Monday
        WHEN 'tue' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0) -
                                                                                        4), false); -- Tuesday
        WHEN 'wed' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0) -
                                                                                        5), false); -- Wednesday
        WHEN 'thu' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0) -
                                                                                        6), false); --Thursday
        WHEN 'fri' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0)),
                                           false); -- Friday
        WHEN 'sat' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0) -
                                                                                        1), false); -- Saturday
        WHEN 'sun' THEN PERFORM set_config('pool_transfer_ld_effectivedate', date_trunc('day',
                                                                                        current_setting('pool_transfer_ld_effectivedate')::timestamp(0) -
                                                                                        2), false); --Sunday
        END CASE;
    pd_LatestReturnDate := current_setting('pool_transfer_ld_effectivedate')::timestamp(0) + pn_DeadLine + 5;


END;

$procedure$
;

-- DROP PROCEDURE juror.pool_transfer_transfer_court_unique_pool(text);

CREATE OR REPLACE PROCEDURE juror.pool_transfer_transfer_court_unique_pool(IN location_code text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE

    ora2pg_rowcount   int;
    ln_up_ins_records bigint := 0;
    ln_up_found       bigint := 0;
    C5_unique_pool CURSOR (location_code text) FOR
        SELECT pool_no,
               jurisdiction_code,
               date_trunc('day', return_date) return_date,
               next_date,
               pool_total,
               no_requested,
               reg_spc,
               pool_type,
               u.loc_code,
               new_request,
               read_only
        FROM unique_pool u,
             court_location c
        WHERE u.owner = '400'
          AND read_only = 'N'
          and c.loc_code = u.loc_code
          AND date_trunc('day', return_date) <= ld_LatestReturnDate + coalesce(pool_transfer_adjustment_days, 0)
          AND u.loc_code in (SELECT loc_code from context_data where context_id = location_code);

BEGIN
    For unique_pool_recs in c5_unique_pool(location_code)
        Loop
            EXIT WHEN NOT FOUND; /* apply on c5_unique_pool */

            SELECT count(1)
            INTO STRICT ln_up_found
            FROM unique_pool
            WHERE OWNER = location_code
              AND pool_no = unique_pool_recs.pool_no;

            IF ln_up_found = 0 THEN

                INSERT INTO unique_pool(owner,
                                        pool_no,
                                        jurisdiction_code,
                                        return_date,
                                        next_date,
                                        pool_total,
                                        no_requested,
                                        reg_spc,
                                        pool_type,
                                        loc_code,
                                        new_request,
                                        read_only)
                VALUES (location_code, unique_pool_recs.pool_no,
                        unique_pool_recs.jurisdiction_code,
                        unique_pool_recs.return_date,
                        unique_pool_recs.next_date,
                        unique_pool_recs.pool_total,
                        unique_pool_recs.no_requested,
                        unique_pool_recs.reg_spc,
                        unique_pool_recs.pool_type,
                        unique_pool_recs.loc_code,
                        'N',
                        'N');
                GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;

                ln_up_ins_records := ln_up_ins_records + ora2pg_rowcount;


            Else
                UPDATE unique_pool
                SET jurisdiction_code = unique_pool_recs.jurisdiction_code,
                    return_date       = unique_pool_recs.return_date,
                    next_date         = unique_pool_recs.next_date,
                    pool_total        = unique_pool_recs.pool_total,
                    no_requested      = unique_pool_recs.no_requested,
                    reg_spc           = unique_pool_recs.reg_spc,
                    pool_type         = unique_pool_recs.pool_type,
                    loc_code          = unique_pool_recs.loc_code,
                    new_request       = 'N',
                    read_only         = CASE WHEN 'OWNER' = '400' THEN 'Y' ELSE 'N' END
                WHERE pool_no = unique_pool_recs.pool_no;

            End If;
            -- update unique_pool read_only flag in Bureau
            UPDATE unique_pool
            SET read_only   ='Y'
              , new_request = 'N'
            WHERE pool_no = unique_pool_recs.pool_no
              AND owner = '400';
        End loop;

Exception
    when others then
        CALL pool_transfer_write_error_message('POOL TRANSFER', 'Error in TRANSFER_COURT_UNIQUE_POOL Package. ' ||
                                                                SUBSTR(SQLERRM, 1, 100));
        rollback;
        raise;
END;

$procedure$
;

-- DROP PROCEDURE juror.pool_transfer_transfer_pool(text);

CREATE OR REPLACE PROCEDURE juror.pool_transfer_transfer_pool(IN location_code text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


-- Cursor for pool records
-- RFS 3681 included status 11
    C2_pool_records CURSOR (cp_loc_code text) FOR SELECT p.oid row_id, p.*
                                                  FROM pool p
                                                  WHERE p.status IN (1, 2, 11)
                                                    AND p.owner = '400'
                                                    and (p.read_only = 'N' or p.read_only is null)
                                                    and p.pool_no in (SELECT pool_no
                                                                      from unique_pool u,
                                                                           court_location c
                                                                      WHERE read_only = 'N'
                                                                        and c.loc_code = u.loc_code
                                                                        AND u.owner = '400'
                                                                        AND date_trunc('day', return_date) <=
                                                                            ld_LatestReturnDate +
                                                                            coalesce(pool_transfer_adjustment_days, 0)
                                                                        AND u.loc_code in
                                                                            (select loc_code from context_data where context_id = cp_loc_code));

BEGIN

    For Pool_records in C2_pool_records(location_code)
        Loop
            EXIT WHEN NOT FOUND;
            /* apply on C2_pool_records */

            -- RFS 3681 decode status 2 > 2, others > 1
            INSERT INTO pool(owner, part_no,
                             pool_no,
                             poll_number,
                             title,
                             lname,
                             fname,
                             dob,
                             address,
                             address2,
                             address3,
                             address4,
                             address5,
                             address6,
                             zip,
                             h_phone,
                             w_phone,
                             w_ph_local,
                             times_sel,
                             trial_no,
                             juror_no,
                             reg_spc,
                             ret_date,
                             def_date,
                             responded,
                             date_excus,
                             exc_code,
                             acc_exc,
                             date_disq,
                             disq_code,
                             mileage,
                             location,
                             user_edtq,
                             status,
                             notes,
                             no_attendances,
                             is_active,
                             no_def_pos,
                             no_attended,
                             no_fta,
                             no_awol,
                             pool_seq,
                             edit_tag,
                             pool_type,
                             loc_code,
                             next_date,
                             on_call,
                             perm_disqual,
                             pay_county_emp,
                             pay_expenses,
                             spec_need,
                             spec_need_msg,
                             smart_card,
                             amt_spent,
                             completion_flag,
                             completion_date,
                             sort_code,
                             bank_acct_name,
                             bank_acct_no,
                             bldg_soc_roll_no,
                             was_deferred,
                             id_checked,
                             postpone,
                             welsh,
                             paid_cash,
                             travel_time,
                             scan_code,
                             financial_loss,
                             police_check,
                             last_update,
                             read_only,
                             summons_file,
                             reminder_sent,
                             phoenix_date,
                             phoenix_checked,
                             m_phone,
                             h_email,
                             contact_preference)
            VALUES (location_code,
                    pool_records.part_no,
                    pool_records.pool_no,
                    pool_records.poll_number,
                    pool_records.title,
                    pool_records.lname,
                    pool_records.fname,
                    pool_records.dob,
                    pool_records.address,
                    pool_records.address2,
                    pool_records.address3,
                    pool_records.address4,
                    pool_records.address5,
                    pool_records.address6,
                    pool_records.zip,
                    pool_records.h_phone,
                    pool_records.w_phone,
                    pool_records.w_ph_local,
                    pool_records.times_sel,
                    pool_records.trial_no,
                    pool_records.juror_no,
                    pool_records.reg_spc,
                    pool_records.ret_date,
                    pool_records.def_date,
                    pool_records.responded,
                    pool_records.date_excus,
                    pool_records.exc_code,
                    pool_records.acc_exc,
                    pool_records.date_disq,
                    pool_records.disq_code,
                    pool_records.mileage,
                    pool_records.location,
                    pool_records.user_edtq,
                    CASE WHEN pool_records.status = 2 THEN 2 ELSE 1 END,
                    pool_records.notes,
                    pool_records.no_attendances,
                    pool_records.is_active,
                    pool_records.no_def_pos,
                    pool_records.no_attended,
                    pool_records.no_fta,
                    pool_records.no_awol,
                    pool_records.pool_seq,
                    pool_records.edit_tag,
                    pool_records.pool_type,
                    pool_records.loc_code,
                    pool_records.next_date,
                    pool_records.on_call,
                    pool_records.perm_disqual,
                    pool_records.pay_county_emp,
                    pool_records.pay_expenses,
                    pool_records.spec_need,
                    pool_records.spec_need_msg,
                    pool_records.smart_card,
                    pool_records.amt_spent,
                    pool_records.completion_flag,
                    pool_records.completion_date,
                    pool_records.sort_code,
                    pool_records.bank_acct_name,
                    pool_records.bank_acct_no,
                    pool_records.bldg_soc_roll_no,
                    pool_records.was_deferred,
                    pool_records.id_checked,
                    pool_records.postpone,
                    pool_records.welsh,
                    pool_records.paid_cash,
                    pool_records.travel_time,
                    pool_records.scan_code,
                    pool_records.financial_loss,
                    pool_records.police_check,
                    pool_records.last_update,
                    'N',
                    pool_records.summons_file,
                    pool_records.reminder_sent,
                    pool_records.phoenix_date,
                    pool_records.phoenix_checked,
                    pool_records.m_phone,
                    pool_records.h_email,
                    pool_records.contact_preference);

            -- Update the read_only flag in the bureau side
            UPDATE pool
            SET read_only ='Y'
            WHERE rowid = pool_records.row_id;


-- Insert into the part_hist details
            INSERT INTO part_hist(Owner, part_no, date_part, history_code, user_id, other_information, pool_no)
            SELECT location_code, part_no, date_part, history_code, user_id, other_information, pool_no
            FROM part_hist
            WHERE owner = '400'
              AND part_no = pool_records.part_no;


-- Insert into the  phone_log table
            INSERT INTO phone_log(owner, part_no, start_call, user_id, end_call, phone_code, notes)
            SELECT location_code, part_no, start_call, user_id, end_call, phone_code, notes
            FROM phone_log
            WHERE owner = '400'
              AND part_no = pool_records.part_no;


-- RFC 1571 Insert into the  part_amendments table
            INSERT INTO part_amendments(owner, part_no, edit_date, edit_userid, title, fname, lname, dob, address, zip,
                                        sort_code, bank_acct_name, bank_acct_no, bldg_soc_roll_no, pool_no)
            SELECT location_code,
                   part_no,
                   edit_date,
                   edit_userid,
                   title,
                   fname,
                   lname,
                   dob,
                   address,
                   zip,
                   sort_code,
                   bank_acct_name,
                   bank_acct_no,
                   bldg_soc_roll_no,
                   pool_no
            FROM part_amendments
            WHERE owner = '400'
              AND part_no = pool_records.part_no;
        End Loop;

Exception
    WHEN OTHERS THEN
        CALL pool_transfer_write_error_message('POOL TRANSFER',
                                               'Error in TRANSFER_POOL Package. ' || SUBSTR(SQLERRM, 1, 100));
        rollback;
        raise;

END;

$procedure$
;

-- DROP PROCEDURE juror.pool_transfer_transfer_pool_details();

CREATE OR REPLACE PROCEDURE juror.pool_transfer_transfer_pool_details()
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    C1_sups_courts CURSOR FOR SELECT distinct(owner) owner
                              from UNIQUE_POOL
                              where owner <> '400';
    location_codes varchar(3);

BEGIN
    OPEN C1_sups_courts;
    PERFORM set_config('pool_transfer_ln_deadline', (SELECT SP_VALUE from SYSTEM_PARAMETER where SP_ID = 7), false);
    IF current_setting('pool_transfer_ln_deadline', false) = '' THEN
        PERFORM set_config('pool_transfer_ln_deadline', 10, false);
    END IF;

    PERFORM set_config('pool_transfer_ld_latestreturndate',
                       pool_transfer_get_return_date(current_setting('pool_transfer_ln_deadline')::bigint,
                                                     current_setting('pool_transfer_ld_latestreturndate')::timestamp(0)));

    Loop
        FETCH C1_sups_courts into location_codes;
        Begin
            CALL pool_transfer_transfer_pool(location_codes.owner);
            CALL pool_transfer_transfer_court_unique_pool(location_codes.owner);
            commit; -- commit the transaction for each court.
        EXCEPTION
            WHEN OTHERS THEN
                CALL pool_transfer_write_error_message('POOL TRANSFER',
                                                       'LOC_CODE :' || location_codes.owner || ' : ' || SQLERRM);
                rollback;
                PERFORM set_config('pool_transfer_g_job_status', false, false);
        End;
    End loop;
    IF NOT current_setting('pool_transfer_g_job_status')::boolean THEN
        RAISE EXCEPTION '%', 'Error in Pool Transfer Procedure. Not all pools are transferred.' USING ERRCODE = '45001';
        RAISE EXCEPTION '%', 'Check ERROR_LOG table for failed Locations.' USING ERRCODE = '45001';
    END IF;
    commit;
EXCEPTION
    WHEN OTHERS THEN
        CALL pool_transfer_write_error_message('POOL TRANSFER', SQLERRM);
        rollback;
        raise;
END;

$procedure$
;


-- DROP PROCEDURE juror.pool_transfer_write_error_message_atx(text, text);

CREATE OR REPLACE PROCEDURE juror.pool_transfer_write_error_message_atx(IN p_job text, IN p_message text)
    LANGUAGE plpgsql
AS
$procedure$
BEGIN
    INSERT INTO ERROR_LOG(job, error_info) values (p_job, p_Message);
    commit;
END;

$procedure$
;

-- DROP PROCEDURE juror.printfiles_to_clob_extract(int8);

CREATE OR REPLACE PROCEDURE juror.printfiles_to_clob_extract(IN p_limit bigint DEFAULT 1000000)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    c1 CURSOR FOR SELECT *
                  FROM FORM_ATTR;
    l_form_type   text;
    l_max_rec_len bigint;

BEGIN
    OPEN c1;
    LOOP
        FETCH c1 into l_form_type, l_max_rec_len;
        PERFORM set_config('printfiles_to_clob_g_limit', p_limit, false);
        -- Write deatils into flat file
        begin
            PERFORM write_to_clob(l_form_type, l_max_rec_len);
        end;
    END LOOP;
    commit;

EXCEPTION
    WHEN OTHERS THEN
        rollback;
        RAISE NOTICE 'Rollback';
        RAISE EXCEPTION '%', 'Error in Extract Procedure' USING ERRCODE = '45008';
        raise;

END;

$procedure$
;

-- DROP PROCEDURE juror.printfiles_to_clob_write_to_clob(text, int8);

CREATE OR REPLACE PROCEDURE juror.printfiles_to_clob_write_to_clob(IN p_formtype text, IN p_recordlength bigint)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    c_extract CURSOR (c_FormType text) FOR
        SELECT oid                                                      row_id
             , replace(replace(detail_rec, chr(10), ' '), chr(13), ' ') detail_rec
        FROM PRINT_FILES
        WHERE form_type = p_FormType
          AND CASE WHEN extracted_flag IS NULL THEN 'N' ELSE NULL END = 'N'
          AND date_trunc('day', creation_date) <= ext_date;
    l_data     juror.t_clob_detail_array[];
    l_rowid    juror.t_clob_rowid[];
    c_lob      text;
    l_count    bigint      := 0;
    l_fileName varchar(15) := PRINT_FILES.PRINTFILE_NAME;
    l_Header   varchar(300);

BEGIN

    OPEN c_extract(p_FormType);
    LOOP
        for i in 1..current_setting('printfiles_to_clob_g_limit')::bigint
            loop
                FETCH c_extract into l_rowid, l_data;
                l_count := l_data.count;
                IF l_count > 0 THEN
                    SELECT 'JURY' || lpad(nextval('data_file_no'), 4, '0') || lpad(print_Transmission, 2, '0') || '.' ||
                           lpad(print_Transmission, 4, '0'),
                           rpad('   ' || rpad(p_FormType, 16) || lpad(l_count, 6, 0) || lpad(l_count, 6, 0) || '50' ||
                                lpad(p_RecordLength, 8, 0), 256, ' ')
                    INTO STRICT l_fileName , l_Header
                    FROM system_file
                    where owner = '400';

                    insert into content_store(request_id, document_id, file_type, data)
                    values (nextval('content_store_seq'), l_FileName, 'PRINT', NULL)
                    returning data into c_lob;

                    c_lob := l_header || chr(10);

                    FOR j in 1.. l_data.count
                        LOOP
                            c_lob := c_lob || l_data(j) || chr(10);
                            update print_files set extracted_flag = 'Y' where rowid = l_rowid(j);
                        END LOOP;

                    commit;
                END IF;
                EXIT WHEN NOT FOUND; /* apply on c_extract */
            END LOOP;
        EXIT;
    END LOOP;
exception
    when others then
        RAISE EXCEPTION '%', 'Error in write_details Procedure' USING ERRCODE = '45008';
        raise;
END;

$procedure$
;

-- DROP PROCEDURE juror.reset_clear_juror_data();

CREATE OR REPLACE PROCEDURE juror.reset_clear_juror_data()
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
BEGIN

    EXECUTE 'Truncate table attendance';
    EXECUTE 'Truncate table part_expenses';
    EXECUTE 'Truncate table appearances';
    EXECUTE 'Truncate table acct_pay_data';
    EXECUTE 'Truncate table audit_report';
    EXECUTE 'Truncate table audit_f_report';

    EXECUTE 'Truncate table def_lett';
    EXECUTE 'Truncate table exc_lett';
    EXECUTE 'Truncate table fta_lett';
    EXECUTE 'Truncate table disq_lett';
    EXECUTE 'Truncate table exc_denied_lett';
    EXECUTE 'Truncate table request_lett';
    EXECUTE 'Truncate table def_denied';
    EXECUTE 'Truncate table aramis_payments';
    EXECUTE 'Truncate table cert_lett';
    EXECUTE 'Truncate table confirm_lett';
    EXECUTE 'Truncate table postpone_lett';
    EXECUTE 'Truncate table release_lett';

    EXECUTE 'Truncate table panel';
    EXECUTE 'Truncate table trial';

    EXECUTE 'Truncate table phoenix_temp';
    EXECUTE 'Truncate table juror';
    EXECUTE 'Truncate table juror_court_police_check';

    EXECUTE 'Truncate table current_trans';
    EXECUTE 'Truncate table peak_usage';
    EXECUTE 'Truncate table pool_stats';
    EXECUTE 'Truncate table manuals';
    EXECUTE 'Truncate table part_amendments';
    EXECUTE 'Truncate table part_hist';
    EXECUTE 'Truncate table phone_log';
    EXECUTE 'Truncate table defer_dbf';
    EXECUTE 'Truncate table pool_comments';
    EXECUTE 'Truncate table pool_hist';
    EXECUTE 'Truncate table pool';
    EXECUTE 'Truncate table unique_pool';

    EXECUTE 'Truncate table coroner_pool_detail';
    EXECUTE 'Truncate table coroner_pool';

    EXECUTE 'Truncate table holidays';

    EXECUTE 'Truncate table abaccus';
    EXECUTE 'Truncate table print_files';
    EXECUTE 'Truncate table content_store';

END;

$procedure$
;

-- DROP PROCEDURE juror.reset_refresh_voters(text);

CREATE OR REPLACE PROCEDURE juror.reset_refresh_voters(IN p_loc_code text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    -- Param in: p_loc_code - Either '000' for all locations otherwise valid court location.
    --
    -- Performs refresh of the votersnnn tables
    -- If parameter in is '000' then refresh is performed for ALL court locations
    -- otherwise it is just for the specified court location.
    l_zip varchar(12);
    l_sql varchar(600);
    c_voters_table CURSOR FOR
        SELECT tname, Substr(tname, 7, 3) loc_code
        from tab
        where tname like CASE WHEN p_loc_code = '000' THEN 'VOTERS%' ELSE 'VOTERS' || p_loc_code END;

BEGIN

    FOR ii in c_voters_table
        LOOP
            -- clear down the voters table
            l_sql := 'Truncate table VOTERS' || ii.loc_code;
            EXECUTE l_sql;

            -- get post code from court_location
            select cl.loc_zip
            into STRICT l_zip
            from court_location cl
            where cl.loc_code = ii.loc_code;

            -- Ensure psotcode is in the catchment area list
            -- Assumes loc_zip contains a space
            insert into court_catchment_area(SELECT substr(loc_zip, 1, position(' ' in loc_zip) - 1), loc_code
                                             from court_location
                                             where loc_code = ii.loc_code
                                             EXCEPT
                                             SELECT postcode, loc_code
                                             from court_catchment_area);

            -- Insert 999 rows into votersnnn
            -- Sample data
            -- PART_NO	TITLE	LNAME	FNAME	DOB	FLAGS	ADDRESS	ADDRESS2	ADDRESS3	ADDRESS4	ADDRESS5	ADDRESS6	ZIP
            -- 712700001		LNAMEONE	FNAMEONE			1 STREET NAME		ANYTOWN				B4 7NA
            l_sql :=
                    'Insert into VOTERS' || ii.loc_code ||
                    ' (PART_NO, REGISTER_LETT, POLL_NUMBER, NEW_MARKER, TITLE, LNAME, FNAME, DOB, FLAGS,' ||
                    ' ADDRESS, ADDRESS2, ADDRESS3, ADDRESS4, ADDRESS5, ADDRESS6, ZIP,' ||
                    ' DATE_SELECTED1, DATE_SELECTED2, DATE_SELECTED3, REC_NUM, PERM_DISQUAL, SOURCE_ID)' ||
                    '(select ''6''||' || ii.loc_code || '||LPad(rownum, 5,''0''),' ||
                    'rownum,' ||
                    'rownum,' ||
                    'null,' ||
                    'null,' ||
                    '''LNAME''||rownum,' ||
                    '''FNAME''||rownum,' ||
                    'null,' ||
                    'decode(Mod(rownum, 100),0,''X'',null),' ||
                    'rownum||'' STREET NAME'',' ||
                    '''ANYTOWN'',' ||
                    'null,' ||
                    'null,' ||
                    'null,' ||
                    'null,' ||
                    '''' || l_zip || ''',' ||
                    'null,' ||
                    'null,' ||
                    'null,' ||
                    'rownum,' ||
                    'null,' ||
                    'null' ||
                    ' from dual connect by level < 1000)';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''1'',''ONE''),' ||
                     ' lname = replace(lname,''1'',''ONE'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''2'',''TWO''),' ||
                     ' lname = replace(lname,''2'',''TWO'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''3'',''THREE''),' ||
                     ' lname = replace(lname,''3'',''THREE'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''4'',''FOUR''),' ||
                     ' lname = replace(lname,''4'',''FOUR'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''5'',''FIVE''),' ||
                     ' lname = replace(lname,''5'',''FIVE'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''6'',''SIX''),' ||
                     ' lname = replace(lname,''6'',''SIX'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''7'',''SEVEN''),' ||
                     ' lname = replace(lname,''7'',''SEVEN'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''8'',''EIGHT''),' ||
                     ' lname = replace(lname,''8'',''EIGHT'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''9'',''NINE''),' ||
                     ' lname = replace(lname,''9'',''NINE'')';
            EXECUTE l_sql;

            l_sql := 'Update VOTERS' || ii.loc_code || ' set fname = replace(fname,''0'',''ZERO''),' ||
                     ' lname = replace(lname,''0'',''ZERO'')';
            EXECUTE l_sql;

        END LOOP;

    commit;

EXCEPTION
    when others then
        rollback;
        raise;

END;

$procedure$
;

-- DROP PROCEDURE juror.reset_set_summoned_dates(text, timestamp);

CREATE OR REPLACE PROCEDURE juror.reset_set_summoned_dates(IN p_pool_no text, IN p_date timestamp without time zone)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE


    -- Amend start date of pool
    -- Will only update if the pool only has jurors with status = 1 i.e. summoned
    l_count bigint;


BEGIN

    select count(1)
    into STRICT l_count
    from pool
    where pool_no = p_pool_no
      and status <> 1
      and summons_file is null; -- don't count disqualified on selection
    IF l_count = 0 THEN
        BEGIN

            -- what about the year and month in pool_no?

            update unique_pool
            set return_date = date_trunc('day', p_date),
                next_date   = date_trunc('day', p_date)
            where pool_no = p_pool_no;

            update pool
            set ret_date  = date_trunc('day', p_date),
                next_date = CASE WHEN next_date IS NULL THEN null ELSE date_trunc('day', p_date) END
            where pool_no = p_pool_no;

            commit;

        END;
    END IF;

EXCEPTION
    when others then
        rollback;
        raise;

END;

$procedure$
;

-- DROP FUNCTION juror.restrict_court(text, text);

CREATE OR REPLACE FUNCTION juror.restrict_court(p_schema text, p_object text)
    RETURNS character varying
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
BEGIN

    return '(case  when nvl(current_setting(''JUROR_APP.OWNER'', true),''400'') <> ''400'' and  owner = current_setting(''JUROR_APP.OWNER'', true) then 1 when nvl(current_setting(''JUROR_APP.OWNER'', true),''400'') = ''400''   then 1  end) = 1';
end;
$function$
;

-- DROP FUNCTION juror.return_owner(text, text);

CREATE OR REPLACE FUNCTION juror.return_owner(p_schema text, p_object text)
    RETURNS character varying
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
BEGIN
    return ' (case  when current_setting(''JUROR_APP.OWNER'', true) is NOT NULL and  owner = current_setting(''JUROR_APP.OWNER'', true) then 1 when current_setting(''JUROR_APP.OWNER'', true) IS NULL then 1 end ) = 1';
end;
$function$
;

-- DROP PROCEDURE juror.set_session_context(text, text);

CREATE OR REPLACE PROCEDURE juror.set_session_context(IN p_loccode text, IN p_clear_context text DEFAULT 'N'::text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
DECLARE


    l_context_id varchar(3);
/******************************************************************************
   NAME:       SET_SESSION_CONTEXT
   PURPOSE:    To set context for the oracle session.

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        17/08/2005      Jeeva Konar   1. Created this procedure.
   1.1        15/09/2005	Jeeva Konar		2. Consolidated JUROR_SET_CONTEXT procedure into this procedure.
   1.2	  	  04/10/2005	Jeeva Konar		3. Changed the datatype of variable to varchar2
   1.3	  	  14/10/2005	Jeeva Konar		4. Introduced setting of client identifier to that of context value
   			  					  			   This is done to help support staff to identify what was context was set for the session.
											   This value can be found in V$SESSION under  column CLIENT_IDENTIFIER.
   1.4        18/11/2005    Jeeva Konar		5. Introduced code to unsetting context


   PARAMETERS:
   INPUT:
   OUTPUT:
   RETURNED VALUE:
   CALLED BY:
   CALLS:
   EXAMPLE USE:     SET_SESSION_CONTEXT;
   ASSUMPTIONS:
   LIMITATIONS:
   ALGORITHM:
   NOTES:

******************************************************************************/
BEGIN

    IF p_clear_context = 'Y' THEN
        SET LOCAL JUROR_APP.OWNER = '';
    ELSE
        SELECT context_id
        INTO STRICT l_context_id
        FROM context_data
        WHERE loc_code = p_LocCode;

        -- using set_config as it only lasts until the end of the transactin (whether committed or not) (https://www.postgresql.org/docs/current/sql-set.html)
        SELECT set_config('JUROR_APP.OWNER', l_context_id, true);
    END IF;

EXCEPTION
    WHEN no_data_found THEN
        RAISE EXCEPTION '%', 'Invalid Location Code. Session Context not set' USING ERRCODE = '45001';
    when others then
        raise;

END;
$procedure$
;

-- DROP FUNCTION juror.set_up_voters(text, text, int4);

CREATE OR REPLACE FUNCTION juror.set_up_voters(p_court_no text, p_zip text, p_num_reqd integer)
    RETURNS integer
    LANGUAGE plpgsql
    STABLE
AS
$function$
DECLARE

    ora2pg_rowcount     int;

-- Alter as appropriate.
    lb_commit           boolean := true;

    -- Internal variables.
    ls_voter_start_no   varchar(9);
    ls_part_no          varchar(9);
    ls_voter_no         varchar(4);
    ls_fname            varchar(30);
    ls_lname            varchar(30);
    ls_address          varchar(30);
    ls_address2         varchar(30);
    ls_zip              varchar(9);
    li_zip_sector       integer := 0;
    li_rec_count        integer;
    li_recs_added       integer := 0;
    ls_execute          varchar(300);
    ls_curr_max_part_no varchar(9);
    li_curr_max_part_no integer;

BEGIN

    ls_address2 := 'TOWN' || p_court_no;

    ls_execute := 'select count(*) from voters' || p_court_no;
    EXECUTE ls_execute into STRICT li_rec_count;

    if li_rec_count > 0 then
        -- Get highest current participant number.
        ls_execute := 'select max(part_no) from voters' || p_court_no;
        EXECUTE ls_execute into STRICT ls_curr_max_part_no;

        -- Remove the first 4 chars
        ls_curr_max_part_no := substr(ls_curr_max_part_no, 5, 10);
        li_curr_max_part_no := (ls_curr_max_part_no)::numeric;
        li_curr_max_part_no := li_curr_max_part_no + 1;
        ls_voter_no := li_curr_max_part_no;
        ls_curr_max_part_no := li_curr_max_part_no::varchar;
        ls_voter_start_no := '8' || p_court_no || LPAD(ls_curr_max_part_no, 5, '0');

    else
        ls_voter_start_no := '8' || p_court_no || '00001';
        ls_voter_no := 1;
    end if;
    ls_part_no := ls_voter_start_no;

    for counter in 1..p_num_reqd
        loop
            li_zip_sector := li_zip_sector + 1;
            if li_zip_sector > 8 then
                li_zip_sector := 1;
            end if;
            ls_lname := 'LNAME' || ls_voter_no;
            ls_fname := 'FNAME' || ls_voter_no;
            ls_address := ls_voter_no || ' STREET NAME';
            ls_zip := p_zip || li_zip_sector || ' 1NN';

            --dbms_output.put_line('Inserting part no: ' || ls_part_no || ' voter no ' || ls_voter_no);

            ls_execute := 'insert into voters' || p_court_no || ' ' ||
                          ' (part_no, register_lett, poll_number, lname, fname, address, address2, zip, rec_num)' ||
                          ' values ' ||
                          ' (:ls_part_no, :ls_voter_no, :ls_voter_no, :ls_lname, :ls_fname, :ls_address, :ls_address2, :ls_zip, :ls_voter_no)';

            EXECUTE (ls_execute) using ls_part_no, ls_voter_no, ls_voter_no, ls_lname, ls_fname, ls_address, ls_address2, ls_zip, ls_voter_no;
            GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;

            --dbms_output.put_line('inserted' || SQL%rowcount);
            li_recs_added := li_recs_added + ora2pg_rowcount;

            ls_part_no := ls_part_no + 1;
            ls_voter_no := ls_voter_no + 1;

            if ls_voter_no > 99999 then
                RAISE NOTICE 'Terminated: Voter No: %', ls_voter_no;
                exit;
            end if;

        end loop;

    ls_execute := 'select count(*) from voters' || p_court_no;

    EXECUTE ls_execute into STRICT li_rec_count;

    RAISE NOTICE 'Recs added: %', li_recs_added;
    RAISE NOTICE 'Total Recs: %', li_rec_count;

    if lb_commit then
        commit;
        RAISE NOTICE 'COMMIT';
    else
        rollback;
        RAISE NOTICE 'ROLLBACK';
    end if;

    return 0;

Exception
    when others then
        RAISE NOTICE 'Insert error:%', SUBSTR(SQLERRM, 1, 100);
        rollback;
        raise;

end;
$function$
;

-- DROP FUNCTION juror.trigger_fct_attendance_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_attendance_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    NEW.LAST_UPDATE := statement_timestamp();
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_court_location_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_court_location_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    NEW.LAST_UPDATE := statement_timestamp();
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_part_hist_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_part_hist_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    NEW.LAST_UPDATE := statement_timestamp();
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_phoenix_dob_zip();

CREATE OR REPLACE FUNCTION juror.trigger_fct_phoenix_dob_zip()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
declare
    l_check_on varchar(1);
BEGIN
    BEGIN
        select coalesce(PNC_CHECK_ON, 'N')
        into STRICT l_check_on
        from JUROR.COURT_LOCATION
        where COURT_LOCATION.LOC_CODE = NEW.LOC_CODE;
        if (l_check_on = 'Y' or l_check_on = 'y') then
            BEGIN
                if (coalesce(NEW.POLICE_CHECK, '^') != 'E' and coalesce(NEW.POLICE_CHECK, '^') != 'P') then
                    BEGIN
                        NEW.PHOENIX_DATE := date_trunc('day', statement_timestamp());
                    end;
                end if;
            end;
        end if;
    exception
        when OTHERS then
            RAISE EXCEPTION '%', 'Trigger: phoenix_dob_zip ' || SQLERRM || '(' || SQLSTATE || ')' USING ERRCODE = '45902';
    END;
    RETURN NEW;
end
$function$
;

-- DROP FUNCTION juror.trigger_fct_phoenix_status();

CREATE OR REPLACE FUNCTION juror.trigger_fct_phoenix_status()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
declare
    l_check_on varchar(1);
BEGIN
    BEGIN
        if (coalesce(OLD.STATUS, 99) != 2) then
            BEGIN
                select coalesce(PNC_CHECK_ON, 'N')
                into STRICT l_check_on
                from JUROR.COURT_LOCATION
                where COURT_LOCATION.LOC_CODE = NEW.LOC_CODE;
                if (l_check_on = 'Y' or l_check_on = 'y') then
                    BEGIN
                        if (coalesce(NEW.POLICE_CHECK, '^') != 'E' and coalesce(NEW.POLICE_CHECK, '^') != 'P') then
                            BEGIN
                                NEW.PHOENIX_DATE := date_trunc('day', statement_timestamp());
                            end;
                        end if;
                    end;
                end if;
            end;
        end if;
    exception
        when OTHERS then
            RAISE EXCEPTION '%', 'Trigger: phoenix_status ' || SQLERRM || '(' || SQLSTATE || ')' USING ERRCODE = '45901';
    END;
    RETURN NEW;
end
$function$
;

-- DROP FUNCTION juror.trigger_fct_phone_log_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_phone_log_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    NEW.LAST_UPDATE := statement_timestamp();
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_pool_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_pool_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    If (NEW.READ_ONLY <> '*' and OLD.READ_ONLY <> '*') or NEW.READ_ONLY is null
        or NEW.READ_ONLY = 'N' Then
        NEW.LAST_UPDATE := statement_timestamp();
    End If;
    IF OLD.STATUS is null THEN
        IF NEW.STATUS = 6 and NEW.DISQ_CODE = 'A' and NEW.RESPONDED = 'N' THEN
            NEW.SUMMONS_FILE := 'Disq. on selection';
        END IF;
    END IF;
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_print_files_part_no();

CREATE OR REPLACE FUNCTION juror.trigger_fct_print_files_part_no()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    IF NEW.PART_NO = ' ' OR NEW.PART_NO IS NULL THEN
        NEW.PART_NO := CASE
                           WHEN NEW.FORM_TYPE = '5221' THEN SUBSTR(NEW.DETAIL_REC, 280, 9)
                           WHEN NEW.FORM_TYPE = '5221C' THEN SUBSTR(NEW.DETAIL_REC, 280, 9)
                           WHEN NEW.FORM_TYPE = '5224' THEN SUBSTR(NEW.DETAIL_REC, 632, 9)
                           WHEN NEW.FORM_TYPE = '5224A' THEN SUBSTR(NEW.DETAIL_REC, 675, 9)
                           WHEN NEW.FORM_TYPE = '5224AC' THEN SUBSTR(NEW.DETAIL_REC, 656, 9)
                           WHEN NEW.FORM_TYPE = '5224C' THEN SUBSTR(NEW.DETAIL_REC, 613, 9)
                           WHEN NEW.FORM_TYPE = '5225' THEN SUBSTR(NEW.DETAIL_REC, 632, 9)
                           WHEN NEW.FORM_TYPE = '5225C' THEN SUBSTR(NEW.DETAIL_REC, 613, 9)
                           WHEN NEW.FORM_TYPE = '5226' THEN SUBSTR(NEW.DETAIL_REC, 852, 9)
                           WHEN NEW.FORM_TYPE = '5226A' THEN SUBSTR(NEW.DETAIL_REC, 852, 9)
                           WHEN NEW.FORM_TYPE = '5226AC' THEN SUBSTR(NEW.DETAIL_REC, 833, 9)
                           WHEN NEW.FORM_TYPE = '5226C' THEN SUBSTR(NEW.DETAIL_REC, 833, 9)
                           WHEN NEW.FORM_TYPE = '5227' THEN SUBSTR(NEW.DETAIL_REC, 842, 9)
                           WHEN NEW.FORM_TYPE = '5227C' THEN SUBSTR(NEW.DETAIL_REC, 823, 9)
                           WHEN NEW.FORM_TYPE = '5228' THEN SUBSTR(NEW.DETAIL_REC, 632, 9)
                           WHEN NEW.FORM_TYPE = '5228C' THEN SUBSTR(NEW.DETAIL_REC, 653, 9)
                           WHEN NEW.FORM_TYPE = '5229' THEN SUBSTR(NEW.DETAIL_REC, 632, 9)
                           WHEN NEW.FORM_TYPE = '5229A' THEN SUBSTR(NEW.DETAIL_REC, 672, 9)
                           WHEN NEW.FORM_TYPE = '5229AC' THEN SUBSTR(NEW.DETAIL_REC, 653, 9)
                           WHEN NEW.FORM_TYPE = '5229C' THEN SUBSTR(NEW.DETAIL_REC, 613, 9)
            END;
    END IF;
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_trg_sp_insertupdate();

CREATE OR REPLACE FUNCTION juror.trigger_fct_trg_sp_insertupdate()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.CREATED_BY := current_user;
        NEW.CREATED_DATE := statement_timestamp();
        NEW.UPDATED_BY := current_user;
        NEW.UPDATED_DATE := statement_timestamp();
    ELSIF TG_OP = 'UPDATE' THEN
        NEW.UPDATED_BY := current_user;
        NEW.UPDATED_DATE := statement_timestamp();
    END IF;
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_unique_pool_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_unique_pool_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    If (NEW.READ_ONLY <> '*' and OLD.READ_ONLY <> '*') or NEW.READ_ONLY is null Then
        NEW.LAST_UPDATE := statement_timestamp();
    End If;
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.trigger_fct_welsh_location_update();

CREATE OR REPLACE FUNCTION juror.trigger_fct_welsh_location_update()
    RETURNS trigger
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$function$
BEGIN
    NEW.LAST_UPDATE := statement_timestamp();
    RETURN NEW;
END
$function$
;

-- DROP FUNCTION juror.word_wrap(text, int8);

CREATE OR REPLACE FUNCTION juror.word_wrap(p_string text, p_len bigint DEFAULT 35)
    RETURNS text
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS
$function$
DECLARE
    l_string text        := REPLACE(p_string || CHR(10), CHR(9), '    ');
    l_result text        := NULL;
    l_piece  text;
    l_ws     varchar(25) := ' ' || CHR(9);
    l_sep    varchar(5)  := NULL;
    n        bigint;

BEGIN
    LOOP
        EXIT WHEN l_string IS NULL;
        n := position(CHR(10) in l_string);
        l_piece := SUBSTR(l_string, 1, n - 1);
        l_string := SUBSTR(l_string, n + 1);
        LOOP
            EXIT WHEN l_piece IS NULL;
            n := LENGTH(l_piece);
            IF (n > p_len) THEN
                n := INSTR(SUBSTR(TRANSLATE(l_piece, l_ws, RPAD(' ', LENGTH(l_ws))), 1, p_len), ' ', -1);
                IF (coalesce(n, 0) = 0) THEN
                    n := p_len;
                END IF;
            END IF;
            l_result := l_result || l_sep || SUBSTR(l_piece, 1, n);
            l_sep := CHR(10);
            l_piece := SUBSTR(l_piece, n + 1);
        END LOOP;
    END LOOP;
    RETURN l_result;
END;
$function$
;


-- Table Triggers

create trigger court_location_update
    before
        insert
        or
        update
    on
        juror.court_location
    for each row
execute function juror.trigger_fct_court_location_update();

-- Table Triggers

create trigger phone_log_update
    before
        insert
        or
        update
    on
        juror.phone_log
    for each row
execute function juror.trigger_fct_phone_log_update();

-- Table Triggers

create trigger print_files_part_no
    before
        insert
    on
        juror.print_files
    for each row
execute function juror.trigger_fct_print_files_part_no();


-- Table Triggers

create trigger welsh_location_update
    before
        insert
        or
        update
    on
        juror.welsh_location
    for each row
execute function juror.trigger_fct_welsh_location_update();

-- Table Triggers

create trigger unique_pool_update
    before
        insert
        or
        update
    on
        juror.unique_pool
    for each row
execute function juror.trigger_fct_unique_pool_update();

-- Table Triggers

create trigger pool_update
    before
        insert
        or
        update
    on
        juror.pool
    for each row
execute function juror.trigger_fct_pool_update();

create trigger phoenix_dob_zip
    before
        update
            of dob,
            zip
    on
        juror.pool
    for each row
    when ((((old.dob is null)
        or (old.zip is null))
        and (new.dob is not null)
        and (new.zip is not null)
        and ((old.status = (2)::numeric)
            and (new.status = (2)::numeric))))
execute function juror.trigger_fct_phoenix_dob_zip();

create trigger phoenix_status
    before
        insert
        or
        update
            of status
    on
        juror.pool
    for each row
    when ((new.status = (2)::numeric))
execute function juror.trigger_fct_phoenix_status();
-- Table Triggers

create trigger part_hist_update
    before
        insert
        or
        update
    on
        juror.part_hist
    for each row
execute function juror.trigger_fct_part_hist_update();
-- Table Triggers

create trigger trg_sp_insertupdate
    before
        insert
        or
        update
    on
        juror.system_parameter
    for each row
execute function juror.trigger_fct_trg_sp_insertupdate();
-- Table Triggers

create trigger attendance_update
    before
        insert
        or
        update
    on
        juror.attendance
    for each row
execute function juror.trigger_fct_attendance_update();




-- DROP PROCEDURE juror.payment_files_to_clob_write_to_clob(timestamp, text, text);

CREATE OR REPLACE PROCEDURE juror.payment_files_to_clob_write_to_clob(IN p_creation_date timestamp without time zone,
                                                                      IN p_header text, IN p_file_name text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
DECLARE

    c_extract CURSOR FOR SELECT LOC_CODE,
                                UNIQUE_ID,
                                CREATION_DATE,
                                EXPENSE_TOTAL,
                                PART_INVOICE,
                                BANK_SORT_CODE,
                                replace(replace(replace(BANK_AC_NAME, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              BANK_AC_NAME,
                                replace(replace(replace(BANK_AC_NUMBER, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              BANK_AC_NUMBER,
                                replace(replace(replace(BUILD_SOC_NUMBER, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              BUILD_SOC_NUMBER,
                                replace(replace(replace(ADDRESS_LINE1, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              ADDRESS_LINE1,
                                replace(replace(replace(ADDRESS_LINE2, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              ADDRESS_LINE2,
                                replace(replace(replace(ADDRESS_LINE3, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              ADDRESS_LINE3,
                                replace(replace(replace(ADDRESS_LINE4, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              ADDRESS_LINE4,
                                replace(replace(replace(ADDRESS_LINE5, '|', ' '), chr(10), ' '), chr(13),
                                        ' ')                                                              ADDRESS_LINE5,
                                replace(replace(replace(POSTCODE, '|', ' '), chr(10), ' '), chr(13), ' ') POSTCODE,
                                ARAMIS_AUTH_CODE,
                                replace(replace(replace(NAME, '|', ' '), chr(10), ' '), chr(13), ' ')     NAME,
                                LOC_COST_CENTRE,
                                TRAVEL_TOTAL,
                                SUB_TOTAL,
                                FLOSS_TOTAL,
                                SUB_DATE
                         FROM juror.ARAMIS_PAYMENTS
                         WHERE date_trunc('day', CREATION_DATE) = p_creation_date;
    out_rec  varchar(450);
    out_rec2 varchar(450);
    out_rec3 varchar(450);
    c_lob    text;
    i        juror.ARAMIS_PAYMENTS%rowtype;
-- TODO take attributes from cursor and declare variables to copy into for the fetch statement
-- TODO then concat c_lob using ||

BEGIN
    -- Write header line into CLOB
    insert into content_store(request_id, document_id, file_type, data)
    values (nextval('content_store_seq'),
            p_File_Name,
            'PAYMENT',
            NULL)
    returning data into c_lob;
    c_lob := p_header || chr(10);
    OPEN c_extract;
    LOOP
        FETCH c_extract into i;
        out_rec := i.loc_code || i.unique_id || '|' || to_char(i.creation_date, 'DD-Mon-YYYY') || '|' ||
                   lpad(to_char(i.expense_total, '9999990.90'), 11) || '|' || rpad(i.loc_code || i.part_invoice, 50) ||
                   '|' || to_char(i.creation_date, 'DD-Mon-YYYY') || '|' || i.bank_sort_code || '|' ||
                   rpad(i.bank_ac_name, 18) || '|' || rpad(i.bank_ac_number, 8) || '|' || rpad(i.build_soc_number, 18);
        out_rec2 := '|' || rpad(i.address_line1, 35) || '|' || rpad(i.address_line2, 35) || '|' ||
                    rpad(i.address_line3, 35) || '|' || rpad(i.address_line4, 35);

        IF i.travel_total IS NOT NULL THEN
            out_rec3 := '|' || rpad(i.address_line5, 35) || '|' || rpad(i.postcode, 20) || '|' || i.aramis_auth_code ||
                        '|' || rpad(i.name, 50) || '|' || i.loc_cost_centre || '|' || '2' || '|' ||
                        lpad(to_char(i.travel_total, '9999990.90'), 11) || '|' || to_char(i.sub_date, 'DD-Mon-YYYY');
            c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || chr(10);
        END IF;
        IF i.sub_total IS NOT NULL THEN
            out_rec3 := '|' || rpad(i.address_line5, 35) || '|' || rpad(i.postcode, 20) || '|' || i.aramis_auth_code ||
                        '|' || rpad(i.name, 50) || '|' || i.loc_cost_centre || '|' || '1' || '|' ||
                        lpad(to_char(i.sub_total, '9999990.90'), 11) || '|' || to_char(i.sub_date, 'DD-Mon-YYYY');
            c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || chr(10);
        END IF;
        IF i.floss_total IS NOT NULL THEN
            out_rec3 := '|' || rpad(i.address_line5, 35) || '|' || rpad(i.postcode, 20) || '|' || i.aramis_auth_code ||
                        '|' || rpad(i.name, 50) || '|' || i.loc_cost_centre || '|' || '0' || '|' ||
                        lpad(to_char(i.floss_total, '9999990.90'), 11) || '|' || to_char(i.sub_date, 'DD-Mon-YYYY');
            c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || chr(10);
        END IF;
    END LOOP;
    c_lob := c_lob || '****' || chr(10);
END;

$procedure$
;
-- DROP PROCEDURE juror.court_phoenix_populate_court_checks(text, text);

CREATE OR REPLACE PROCEDURE juror.court_phoenix_populate_court_checks(IN l_part_no text, IN l_user text)
    LANGUAGE plpgsql
AS
$procedure$
DECLARE
    l_court_juror juror.POOL%ROWTYPE;
    l_pool_no     bigint;
    l_fname       varchar(20);
    l_middle_name varchar(20);
    l_sur_name    varchar(20);
    l_zip         varchar(20);
    l_dob         timestamp(0);
    lc_Job_Type   text := 'court_phoenix_POPUALTE_COURT_CHECKS';

BEGIN
    /** Get data from pool table to juror */

    select *
    into STRICT l_court_juror
    from juror.pool
    where part_no = l_part_no
      and is_active = 'Y';
    /** store data in local variables */

    l_pool_no := l_court_juror.pool_no;
    l_sur_name := l_court_juror.lname;
    l_fname := l_court_juror.fname;
    l_middle_name := l_fname;
    l_dob := l_court_juror.dob;
    l_zip := l_court_juror.zip;

    /** Parse name */

    IF ((l_fname is NULL) OR (l_sur_name is NULL)) THEN
        CALL court_phoenix_write_error('Juror ' || l_part_no || ' contains null data');
    ELSE
        /***** BUG: If name is null error is generated, but insert runs anyway *****/

        BEGIN
            l_fname :=
                    court_phoenix_jurorFirstName(replace(replace(regexp_replace(l_fname, '[ ]+', ' ', 'g'), ' -', '-'),
                                                         '- ', '-'));
            l_middle_name := court_phoenix_jurorMiddleName(replace(
                    replace(regexp_replace(l_middle_name, '[ ]+', ' ', 'g'), ' -', '-'), '- ', '-'));
            l_sur_name :=
                    court_phoenix_jurorSurname(replace(replace(regexp_replace(l_sur_name, '[ ]+', ' ', 'g'), ' -', '-'),
                                                       '- ', '-'));

            /** insert rows to court_police_checks ready for phoenix run */

            insert into juror_court_police_check(id,
                                                 surname,
                                                 first_name,
                                                 last_name,
                                                 postcode,
                                                 dob,
                                                 disqualified,
                                                 check_complete,
                                                 try_count)
            values (l_part_no,
                    l_sur_name,
                    l_fname,
                    l_middle_name,
                    UPPER(replace(l_zip, ' ', '')),
                    l_dob,
                    'N', 'N', 0);

        EXCEPTION
            WHEN OTHERS THEN
                CALL court_phoenix_write_error('Error on writing ' || l_part_no || ' to juror table for checking ');
                ROLLBACK;
                RAISE;
        END;
        /*END IF;
      --END;*/

        /** insert rows to part_hist for history */
        insert into part_hist(part_no,
                              date_part,
                              history_code,
                              user_id,
                              other_information,
                              pool_no)
        values (l_part_no,
                clock_timestamp(),
                'POLE',
                l_user,
                'Check Requested',
                l_pool_no);

        /** Update pool table + phoenix_date
   -- Note: owner included in where clause by Oracle becuase context is already set */
        update pool
        set phoenix_date = trunc(sysdate),
            police_check = 'E',
            user_EDTQ    = l_user
        where part_no = l_part_no
          and is_active = 'Y';

    END IF;
    /** End if on null names */

    --COMMIT; --Removed as app will perform commit
EXCEPTION
    when others then
        CALL court_phoenix_write_error(sqlerrm);
        rollback;
        raise;
END;

$procedure$
;



-- DROP SCHEMA juror_digital;

CREATE SCHEMA IF NOT EXISTS juror_digital;

-- DROP SEQUENCE juror_digital.change_log_item_seq;

CREATE SEQUENCE juror_digital.change_log_item_seq
    INCREMENT BY 1
    MINVALUE 0
    MAXVALUE 9223372036854775807
    START 1000
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE juror_digital.change_log_seq;

CREATE SEQUENCE juror_digital.change_log_seq
    INCREMENT BY 1
    MINVALUE 0
    MAXVALUE 9223372036854775807
    START 1000
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE juror_digital.cjs_employment_seq;

CREATE SEQUENCE juror_digital.cjs_employment_seq
    INCREMENT BY 1
    MINVALUE 0
    MAXVALUE 9223372036854775807
    START 1000
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE juror_digital.spec_need_seq;

CREATE SEQUENCE juror_digital.spec_need_seq
    INCREMENT BY 1
    MINVALUE 0
    MAXVALUE 9223372036854775807
    START 1000
    CACHE 1
    NO CYCLE;-- juror_digital.app_settings definition

-- Drop table

-- DROP TABLE juror_digital.app_settings;

CREATE TABLE juror_digital.app_settings (
                                            setting varchar(80) NOT NULL,
                                            value varchar(200) NULL,
                                            CONSTRAINT app_settings_pkey PRIMARY KEY (setting)
);


-- juror_digital.bureau_auth definition

-- Drop table

-- DROP TABLE juror_digital.bureau_auth;

CREATE TABLE juror_digital.bureau_auth (
                                           username varchar(20) NOT NULL,
                                           loginattempts int8 NOT NULL
);


-- juror_digital.change_log definition

-- Drop table

-- DROP TABLE juror_digital.change_log;

CREATE TABLE juror_digital.change_log (
                                          id int8 NOT NULL,
                                          juror_number varchar(9) NULL,
                                          "timestamp" timestamp NULL,
                                          staff varchar(20) NULL,
                                          "type" varchar(50) NULL,
                                          notes varchar(2000) NULL,
                                          "version" int8 NULL,
                                          CONSTRAINT change_log_pkey PRIMARY KEY (id)
);


-- juror_digital.change_log_item definition

-- Drop table

-- DROP TABLE juror_digital.change_log_item;

CREATE TABLE juror_digital.change_log_item (
                                               id int8 NOT NULL,
                                               change_log int8 NULL,
                                               old_key varchar(128) NULL,
                                               old_value varchar(2048) NULL,
                                               new_key varchar(128) NULL,
                                               new_value varchar(2048) NULL,
                                               "version" int8 NULL,
                                               CONSTRAINT change_log_item_pkey PRIMARY KEY (id)
);


-- juror_digital.contact_log_ext definition

-- Drop table

-- DROP TABLE juror_digital.contact_log_ext;

CREATE TABLE juror_digital.contact_log_ext (
                                               part_no varchar(9) NOT NULL,
                                               start_call timestamp(0) NOT NULL,
                                               repeat_enquiry varchar(1) NOT NULL,
                                               CONSTRAINT contact_log_ext_pkey PRIMARY KEY (part_no, start_call)
);


-- juror_digital.coroner_pool_ext definition

-- Drop table

-- DROP TABLE juror_digital.coroner_pool_ext;

CREATE TABLE juror_digital.coroner_pool_ext (
                                                cor_pool_no varchar(9) NOT NULL,
                                                email varchar(254) NOT NULL,
                                                phone varchar(15) NULL,
                                                CONSTRAINT coroner_pool_ext_pkey PRIMARY KEY (cor_pool_no)
);


-- juror_digital.court_region definition

-- Drop table

-- DROP TABLE juror_digital.court_region;

CREATE TABLE juror_digital.court_region (
                                            region_id varchar(5) NOT NULL,
                                            region_name varchar(30) NOT NULL,
                                            notify_account_key varchar(100) NULL,
                                            CONSTRAINT court_region_pkey PRIMARY KEY (region_id),
                                            CONSTRAINT court_region_region_name_key UNIQUE (region_name)
);


-- juror_digital.court_whitelist definition

-- Drop table

-- DROP TABLE juror_digital.court_whitelist;

CREATE TABLE juror_digital.court_whitelist (
                                               loc_code varchar(3) NOT NULL,
                                               CONSTRAINT court_whitelist_loc_code_key UNIQUE (loc_code)
);


-- juror_digital.expenses_rates definition

-- Drop table

-- DROP TABLE juror_digital.expenses_rates;

CREATE TABLE juror_digital.expenses_rates (
                                              expense_type varchar(80) NOT NULL,
                                              rate float4 NULL,
                                              CONSTRAINT expenses_rates_pkey PRIMARY KEY (expense_type)
);


-- juror_digital.jd_housekeeping_audit definition

-- Drop table

-- DROP TABLE juror_digital.jd_housekeeping_audit;

CREATE TABLE juror_digital.jd_housekeeping_audit (
                                                     juror_number varchar(9) NULL,
                                                     selected_date timestamp(0) NULL,
                                                     deletion_date timestamp(0) NULL,
                                                     deletion_summary varchar(1500) NULL
);


-- juror_digital.juror_response definition

-- Drop table

-- DROP TABLE juror_digital.juror_response;

CREATE TABLE juror_digital.juror_response (
                                              juror_number varchar(9) NOT NULL,
                                              date_received timestamp(0) NOT NULL,
                                              title varchar(10) NULL,
                                              first_name varchar(20) NULL,
                                              last_name varchar(20) NULL,
                                              address varchar(35) NULL,
                                              address2 varchar(35) NULL,
                                              address3 varchar(35) NULL,
                                              address4 varchar(35) NULL,
                                              address5 varchar(35) NULL,
                                              address6 varchar(35) NULL,
                                              zip varchar(10) NULL,
                                              processing_status varchar(50) NULL,
                                              date_of_birth timestamp(0) NULL,
                                              phone_number varchar(15) NULL,
                                              alt_phone_number varchar(15) NULL,
                                              email varchar(254) NULL,
                                              residency varchar(1) NOT NULL DEFAULT 'N'::character varying,
                                              residency_detail varchar(1000) NULL,
                                              mental_health_act varchar(1) NOT NULL DEFAULT 'N'::character varying,
                                              mental_health_act_details varchar(2020) NULL,
                                              bail varchar(1) NOT NULL DEFAULT 'N'::character varying,
                                              bail_details varchar(1000) NULL,
                                              convictions varchar(1) NOT NULL DEFAULT 'N'::character varying,
                                              convictions_details varchar(1000) NULL,
                                              deferral_reason varchar(1000) NULL,
                                              deferral_date varchar(1000) NULL,
                                              special_needs_arrangements varchar(1000) NULL,
                                              excusal_reason varchar(1000) NULL,
                                              processing_complete varchar(1) NULL DEFAULT 'N'::character varying,
                                              "version" int8 NULL DEFAULT 0,
                                              thirdparty_fname varchar(50) NULL,
                                              thirdparty_lname varchar(50) NULL,
                                              relationship varchar(50) NULL,
                                              main_phone varchar(50) NULL,
                                              other_phone varchar(50) NULL,
                                              email_address varchar(254) NULL,
                                              thirdparty_reason varchar(1000) NULL,
                                              thirdparty_other_reason varchar(1000) NULL,
                                              juror_phone_details varchar(1) NULL,
                                              juror_email_details varchar(1) NULL,
                                              staff_login varchar(20) NULL,
                                              staff_assignment_date timestamp(0) NULL,
                                              urgent varchar(1) NULL DEFAULT 'N'::character varying,
                                              super_urgent varchar(1) NULL DEFAULT 'N'::character varying,
                                              completed_at timestamp NULL,
                                              welsh varchar(1) NULL DEFAULT 'N'::character varying,
                                              CONSTRAINT juror_response_pkey PRIMARY KEY (juror_number)
);


-- juror_digital.juror_response_aud definition

-- Drop table

-- DROP TABLE juror_digital.juror_response_aud;

CREATE TABLE juror_digital.juror_response_aud (
                                                  juror_number varchar(9) NULL,
                                                  changed timestamp(0) NULL,
                                                  login varchar(20) NULL,
                                                  old_processing_status varchar(50) NULL,
                                                  new_processing_status varchar(50) NULL
);


-- juror_digital.juror_response_cjs_employment definition

-- Drop table

-- DROP TABLE juror_digital.juror_response_cjs_employment;

CREATE TABLE juror_digital.juror_response_cjs_employment (
                                                             juror_number varchar(9) NOT NULL,
                                                             cjs_employer varchar(100) NOT NULL,
                                                             cjs_employer_details varchar(1000) NOT NULL,
                                                             id int8 NOT NULL,
                                                             CONSTRAINT juror_response_cjs_employment_pkey PRIMARY KEY (id)
);


-- juror_digital.juror_response_special_needs definition

-- Drop table

-- DROP TABLE juror_digital.juror_response_special_needs;

CREATE TABLE juror_digital.juror_response_special_needs (
                                                            juror_number varchar(9) NOT NULL,
                                                            spec_need varchar(1) NOT NULL,
                                                            spec_need_detail varchar(1000) NOT NULL,
                                                            id int8 NOT NULL,
                                                            CONSTRAINT juror_response_special_needs_pkey PRIMARY KEY (id)
);


-- juror_digital.notify_template_field definition

-- Drop table

-- DROP TABLE juror_digital.notify_template_field;

CREATE TABLE juror_digital.notify_template_field (
                                                     id int4 NOT NULL,
                                                     template_id varchar(50) NOT NULL,
                                                     template_field varchar(40) NOT NULL,
                                                     database_field varchar(80) NOT NULL,
                                                     position_from int2 NULL,
                                                     position_to int2 NULL,
                                                     field_length int2 NULL,
                                                     convert_to_date varchar(1) NULL DEFAULT 'N'::character varying,
                                                     jd_class_name varchar(60) NULL,
                                                     jd_class_property varchar(60) NULL,
                                                     "version" int8 NULL,
                                                     CONSTRAINT notify_template_field_pkey PRIMARY KEY (id)
);


-- juror_digital.notify_template_mapping definition

-- Drop table

-- DROP TABLE juror_digital.notify_template_mapping;

CREATE TABLE juror_digital.notify_template_mapping (
                                                       template_id varchar(50) NOT NULL,
                                                       template_name varchar(40) NOT NULL,
                                                       notify_name varchar(60) NOT NULL,
                                                       form_type varchar(6) NULL,
                                                       notification_type int2 NULL,
                                                       "version" int8 NULL,
                                                       CONSTRAINT notify_template_mapping_pkey PRIMARY KEY (template_id),
                                                       CONSTRAINT notify_template_mapping_template_name_key UNIQUE (template_name)
);


-- juror_digital.paper_response definition

-- Drop table

-- DROP TABLE juror_digital.paper_response;

CREATE TABLE juror_digital.paper_response (
                                              juror_number varchar(9) NOT NULL,
                                              date_received timestamp(0) NOT NULL,
                                              title varchar(10) NULL,
                                              first_name varchar(20) NULL,
                                              last_name varchar(20) NULL,
                                              address varchar(35) NULL,
                                              address2 varchar(35) NULL,
                                              address3 varchar(35) NULL,
                                              address4 varchar(35) NULL,
                                              address5 varchar(35) NULL,
                                              zip varchar(10) NULL,
                                              processing_status varchar(50) NULL,
                                              date_of_birth timestamp(0) NULL,
                                              phone_number varchar(15) NULL,
                                              alt_phone_number varchar(15) NULL,
                                              email varchar(254) NULL,
                                              residency varchar(1) NULL,
                                              mental_health_act varchar(1) NULL,
                                              mental_health_capacity varchar(1) NULL,
                                              bail varchar(1) NULL,
                                              convictions varchar(1) NULL,
                                              special_needs_arrangements varchar(1000) NULL,
                                              relationship varchar(50) NULL,
                                              thirdparty_reason varchar(1000) NULL,
                                              deferral varchar(1) NULL DEFAULT 'N'::character varying,
                                              excusal varchar(1) NULL DEFAULT 'N'::character varying,
                                              signed varchar(1) NULL,
                                              staff_login varchar(20) NULL,
                                              urgent varchar(1) NULL DEFAULT 'N'::character varying,
                                              super_urgent varchar(1) NULL DEFAULT 'N'::character varying,
                                              processing_complete varchar(1) NULL DEFAULT 'N'::character varying,
                                              completed_at timestamp NULL,
                                              welsh varchar(1) NULL DEFAULT 'N'::character varying,
                                              CONSTRAINT paper_response_pkey PRIMARY KEY (juror_number)
);


-- juror_digital.paper_response_cjs_employment definition

-- Drop table

-- DROP TABLE juror_digital.paper_response_cjs_employment;

CREATE TABLE juror_digital.paper_response_cjs_employment (
                                                             juror_number varchar(9) NOT NULL,
                                                             cjs_employer varchar(100) NOT NULL,
                                                             cjs_employer_details varchar(1000) NULL,
                                                             id int8 NOT NULL,
                                                             CONSTRAINT paper_response_cjs_employment_pkey PRIMARY KEY (id)
);


-- juror_digital.paper_response_special_needs definition

-- Drop table

-- DROP TABLE juror_digital.paper_response_special_needs;

CREATE TABLE juror_digital.paper_response_special_needs (
                                                            juror_number varchar(9) NOT NULL,
                                                            spec_need varchar(1) NOT NULL,
                                                            spec_need_detail varchar(1000) NULL,
                                                            id int8 NOT NULL,
                                                            CONSTRAINT paper_response_special_needs_pkey PRIMARY KEY (id)
);


-- juror_digital.pool_extend definition

-- Drop table

-- DROP TABLE juror_digital.pool_extend;

CREATE TABLE juror_digital.pool_extend (
                                           part_no varchar(9) NOT NULL,
                                           is_locked varchar(1) NULL DEFAULT 'N'::character varying,
                                           CONSTRAINT pool_extend_pkey PRIMARY KEY (part_no)
);


-- juror_digital.pool_member_ext definition

-- Drop table

-- DROP TABLE juror_digital.pool_member_ext;

CREATE TABLE juror_digital.pool_member_ext (
                                               "owner" varchar(3) NOT NULL,
                                               part_no varchar(9) NOT NULL,
                                               pool_no varchar(9) NOT NULL,
                                               optic_reference varchar(8) NULL,
                                               CONSTRAINT pool_member_ext_pkey PRIMARY KEY (part_no, pool_no, owner)
);


-- juror_digital.pool_request_ext definition

-- Drop table

-- DROP TABLE juror_digital.pool_request_ext;

CREATE TABLE juror_digital.pool_request_ext (
                                                pool_no varchar(9) NOT NULL,
                                                total_no_required numeric(38) NOT NULL,
                                                last_update timestamp(0) NULL,
                                                CONSTRAINT pool_request_ext_pkey PRIMARY KEY (pool_no)
);


-- juror_digital.region_notify_template definition

-- Drop table

-- DROP TABLE juror_digital.region_notify_template;

CREATE TABLE juror_digital.region_notify_template (
                                                      region_template_id int2 NOT NULL,
                                                      template_name varchar(100) NULL,
                                                      region_id varchar(5) NULL,
                                                      triggered_template_id varchar(100) NULL,
                                                      legacy_template_id int8 NULL,
                                                      notify_template_id varchar(100) NULL,
                                                      message_format varchar(10) NULL,
                                                      welsh_language varchar(1) NULL,
                                                      CONSTRAINT region_notify_template_pkey PRIMARY KEY (region_template_id)
);


-- juror_digital.schema_history definition

-- Drop table

-- DROP TABLE juror_digital.schema_history;

CREATE TABLE juror_digital.schema_history (
                                              installed_rank int4 NOT NULL,
                                              "version" varchar(50) NULL,
                                              description varchar(200) NOT NULL,
                                              "type" varchar(20) NOT NULL,
                                              script varchar(1000) NOT NULL,
                                              checksum int4 NULL,
                                              installed_by varchar(100) NOT NULL,
                                              installed_on timestamp NOT NULL DEFAULT now(),
                                              execution_time int4 NOT NULL,
                                              success bool NOT NULL,
                                              CONSTRAINT schema_history_pk PRIMARY KEY (installed_rank)
);
CREATE INDEX schema_history_s_idx ON juror_digital.schema_history USING btree (success);


-- juror_digital.staff definition

-- Drop table

-- DROP TABLE juror_digital.staff;

CREATE TABLE juror_digital.staff (
                                     login varchar(20) NOT NULL,
                                     "name" varchar(50) NULL,
                                     "rank" int8 NULL,
                                     active int8 NULL,
                                     court_1 varchar(3) NULL,
                                     court_2 varchar(3) NULL,
                                     court_3 varchar(3) NULL,
                                     court_4 varchar(3) NULL,
                                     court_5 varchar(3) NULL,
                                     court_6 varchar(3) NULL,
                                     court_7 varchar(3) NULL,
                                     court_8 varchar(3) NULL,
                                     court_9 varchar(3) NULL,
                                     court_10 varchar(3) NULL,
                                     "version" int8 NULL,
                                     team_id int8 NULL,
                                     CONSTRAINT staff_pkey PRIMARY KEY (login)
);


-- juror_digital.staff_audit definition

-- Drop table

-- DROP TABLE juror_digital.staff_audit;

CREATE TABLE juror_digital.staff_audit (
                                           "action" varchar(20) NOT NULL,
                                           editor_login varchar(20) NOT NULL,
                                           created timestamp NOT NULL,
                                           login varchar(20) NULL,
                                           "name" varchar(50) NULL,
                                           "rank" int8 NULL,
                                           active int8 NULL,
                                           court_1 varchar(3) NULL,
                                           court_2 varchar(3) NULL,
                                           court_3 varchar(3) NULL,
                                           court_4 varchar(3) NULL,
                                           court_5 varchar(3) NULL,
                                           court_6 varchar(3) NULL,
                                           court_7 varchar(3) NULL,
                                           court_8 varchar(3) NULL,
                                           court_9 varchar(3) NULL,
                                           court_10 varchar(3) NULL,
                                           team_id int8 NULL,
                                           "version" int8 NULL,
                                           CONSTRAINT staff_audit_pkey PRIMARY KEY (action, editor_login, created)
);


-- juror_digital.staff_juror_response_audit definition

-- Drop table

-- DROP TABLE juror_digital.staff_juror_response_audit;

CREATE TABLE juror_digital.staff_juror_response_audit (
                                                          team_leader_login varchar(20) NOT NULL,
                                                          staff_login varchar(20) NULL,
                                                          juror_number varchar(9) NOT NULL,
                                                          date_received timestamp NOT NULL,
                                                          staff_assignment_date timestamp(0) NOT NULL,
                                                          created timestamp NOT NULL,
                                                          "version" int8 NULL,
                                                          CONSTRAINT staff_juror_response_audit_pkey PRIMARY KEY (team_leader_login, juror_number, date_received, created)
);


-- juror_digital.stats_auto_processed definition

-- Drop table

-- DROP TABLE juror_digital.stats_auto_processed;

CREATE TABLE juror_digital.stats_auto_processed (
                                                    processed_date timestamp(0) NOT NULL,
                                                    processed_count int4 NULL DEFAULT 0,
                                                    CONSTRAINT stats_auto_processed_pkey PRIMARY KEY (processed_date)
);


-- juror_digital.stats_deferrals definition

-- Drop table

-- DROP TABLE juror_digital.stats_deferrals;

CREATE TABLE juror_digital.stats_deferrals (
                                               bureau_or_court varchar(6) NOT NULL,
                                               exec_code varchar(1) NOT NULL,
                                               calendar_year varchar(4) NOT NULL,
                                               financial_year varchar(7) NOT NULL,
                                               week varchar(7) NOT NULL,
                                               excusal_count int4 NOT NULL,
                                               CONSTRAINT stats_deferrals_pkey PRIMARY KEY (bureau_or_court, exec_code, calendar_year, financial_year, week)
);


-- juror_digital.stats_excusals definition

-- Drop table

-- DROP TABLE juror_digital.stats_excusals;

CREATE TABLE juror_digital.stats_excusals (
                                              bureau_or_court varchar(6) NOT NULL,
                                              exec_code varchar(1) NOT NULL,
                                              calendar_year varchar(4) NOT NULL,
                                              financial_year varchar(7) NOT NULL,
                                              week varchar(7) NOT NULL,
                                              excusal_count int4 NOT NULL,
                                              CONSTRAINT stats_excusals_pkey PRIMARY KEY (bureau_or_court, exec_code, calendar_year, financial_year, week)
);


-- juror_digital.stats_not_responded definition

-- Drop table

-- DROP TABLE juror_digital.stats_not_responded;

CREATE TABLE juror_digital.stats_not_responded (
                                                   summons_month timestamp(0) NOT NULL,
                                                   loc_code varchar(3) NOT NULL,
                                                   non_responsed_count int4 NULL DEFAULT 0,
                                                   CONSTRAINT stats_not_responded_pkey PRIMARY KEY (summons_month, loc_code)
);


-- juror_digital.stats_response_times definition

-- Drop table

-- DROP TABLE juror_digital.stats_response_times;

CREATE TABLE juror_digital.stats_response_times (
                                                    summons_month timestamp(0) NOT NULL,
                                                    response_month timestamp(0) NOT NULL,
                                                    response_period varchar(15) NOT NULL,
                                                    loc_code varchar(3) NOT NULL,
                                                    response_method varchar(13) NOT NULL,
                                                    response_count int4 NULL DEFAULT 0,
                                                    CONSTRAINT stats_response_times_pkey PRIMARY KEY (summons_month, response_month, response_period, loc_code, response_method)
);


-- juror_digital.stats_thirdparty_online definition

-- Drop table

-- DROP TABLE juror_digital.stats_thirdparty_online;

CREATE TABLE juror_digital.stats_thirdparty_online (
                                                       summons_month timestamp(0) NOT NULL,
                                                       thirdparty_response_count int4 NULL DEFAULT 0,
                                                       CONSTRAINT stats_thirdparty_online_pkey PRIMARY KEY (summons_month)
);


-- juror_digital.stats_unprocessed_responses definition

-- Drop table

-- DROP TABLE juror_digital.stats_unprocessed_responses;

CREATE TABLE juror_digital.stats_unprocessed_responses (
                                                           loc_code varchar(3) NOT NULL,
                                                           unprocessed_count int4 NULL DEFAULT 0,
                                                           CONSTRAINT stats_unprocessed_responses_pkey PRIMARY KEY (loc_code)
);


-- juror_digital.stats_welsh_online_responses definition

-- Drop table

-- DROP TABLE juror_digital.stats_welsh_online_responses;

CREATE TABLE juror_digital.stats_welsh_online_responses (
                                                            summons_month timestamp(0) NOT NULL,
                                                            welsh_response_count int4 NULL DEFAULT 0,
                                                            CONSTRAINT stats_welsh_online_responses_pkey PRIMARY KEY (summons_month)
);


-- juror_digital.survey_response definition

-- Drop table

-- DROP TABLE juror_digital.survey_response;

CREATE TABLE juror_digital.survey_response (
                                               id varchar(20) NOT NULL,
                                               survey_id varchar(20) NOT NULL,
                                               user_no numeric(38) NULL,
                                               survey_response_date timestamp(0) NULL,
                                               satisfaction_desc varchar(50) NULL,
                                               created timestamp NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'::text),
                                               CONSTRAINT survey_response_pkey PRIMARY KEY (id, survey_id)
);


-- juror_digital.team definition

-- Drop table

-- DROP TABLE juror_digital.team;

CREATE TABLE juror_digital.team (
                                    id int8 NOT NULL,
                                    team_name varchar(1000) NOT NULL,
                                    "version" int8 NULL,
                                    CONSTRAINT team_pkey PRIMARY KEY (id),
                                    CONSTRAINT team_team_name_key UNIQUE (team_name)
);

-- juror_digital.abaccus source

CREATE OR REPLACE VIEW juror_digital.abaccus
AS SELECT pf.form_type,
          to_date(pf.creation_date::text, 'YYYY-MM-DD'::text) AS creation_date,
          count(pf.part_no) AS number_of_items
   FROM juror.print_files pf
   GROUP BY pf.form_type, (to_date(pf.creation_date::text, 'YYYY-MM-DD'::text));


CREATE OR REPLACE VIEW juror_digital.summons_snapshot
AS WITH original_summons_cte AS (
    SELECT ph.part_no AS juror_number,
           ph.owner,
           ph.pool_no,
           ph.date_part AS date_created,
           row_number() OVER (PARTITION BY ph.part_no ORDER BY ph.date_part) AS row_no
    FROM juror.part_hist ph
    WHERE ph.history_code::text = 'RSUM'::text
)
   SELECT os.juror_number,
          os.pool_no,
          up.return_date AS service_start_date,
          up.loc_code,
          cl.loc_name AS location_name,
          cl.loc_court_name AS court_name,
          os.date_created
   FROM original_summons_cte os
            JOIN juror.unique_pool up ON up.owner::text = os.owner::text AND up.pool_no::text = os.pool_no::text
            JOIN juror.court_location cl ON up.loc_code::text = cl.loc_code::text
   WHERE os.row_no = 1;

-- DROP FUNCTION juror_digital.reset_seq_bulk(text, int8);

CREATE OR REPLACE FUNCTION juror_digital.reset_seq_bulk(p_name text, p_val bigint)
    RETURNS integer
    LANGUAGE plpgsql
AS $function$

declare sequence_name text;
    declare l_num bigint;
    declare l_increment int;

        c_sequence_names CURSOR for
            select unnest(string_to_array(p_name, ',')) as value;

begin

    open c_sequence_names;

    loop
        fetch c_sequence_names into sequence_name;
        exit when not found;
        select  nextval(sequence_name) INTO l_num;
        l_increment := p_val - l_num - 1;
        execute 'alter sequence ' || sequence_name || ' increment by ' ||  l_increment || ' minvalue 0';
        select  nextval(sequence_name) INTO l_num;
        execute 'alter sequence ' ||  sequence_name || ' increment by 1';
    end loop;

    return 1;

END;
$function$
;
-- juror_digital.moj_juror_detail source

CREATE OR REPLACE VIEW juror_digital.moj_juror_detail
AS WITH juror_details_cte AS (
    SELECT p.part_no,
           COALESCE(s.pool_no, p.pool_no) AS pool_no,
           COALESCE(s.service_start_date, p.ret_date) AS ret_date,
           COALESCE(s.loc_code, p.loc_code) AS loc_code,
           p.title,
           p.fname,
           p.lname,
           p.address,
           p.address2,
           p.address3,
           p.address4,
           p.address5,
           p.address6,
           p.zip,
           p.next_date,
           p.status,
           p.h_phone AS phone_number,
           p.m_phone AS alt_phone_number,
           p.dob,
           p.read_only,
           p.notes,
           p.h_email AS email,
           p.last_update,
           r.title AS new_title,
           r.first_name AS new_first_name,
           r.last_name AS new_last_name,
           r.address AS new_address,
           r.address2 AS new_address2,
           r.address3 AS new_address3,
           r.address4 AS new_address4,
           r.address5 AS new_address5,
           r.address6 AS new_address6,
           r.zip AS new_zip,
           r.date_received,
           r.processing_status,
           r.phone_number AS new_phone_number,
           r.alt_phone_number AS new_alt_phone_number,
           r.date_of_birth AS new_dob,
           r.email AS new_email,
           r.thirdparty_fname,
           r.thirdparty_lname,
           r.thirdparty_reason,
           r.thirdparty_other_reason,
           r.main_phone,
           r.other_phone,
           r.email_address,
           r.relationship,
           r.residency,
           r.residency_detail,
           r.mental_health_act,
           r.mental_health_act_details,
           r.bail,
           r.bail_details,
           r.convictions,
           r.convictions_details,
           r.deferral_reason,
           r.deferral_date,
           r.special_needs_arrangements,
           r.excusal_reason,
           r.processing_complete,
           r.completed_at,
           r.version,
           r.juror_email_details,
           r.juror_phone_details,
           r.staff_login,
           r.staff_assignment_date,
           r.urgent,
           r.super_urgent,
           r.welsh
    FROM juror.pool p
             LEFT JOIN juror_digital.juror_response r ON r.juror_number::text = p.part_no::text
             LEFT JOIN juror_digital.summons_snapshot s ON r.juror_number::text = s.juror_number::text
    WHERE p.owner::text = '400'::text AND p.is_active::text = 'Y'::text
)
   SELECT j.part_no,
          j.pool_no,
          j.ret_date,
          c.loc_code,
          c.loc_name,
          c.loc_court_name,
          c.loc_address1,
          c.loc_address2,
          c.loc_address3,
          c.loc_address4,
          c.loc_address5,
          c.loc_address6,
          c.loc_zip,
          c.loc_attend_time,
          j.title,
          j.fname,
          j.lname,
          j.address,
          j.address2,
          j.address3,
          j.address4,
          j.address5,
          j.address6,
          j.zip,
          j.next_date,
          j.status,
          j.phone_number,
          j.alt_phone_number,
          j.dob,
          j.read_only,
          j.notes,
          j.email,
          j.last_update,
          j.new_title,
          j.new_first_name,
          j.new_last_name,
          j.new_address,
          j.new_address2,
          j.new_address3,
          j.new_address4,
          j.new_address5,
          j.new_address6,
          j.new_zip,
          j.date_received,
          j.processing_status,
          j.new_phone_number,
          j.new_alt_phone_number,
          j.new_dob,
          j.new_email,
          j.thirdparty_fname,
          j.thirdparty_lname,
          j.thirdparty_reason,
          j.thirdparty_other_reason,
          j.main_phone,
          j.other_phone,
          j.email_address,
          j.relationship,
          j.residency,
          j.residency_detail,
          j.mental_health_act,
          j.mental_health_act_details,
          j.bail,
          j.bail_details,
          j.convictions,
          j.convictions_details,
          j.deferral_reason,
          j.deferral_date,
          j.special_needs_arrangements,
          j.excusal_reason,
          j.processing_complete,
          j.completed_at,
          j.version,
          j.juror_email_details,
          j.juror_phone_details,
          j.staff_login,
          j.staff_assignment_date,
          j.urgent,
          j.super_urgent,
          j.welsh,
          CASE
              WHEN wl.loc_code IS NULL THEN 'N'::text
              ELSE 'Y'::text
              END AS welsh_court
   FROM juror_details_cte j
            JOIN juror.court_location c ON j.loc_code::text = c.loc_code::text
            LEFT JOIN juror.welsh_location wl ON c.loc_code::text = wl.loc_code::text;


-- juror_digital.summons_snapshot source

-- DROP SCHEMA juror_digital_user;

CREATE SCHEMA IF NOT EXISTS juror_digital_user;
-- juror_digital_user.active_pools_bureau source


-- juror_digital_user.bureau_juror_detail source

CREATE OR REPLACE VIEW juror_digital_user.bureau_juror_detail
AS SELECT c.loc_code,
          c.loc_name,
          c.loc_court_name,
          c.loc_address1,
          c.loc_address2,
          c.loc_address3,
          c.loc_address4,
          c.loc_address5,
          c.loc_address6,
          c.loc_zip,
          c.loc_attend_time,
          p.part_no,
          p.title,
          p.fname,
          p.lname,
          p.address,
          p.address2,
          p.address3,
          p.address4,
          p.address5,
          p.address6,
          p.zip,
          p.next_date,
          p.ret_date,
          p.status,
          p.h_phone AS phone_number,
          p.m_phone AS alt_phone_number,
          p.dob,
          p.read_only,
          p.pool_no,
          p.notes,
          p.h_email AS email,
          r.title AS new_title,
          r.first_name AS new_first_name,
          r.last_name AS new_last_name,
          r.address AS new_address,
          r.address2 AS new_address2,
          r.address3 AS new_address3,
          r.address4 AS new_address4,
          r.address5 AS new_address5,
          r.address6 AS new_address6,
          r.zip AS new_zip,
          r.date_received,
          r.processing_status,
          r.phone_number AS new_phone_number,
          r.alt_phone_number AS new_alt_phone_number,
          r.date_of_birth AS new_dob,
          r.email AS new_email,
          r.thirdparty_fname,
          r.thirdparty_lname,
          r.thirdparty_reason,
          r.thirdparty_other_reason,
          r.main_phone,
          r.other_phone,
          r.email_address,
          r.relationship,
          r.residency,
          r.residency_detail,
          r.mental_health_act,
          r.mental_health_act_details,
          r.bail,
          r.bail_details,
          r.convictions,
          r.convictions_details,
          r.deferral_reason,
          r.deferral_date,
          r.special_needs_arrangements,
          r.excusal_reason,
          r.processing_complete,
          r.completed_at,
          r.version,
          r.juror_email_details,
          r.juror_phone_details,
          r.staff_login,
          r.staff_assignment_date,
          r.urgent,
          r.super_urgent,
          r.welsh
   FROM juror.court_location c,
        juror_digital.juror_response r,
        juror.pool p
   WHERE p.owner::text = '400'::text AND p.is_active::text = 'Y'::text AND p.loc_code::text = c.loc_code::text AND r.juror_number::text = p.part_no::text;


-- juror_digital_user.cert_letter source

CREATE OR REPLACE VIEW juror_digital_user.cert_letter
AS SELECT cl.owner,
          cl.part_no,
          cl.printed,
          cl.date_printed
   FROM juror.cert_lett cl;


-- juror_digital_user.confirm_lett source

CREATE OR REPLACE VIEW juror_digital_user.confirm_lett
AS SELECT cl.owner,
          cl.part_no,
          cl.printed,
          cl.date_printed
   FROM juror.confirm_lett cl;


-- juror_digital_user.contact_enquiry_type source

CREATE OR REPLACE VIEW juror_digital_user.contact_enquiry_type
AS SELECT t_phone.phone_code AS enquiry_code,
          t_phone.description
   FROM juror.t_phone;


-- juror_digital_user.contact_log source

CREATE OR REPLACE VIEW juror_digital_user.contact_log
AS SELECT phone_log.owner,
          phone_log.part_no,
          phone_log.user_id,
          phone_log.start_call,
          phone_log.end_call,
          phone_log.phone_code AS enquiry_type,
          phone_log.notes,
          phone_log.last_update
   FROM juror.phone_log;


-- juror_digital_user.coroner_pool source

CREATE OR REPLACE VIEW juror_digital_user.coroner_pool
AS SELECT cp.cor_pool_no,
          cp.cor_name,
          cp.cor_court_loc,
          cp.cor_request_dt,
          cp.cor_service_dt,
          cp.cor_no_requested
   FROM juror.coroner_pool cp;


-- juror_digital_user.coroner_pool_detail source

CREATE OR REPLACE VIEW juror_digital_user.coroner_pool_detail
AS SELECT c.cor_pool_no,
          c.part_no,
          c.title,
          c.fname,
          c.lname,
          c.address1,
          c.address2,
          c.address3,
          c.address4,
          c.address5,
          c.address6,
          c.postcode
   FROM juror.coroner_pool_detail c;


-- juror_digital_user.court_catchment_view source

CREATE OR REPLACE VIEW juror_digital_user.court_catchment_view
AS SELECT c.postcode,
          c.loc_code
   FROM juror.court_catchment_area c;


-- juror_digital_user.court_location source

CREATE OR REPLACE VIEW juror_digital_user.court_location
AS SELECT c.owner,
          c.loc_code,
          c.loc_name,
          c.loc_court_name,
          c.loc_attend_time,
          c.loc_address1,
          c.loc_address2,
          c.loc_address3,
          c.loc_address4,
          c.loc_address5,
          c.loc_address6,
          c.loc_zip,
          c.loc_phone,
          c.jury_officer_phone,
          c.location_address,
          c.region_id,
          c.yield,
          c.voters_lock,
          c.term_of_service,
          c.tdd_phone,
          c.loc_signature
   FROM juror.court_location c;


-- juror_digital_user.def_denied source

CREATE OR REPLACE VIEW juror_digital_user.def_denied
AS SELECT dd.owner,
          dd.part_no,
          dd.date_def,
          dd.exc_code,
          dd.printed,
          dd.date_printed
   FROM juror.def_denied dd
   WHERE dd.owner::text = '400'::text;


-- juror_digital_user.def_lett source

CREATE OR REPLACE VIEW juror_digital_user.def_lett
AS SELECT dl.owner,
          dl.part_no,
          dl.date_def,
          dl.exc_code,
          dl.printed,
          dl.date_printed
   FROM juror.def_lett dl
   WHERE dl.owner::text = '400'::text;


-- juror_digital_user.defer_dbf source

CREATE OR REPLACE VIEW juror_digital_user.defer_dbf
AS SELECT d.owner,
          d.part_no,
          d.defer_to,
          d.checked,
          d.loc_code
   FROM juror.defer_dbf d;


-- juror_digital_user.deferral_letter source

CREATE OR REPLACE VIEW juror_digital_user.deferral_letter
AS SELECT dl.owner,
          dl.part_no,
          dl.date_def,
          dl.exc_code,
          dl.printed,
          dl.date_printed
   FROM juror.def_lett dl;


-- juror_digital_user.dis_code source

CREATE OR REPLACE VIEW juror_digital_user.dis_code
AS SELECT d.disq_code,
          d.description
   FROM juror.dis_code d
   WHERE d.enabled::text = 'Y'::text;


-- juror_digital_user.disq_lett source

CREATE OR REPLACE VIEW juror_digital_user.disq_lett
AS SELECT d.owner,
          d.part_no AS juror_number,
          d.disq_code,
          d.date_disq,
          d.date_printed,
          d.printed
   FROM juror.disq_lett d
   WHERE d.owner::text = '400'::text;


-- juror_digital_user.disqualification_letter source

CREATE OR REPLACE VIEW juror_digital_user.disqualification_letter
AS SELECT d.owner,
          d.part_no,
          d.disq_code,
          d.date_disq,
          d.date_printed,
          d.printed
   FROM juror.disq_lett d;


-- juror_digital_user.exc_code source

CREATE OR REPLACE VIEW juror_digital_user.exc_code
AS SELECT e.exc_code,
          e.description
   FROM juror.exc_code e
   WHERE e.enabled::text = 'Y'::text;


-- juror_digital_user.exc_denied_lett source

CREATE OR REPLACE VIEW juror_digital_user.exc_denied_lett
AS SELECT e.owner,
          e.part_no AS juror_number,
          e.exc_code,
          e.date_excused,
          e.date_printed,
          e.printed
   FROM juror.exc_denied_lett e
   WHERE e.owner::text = '400'::text;


-- juror_digital_user.exc_lett source

CREATE OR REPLACE VIEW juror_digital_user.exc_lett
AS SELECT e.owner,
          e.part_no AS juror_number,
          e.exc_code,
          e.date_excused,
          e.date_printed,
          e.printed
   FROM juror.exc_lett e
   WHERE e.owner::text = '400'::text;


-- juror_digital_user.excusal_denied_letter source

CREATE OR REPLACE VIEW juror_digital_user.excusal_denied_letter
AS SELECT e.owner,
          e.part_no,
          e.exc_code,
          e.date_excused,
          e.date_printed,
          e.printed
   FROM juror.exc_denied_lett e;


-- juror_digital_user.excusal_letter source

CREATE OR REPLACE VIEW juror_digital_user.excusal_letter
AS SELECT e.owner,
          e.part_no,
          e.exc_code,
          e.date_excused,
          e.date_printed,
          e.printed
   FROM juror.exc_lett e;


-- juror_digital_user.holidays source

CREATE OR REPLACE VIEW juror_digital_user.holidays
AS SELECT h.owner,
          h.holiday,
          h.description
   FROM juror.holidays h;


-- juror_digital_user.juror_responses_summoned source

CREATE OR REPLACE VIEW juror_digital_user.juror_responses_summoned
AS SELECT p.part_no,
          p.pool_no,
          p.status,
          p.is_active,
          p.read_only,
          r.processing_status,
          r.processing_complete,
          r.completed_at,
          r.welsh
   FROM juror_digital.juror_response r,
        juror.pool p
   WHERE p.owner::text = '400'::text AND p.is_active::text = 'Y'::text AND (p.status <> 1::numeric OR p.status = 1::numeric AND p.read_only::text = 'Y'::text) AND r.processing_status::text = 'TODO'::text AND r.juror_number::text = p.part_no::text;


-- juror_digital_user.login_attempts source

CREATE OR REPLACE VIEW juror_digital_user.login_attempts
AS SELECT bureau_auth.username,
          bureau_auth.loginattempts
   FROM juror_digital.bureau_auth;


-- juror_digital_user.messages source

CREATE OR REPLACE VIEW juror_digital_user.messages
AS SELECT m.part_no,
          m.file_datetime,
          m.username,
          m.loc_code,
          m.phone,
          m.email,
          m.loc_name,
          m.pool_no,
          m.subject,
          m.message_text,
          m.message_id,
          m.message_read
   FROM juror.messages m;


-- juror_digital_user.part_amendments source

CREATE OR REPLACE VIEW juror_digital_user.part_amendments
AS SELECT pa.owner,
          pa.part_no,
          pa.edit_date,
          pa.edit_userid,
          pa.title,
          pa.fname,
          pa.lname,
          pa.dob,
          pa.address,
          pa.zip,
          pa.sort_code,
          pa.bank_acct_name,
          pa.bank_acct_no,
          pa.bldg_soc_roll_no,
          pa.pool_no
   FROM juror.part_amendments pa;


-- juror_digital_user.part_hist source

CREATE OR REPLACE VIEW juror_digital_user.part_hist
AS SELECT pa.owner,
          pa.part_no,
          pa.date_part,
          pa.history_code,
          pa.user_id,
          pa.other_information,
          pa.pool_no,
          pa.last_update
   FROM juror.part_hist pa;


-- juror_digital_user."password" source

CREATE OR REPLACE VIEW juror_digital_user."password"
AS SELECT p.owner,
          p.login,
          p.password,
          p.last_used,
          p.user_level,
          p.aramis_auth_code,
          p.aramis_max_auth,
          p.password_changed_date,
          p.login_enabled_yn
   FROM juror.password p
   WHERE p.owner::text = '400'::text;


-- juror_digital_user.phone_log source

CREATE OR REPLACE VIEW juror_digital_user.phone_log
AS SELECT phone_log.owner,
          phone_log.part_no,
          phone_log.phone_code,
          phone_log.user_id,
          phone_log.notes,
          phone_log.last_update,
          phone_log.start_call,
          phone_log.end_call
   FROM juror.phone_log
   WHERE phone_log.owner::text = '400'::text;


-- juror_digital_user.pool source

CREATE OR REPLACE VIEW juror_digital_user.pool
AS SELECT p.owner,
          p.part_no,
          p.pool_no,
          p.poll_number,
          p.title,
          p.lname,
          p.fname,
          p.dob,
          p.address,
          p.address2,
          p.address3,
          p.address4,
          p.address5,
          p.address6,
          p.zip,
          p.h_phone,
          p.w_phone,
          p.w_ph_local,
          p.times_sel,
          p.trial_no,
          p.juror_no,
          p.reg_spc,
          p.ret_date,
          p.def_date,
          p.responded,
          p.date_excus,
          p.exc_code,
          p.acc_exc,
          p.date_disq,
          p.disq_code,
          p.mileage,
          p.location,
          p.user_edtq,
          p.status,
          p.notes,
          p.no_attendances,
          p.is_active,
          p.no_def_pos,
          p.no_attended,
          p.no_fta,
          p.no_awol,
          p.pool_seq,
          p.edit_tag,
          p.pool_type,
          p.loc_code,
          p.next_date,
          p.on_call,
          p.perm_disqual,
          p.pay_county_emp,
          p.pay_expenses,
          p.spec_need,
          p.spec_need_msg,
          p.smart_card,
          p.amt_spent,
          p.completion_flag,
          p.completion_date,
          p.sort_code,
          p.bank_acct_name,
          p.bank_acct_no,
          p.bldg_soc_roll_no,
          p.was_deferred,
          p.id_checked,
          p.postpone,
          p.welsh,
          p.paid_cash,
          p.travel_time,
          p.scan_code,
          p.financial_loss,
          p.police_check,
          p.last_update,
          p.read_only,
          p.summons_file,
          p.reminder_sent,
          p.phoenix_date,
          p.phoenix_checked,
          p.m_phone,
          p.h_email,
          p.contact_preference,
          p.notifications,
          p.service_comp_comms_status,
          p.transfer_date
   FROM juror.pool p
   WHERE p.owner::text = '400'::text AND p.is_active::text = 'Y'::text;


-- juror_digital_user.pool_comments source

CREATE OR REPLACE VIEW juror_digital_user.pool_comments
AS SELECT pool_comments.owner,
          pool_comments.pool_no,
          pool_comments.user_id,
          pool_comments.last_update,
          pool_comments.pcomment,
          pool_comments.no_requested
   FROM juror.pool_comments;


-- juror_digital_user.pool_court source

CREATE OR REPLACE VIEW juror_digital_user.pool_court
AS SELECT p.owner,
          p.part_no,
          p.pool_no,
          p.poll_number,
          p.title,
          p.lname,
          p.fname,
          p.dob,
          p.address,
          p.address2,
          p.address3,
          p.address4,
          p.address5,
          p.address6,
          p.zip,
          p.h_phone,
          p.w_phone,
          p.w_ph_local,
          p.times_sel,
          p.trial_no,
          p.juror_no,
          p.reg_spc,
          p.ret_date,
          p.def_date,
          p.responded,
          p.date_excus,
          p.exc_code,
          p.acc_exc,
          p.date_disq,
          p.disq_code,
          p.mileage,
          p.location,
          p.user_edtq,
          p.status,
          p.notes,
          p.no_attendances,
          p.is_active,
          p.no_def_pos,
          p.no_attended,
          p.no_fta,
          p.no_awol,
          p.pool_seq,
          p.edit_tag,
          p.pool_type,
          p.loc_code,
          p.next_date,
          p.on_call,
          p.perm_disqual,
          p.pay_county_emp,
          p.pay_expenses,
          p.spec_need,
          p.spec_need_msg,
          p.smart_card,
          p.amt_spent,
          p.completion_flag,
          p.completion_date,
          p.sort_code,
          p.bank_acct_name,
          p.bank_acct_no,
          p.bldg_soc_roll_no,
          p.was_deferred,
          p.id_checked,
          p.postpone,
          p.welsh,
          p.paid_cash,
          p.travel_time,
          p.scan_code,
          p.financial_loss,
          p.police_check,
          p.last_update,
          p.read_only,
          p.summons_file,
          p.reminder_sent,
          p.phoenix_date,
          p.phoenix_checked,
          p.m_phone,
          p.h_email,
          p.contact_preference,
          p.notifications,
          p.service_comp_comms_status
   FROM juror.pool p
   WHERE p.owner::text <> '400'::text AND p.is_active::text = 'Y'::text AND (TRIM(BOTH FROM p.h_email) IS NOT NULL OR p.m_phone IS NOT NULL);


-- juror_digital_user.pool_history source

CREATE OR REPLACE VIEW juror_digital_user.pool_history
AS SELECT ph.owner,
          ph.pool_no,
          ph.date_part,
          ph.history_code,
          ph.user_id,
          ph.other_information
   FROM juror.pool_hist ph;


-- juror_digital_user.pool_member source

CREATE OR REPLACE VIEW juror_digital_user.pool_member
AS SELECT p.owner,
          p.part_no,
          p.pool_no,
          p.poll_number,
          p.title,
          p.lname,
          p.fname,
          p.dob,
          p.address,
          p.address2,
          p.address3,
          p.address4,
          p.address5,
          p.address6,
          p.zip,
          p.h_phone,
          p.w_phone,
          p.w_ph_local,
          p.times_sel,
          p.trial_no,
          p.juror_no,
          p.reg_spc,
          p.ret_date,
          p.def_date,
          p.responded,
          p.date_excus,
          p.exc_code,
          p.acc_exc,
          p.date_disq,
          p.disq_code,
          p.mileage,
          p.location,
          p.user_edtq,
          p.status,
          p.notes,
          p.no_attendances,
          p.is_active,
          p.no_def_pos,
          p.no_attended,
          p.no_fta,
          p.no_awol,
          p.pool_seq,
          p.edit_tag,
          p.pool_type,
          p.loc_code,
          p.next_date,
          p.on_call,
          p.perm_disqual,
          p.pay_county_emp,
          p.pay_expenses,
          p.spec_need,
          p.spec_need_msg,
          p.smart_card,
          p.amt_spent,
          p.completion_flag,
          p.completion_date,
          p.sort_code,
          p.bank_acct_name,
          p.bank_acct_no,
          p.bldg_soc_roll_no,
          p.was_deferred,
          p.id_checked,
          p.postpone,
          p.welsh,
          p.paid_cash,
          p.travel_time,
          p.scan_code,
          p.financial_loss,
          p.police_check,
          p.last_update,
          p.read_only,
          p.summons_file,
          p.reminder_sent,
          p.phoenix_date,
          p.phoenix_checked,
          p.m_phone,
          p.h_email,
          p.contact_preference,
          p.notifications,
          p.service_comp_comms_status,
          p.transfer_date
   FROM juror.pool p;


-- juror_digital_user.pool_request source

CREATE OR REPLACE VIEW juror_digital_user.pool_request
AS SELECT u.owner,
          u.pool_no,
          u.attend_time,
          u.last_update,
          u.loc_code,
          u.new_request,
          u.next_date,
          u.return_date,
          u.pool_type,
          u.no_requested,
          u.pool_total,
          u.deferrals_used,
          u.reg_spc,
          u.read_only,
          u.additional_summons
   FROM juror.unique_pool u;


-- juror_digital_user.pool_stats source

CREATE OR REPLACE VIEW juror_digital_user.pool_stats
AS SELECT pm.pool_no,
          sum(
                  CASE
                      WHEN pm.owner::text = '400'::text THEN 1
                      ELSE 0
                      END) AS total_summoned,
          sum(
                  CASE
                      WHEN pm.owner::text <> '400'::text AND pm.is_active::text = 'Y'::text THEN 1
                      ELSE 0
                      END) AS court_supply,
          sum(
                  CASE
                      WHEN pm.owner::text = '400'::text AND pm.status = 2::numeric THEN 1
                      ELSE 0
                      END) AS available,
          sum(
                  CASE
                      WHEN pm.owner::text = '400'::text AND (pm.status <> ALL (ARRAY[1::numeric, 2::numeric, 11::numeric])) THEN 1
                      ELSE 0
                      END) AS unavailable,
          sum(
                  CASE
                      WHEN pm.owner::text = '400'::text AND (pm.status = ANY (ARRAY[1::numeric, 11::numeric])) THEN 1
                      ELSE 0
                      END) AS unresolved
   FROM juror_digital_user.pool_member pm
   WHERE pm.summons_file IS NULL OR pm.summons_file::text <> 'Disq. on selection'::text
   GROUP BY pm.pool_no;


-- juror_digital_user.pool_status source

CREATE OR REPLACE VIEW juror_digital_user.pool_status
AS SELECT p.status,
          p.status_desc,
          p.active
   FROM juror.pool_status p;


-- juror_digital_user.pool_type source

CREATE OR REPLACE VIEW juror_digital_user.pool_type
AS SELECT pt.pool_type,
          pt.pool_type_desc
   FROM juror.pool_type pt;


-- juror_digital_user.postpone_letter source

CREATE OR REPLACE VIEW juror_digital_user.postpone_letter
AS SELECT pl.owner,
          pl.part_no,
          pl.date_postpone,
          pl.printed,
          pl.date_printed
   FROM juror.postpone_lett pl;


-- juror_digital_user.print_files source

CREATE OR REPLACE VIEW juror_digital_user.print_files
AS SELECT p.printfile_name,
          p.creation_date,
          p.form_type,
          p.detail_rec,
          p.extracted_flag,
          p.part_no,
          p.digital_comms
   FROM juror.print_files p;


-- juror_digital_user.print_files_notify_comms source

CREATE OR REPLACE VIEW juror_digital_user.print_files_notify_comms
AS SELECT p.printfile_name,
          p.creation_date,
          p.form_type,
          p.detail_rec,
          p.extracted_flag,
          p.part_no AS juror_number,
          p.digital_comms,
          n.template_id,
          n.template_name,
          n.notify_name,
          j.h_email
   FROM juror.print_files p,
        juror_digital.notify_template_mapping n,
        juror.pool j
   WHERE p.form_type::text = n.form_type::text AND p.part_no::text = j.part_no::text AND p.creation_date > date_trunc('day'::text, statement_timestamp() - make_interval(days => COALESCE(( SELECT app_settings.value
                                                                                                                                                                                            FROM juror_digital.app_settings
                                                                                                                                                                                            WHERE app_settings.setting::text = 'PRINT_FILES_BACKFILL_DAYS'::text), '3'::character varying)::integer)) AND p.digital_comms::text = 'N'::text AND n.notification_type = 1 AND j.owner::text = '400'::text AND j.is_active::text = 'Y'::text AND TRIM(BOTH FROM j.h_email) IS NOT NULL;


-- juror_digital_user.request_lett source

CREATE OR REPLACE VIEW juror_digital_user.request_lett
AS SELECT request_lett.owner,
          request_lett.part_no,
          request_lett.req_info,
          request_lett.printed,
          request_lett.date_printed
   FROM juror.request_lett;


-- juror_digital_user.stats_not_responded_totals source

CREATE OR REPLACE VIEW juror_digital_user.stats_not_responded_totals
AS SELECT sum(COALESCE(s.non_responsed_count, 0)) AS not_responsed_total
   FROM juror_digital.stats_not_responded s;


-- juror_digital_user.stats_response_times_totals source

CREATE OR REPLACE VIEW juror_digital_user.stats_response_times_totals
AS SELECT sum(COALESCE(s.response_count, 0)) AS all_responses_total,
          sum(
                  CASE
                      WHEN s.response_method::text = 'Online'::text THEN COALESCE(s.response_count, 0)
                      ELSE 0
                      END) AS online_responses_total
   FROM juror_digital.stats_response_times s;


-- juror_digital_user.system_parameter source

CREATE OR REPLACE VIEW juror_digital_user.system_parameter
AS SELECT sp.sp_id,
          sp.sp_desc,
          sp.sp_value,
          sp.created_by,
          sp.created_date,
          sp.updated_by,
          sp.updated_date
   FROM juror.system_parameter sp;


-- juror_digital_user.t_special source

CREATE OR REPLACE VIEW juror_digital_user.t_special
AS SELECT t_special.spec_need,
          t_special.description
   FROM juror.t_special;


-- juror_digital_user.unique_pool source

CREATE OR REPLACE VIEW juror_digital_user.unique_pool
AS SELECT u.additional_summons,
          u.attend_time,
          u.last_update,
          u.loc_code,
          u.new_request,
          u.next_date,
          u.pool_no,
          u.return_date
   FROM juror.unique_pool u
   WHERE u.owner::text = '400'::text;


-- juror_digital_user.welsh_court_location source

CREATE OR REPLACE VIEW juror_digital_user.welsh_court_location
AS SELECT c.loc_code,
          c.loc_name,
          c.loc_address1,
          c.loc_address2,
          c.loc_address3,
          c.loc_address4,
          c.loc_address5,
          c.loc_address6,
          c.location_address
   FROM juror.welsh_location c;


CREATE OR REPLACE VIEW juror_digital_user.active_pools_bureau
AS SELECT pr.pool_no,
          pr.no_requested AS jurors_requested,
          CASE
              WHEN ps.available IS NULL THEN 0::bigint
              ELSE ps.available
              END AS confirmed_jurors,
          cl.loc_name AS court_name,
          pt.pool_type_desc AS pool_type,
          pr.return_date AS service_start_date
   FROM juror_digital_user.pool_request pr
            LEFT JOIN juror_digital_user.pool_stats ps ON pr.pool_no::text = ps.pool_no::text
            JOIN juror_digital_user.court_location cl ON pr.loc_code::text = cl.loc_code::text
            JOIN juror_digital_user.pool_type pt ON pr.pool_type::text = pt.pool_type::text
   WHERE pr.owner::text = '400'::text AND pr.new_request::text = 'N'::text AND pr.read_only::text = 'N'::text AND pr.no_requested <> 0::numeric;

-- DROP SCHEMA hk;

CREATE SCHEMA IF NOT EXISTS hk;
-- hk.hk_owner_restrict definition

-- Drop table

-- DROP TABLE hk.hk_owner_restrict;

CREATE TABLE hk.hk_owner_restrict (
                                      id numeric(38) NOT NULL,
                                      description varchar(60) NOT NULL,
                                      value varchar(3) NOT NULL,
                                      last_updated timestamp(0) NOT NULL
);


-- hk.hk_params definition

-- Drop table

-- DROP TABLE hk.hk_params;

CREATE TABLE hk.hk_params (
                              "key" numeric(38) NOT NULL,
                              value varchar(20) NULL,
                              description varchar(60) NULL,
                              last_updated timestamp(0) NOT NULL
);


-- hk.coroner_pool source

CREATE OR REPLACE VIEW hk.coroner_pool
AS SELECT coroner_pool.cor_pool_no,
          coroner_pool.cor_name,
          coroner_pool.cor_court_loc,
          coroner_pool.cor_request_dt,
          coroner_pool.cor_service_dt,
          coroner_pool.cor_no_requested
   FROM juror.coroner_pool;


-- hk.coroner_pool_detail source

CREATE OR REPLACE VIEW hk.coroner_pool_detail
AS SELECT coroner_pool_detail.cor_pool_no,
          coroner_pool_detail.part_no,
          coroner_pool_detail.title,
          coroner_pool_detail.fname,
          coroner_pool_detail.lname,
          coroner_pool_detail.address1,
          coroner_pool_detail.address2,
          coroner_pool_detail.address3,
          coroner_pool_detail.address4,
          coroner_pool_detail.address5,
          coroner_pool_detail.address6,
          coroner_pool_detail.postcode
   FROM juror.coroner_pool_detail;


-- hk.error_log source

CREATE OR REPLACE VIEW hk.error_log
AS SELECT error_log.time_stamp,
          error_log.job,
          error_log.error_info
   FROM juror.error_log;


-- hk.hk_run_log source

CREATE OR REPLACE VIEW hk.hk_run_log
AS SELECT hk_run_log.seq_id,
          hk_run_log.start_time,
          hk_run_log.end_time,
          hk_run_log.jurors_deleted,
          hk_run_log.jurors_error,
          hk_run_log.return_code
   FROM juror.hk_run_log;


-- hk.holidays source

CREATE OR REPLACE VIEW hk.holidays
AS SELECT holidays.owner,
          holidays.holiday,
          holidays.description
   FROM juror.holidays;


-- hk.pl_xml_status source

CREATE OR REPLACE VIEW hk.pl_xml_status
AS SELECT pl_xml_status.sequence_no,
          pl_xml_status.circuit_name,
          pl_xml_status.shell_circuit_id,
          pl_xml_status.date_time_stamp,
          pl_xml_status.status
   FROM juror.pl_xml_status;



-- DROP PROCEDURE hk.juror_digital_housekeeping_del_juror_resp_cjs_employment(text, timestamp);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_del_juror_resp_cjs_employment(IN juror_no text, IN completed_date timestamp without time zone)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    ora2pg_rowcount int;
    lc_table_deletions text;
    delete_failed text := 'failed delete: ';
    lb_failed bool;
BEGIN

    DELETE FROM JUROR_DIGITAL.juror_response_cjs_employment
    WHERE juror_number = juror_no;

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


    lc_table_deletions := lc_table_deletions||' TABLE juror_response_cjs_employment :'|| ora2pg_rowcount::varchar||', ';

EXCEPTION
    when others then
        BEGIN
            lc_table_deletions := lc_table_deletions||' TABLE juror_response_cjs_employment failed :'||SQLERRM::varchar||', ';
            ROLLBACK;
            call juror_digital_housekeeping_write_audit(juror_no, completed_date, delete_failed||lc_table_deletions);
            lb_failed := TRUE;
        END;
END;

$procedure$
;

-- DROP PROCEDURE hk.juror_digital_housekeeping_del_staff_juror_response_audit(text, timestamp);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_del_staff_juror_response_audit(IN juror_no text, IN completed_date timestamp without time zone)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    ora2pg_rowcount int;
    lc_table_deletions text;
    delete_failed text := 'failed delete: ';
    lb_failed bool;
BEGIN

    DELETE FROM JUROR_DIGITAL.staff_juror_response_audit
    WHERE juror_number = juror_no;

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


    lc_table_deletions := lc_table_deletions||' TABLE staff_juror_response_audit :'|| ora2pg_rowcount::varchar||', ';

EXCEPTION
    when others then
        BEGIN
            lc_table_deletions := lc_table_deletions||' TABLE staff_juror_response_audit failed :'||SQLERRM::varchar||', ';
            ROLLBACK;
            call juror_digital_housekeeping_write_audit(juror_no, completed_date, delete_failed||lc_table_deletions);
            lb_failed := TRUE;
        END;
END;

$procedure$
;

-- DROP PROCEDURE hk.juror_digital_housekeeping_delete_change_log(text, timestamp);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_delete_change_log(IN juror_no text, IN completed_date timestamp without time zone)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    ora2pg_rowcount int;
    lc_table_deletions text;
    delete_failed text := 'failed delete: ';
    lb_failed bool;
BEGIN

    DELETE FROM JUROR_DIGITAL.change_log_view
    WHERE juror_number = juror_no;

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


    lc_table_deletions := lc_table_deletions||' TABLE change_log :'|| ora2pg_rowcount::varchar||', ';

EXCEPTION
    when others then
        BEGIN
            lc_table_deletions := lc_table_deletions||' TABLE change_log failed :'||SQLERRM::varchar||', ';
            ROLLBACK;
            call juror_digital_housekeeping_write_audit(juror_no, completed_date, delete_failed||lc_table_deletions);
            lb_failed := TRUE;
        END;
END;

$procedure$
;

-- DROP PROCEDURE hk.juror_digital_housekeeping_delete_change_log_item(text, timestamp);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_delete_change_log_item(IN juror_no text, IN completed_date timestamp without time zone)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    ora2pg_rowcount int;
    lc_table_deletions text;
    delete_failed text := 'failed delete: ';
    lb_failed bool;
BEGIN

    DELETE FROM juror_digital.change_log_item
    WHERE change_log in (
        SELECT i.change_log FROM juror_digital.change_log_item i
                                     INNER JOIN juror_digital.change_log_view c
                                                ON i.change_log = c.id
                                                    AND c.juror_number = juror_no
    );

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


    lc_table_deletions := lc_table_deletions||' TABLE change_log_item :'|| ora2pg_rowcount::varchar||', ';

EXCEPTION
    when others then
        BEGIN
            lc_table_deletions := lc_table_deletions||' TABLE change_log_item failed :'||SQLERRM::varchar||', ';
            ROLLBACK;
            call juror_digital_housekeeping_write_audit(juror_no, completed_date, delete_failed||lc_table_deletions);
            lb_failed := TRUE;
        END;
END;

$procedure$
;

-- DROP PROCEDURE hk.juror_digital_housekeeping_delete_juror_response(text, timestamp);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_delete_juror_response(IN juror_no text, IN completed_date timestamp without time zone)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    ora2pg_rowcount int;
    lc_table_deletions text;
    delete_failed text := 'failed delete: ';
    lb_failed bool;
BEGIN
    DELETE FROM JUROR_DIGITAL.juror_response
    WHERE juror_number = juror_no;

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


    lc_table_deletions := lc_table_deletions||' TABLE juror_response :'|| ora2pg_rowcount::varchar||', ';

EXCEPTION
    when others then
        lc_table_deletions := lc_table_deletions||' TABLE juror_response failed :'||SQLERRM::varchar||', ';
        ROLLBACK;
        call juror_digital_housekeeping_write_audit(juror_no, completed_date,lc_table_deletions);
        lb_failed := TRUE;
END;

$procedure$
;

-- DROP PROCEDURE hk.juror_digital_housekeeping_delete_juror_responses_aud(text, timestamp);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_delete_juror_responses_aud(IN juror_no text, IN completed_date timestamp without time zone)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    ora2pg_rowcount int;
    lc_table_deletions text;
    delete_failed text := 'failed delete: ';
    lb_failed bool;
BEGIN

    DELETE FROM JUROR_DIGITAL.juror_response_aud
    WHERE juror_number = juror_no;

    GET DIAGNOSTICS ora2pg_rowcount = ROW_COUNT;


    lc_table_deletions := lc_table_deletions||' TABLE juror_response_aud :'|| ora2pg_rowcount::varchar||', ';

EXCEPTION
    when others then
        BEGIN
            lc_table_deletions := lc_table_deletions||' TABLE juror_response_aud failed :'||SQLERRM::varchar||', ';
            ROLLBACK;
            call juror_digital_housekeeping_write_audit(juror_no, completed_date, delete_failed||lc_table_deletions);
            lb_failed := TRUE;
        END;
END;

$procedure$
;

-- DROP PROCEDURE hk.juror_digital_housekeeping_perform_deletions(int8);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_perform_deletions(IN p_retention_threshold bigint)
    LANGUAGE plpgsql
AS $procedure$
DECLARE
    li_responses_count int;
    li_deleted_count int;
    lc_table_deletions text;
    lb_failed bool;

    c_jd_responses CURSOR(c_retention bigint) FOR
        SELECT
            r.juror_number,
            r.date_received,
            r.processing_status,
            r.processing_complete,
            r.completed_at
        FROM JUROR_DIGITAL.juror_response r
        WHERE r.completed_at < date_trunc('day', clock_timestamp()) + -12*c_retention*'1 month'::interval;


BEGIN

    li_responses_count := 0;
    li_deleted_count := 0;

    IF (p_retention_threshold < 1 OR p_retention_threshold > 999) THEN
        lc_table_deletions := 'retention period should be between 1 and 999.';
        RAISE EXCEPTION '%', lc_table_deletions USING ERRCODE = '45100';
    END IF;

    FOR juror_rec IN c_jd_responses(p_retention_threshold) LOOP
            lb_failed := FALSE;
            lc_table_deletions := ''|| juror_rec.juror_number||' - ';
            li_responses_count := li_responses_count +1;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_delete_juror_responses_aud(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_del_juror_resp_cjs_employment(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_del_juror_resp_special_needs(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_delete_change_log_item(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_delete_change_log(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_del_staff_juror_response_audit(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_delete_juror_response(juror_rec.juror_number, juror_rec.completed_at);
            END IF;

            IF NOT lb_failed THEN
                call juror_digital_housekeeping_write_audit(juror_rec.juror_number, juror_rec.completed_at,lc_table_deletions);
                COMMIT;
                li_deleted_count := li_deleted_count + 1;
            END IF;
            NULL;
        END LOOP;

    -- insert summary
    call juror_digital_housekeeping_write_audit(NULL, NULL,'responses : '||li_responses_count||' deleted : '||li_deleted_count);

EXCEPTION
    WHEN OTHERS THEN
        call juror_digital_housekeeping_write_audit(NULL, NULL,lc_table_deletions);
END;

$procedure$
;



-- DROP PROCEDURE hk.juror_digital_housekeeping_write_audit_atx(text, timestamp, text);

CREATE OR REPLACE PROCEDURE hk.juror_digital_housekeeping_write_audit_atx(IN p_juror_no text, IN p_completed_date timestamp without time zone, IN p_table_deletions text)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $procedure$
BEGIN
    INSERT INTO JUROR_DIGITAL.JD_HOUSEKEEPING_AUDIT(juror_number, selected_date, deletion_date, deletion_summary)
    VALUES (p_juror_no, p_completed_date, clock_timestamp(), p_table_deletions);

END;

$procedure$
;
-- DROP SCHEMA juror_mod;

CREATE SCHEMA IF NOT EXISTS juror_mod;

-- DROP SEQUENCE juror_mod.appearance_f_audit_seq;

CREATE SEQUENCE juror_mod.appearance_f_audit_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE juror_mod.bulk_print_data_seq;

CREATE SEQUENCE juror_mod.bulk_print_data_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

-- DROP SEQUENCE juror_mod.content_store_seq;

CREATE SEQUENCE juror_mod.content_store_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

-- DROP SEQUENCE juror_mod.payment_data_invoice_number_seq;

CREATE SEQUENCE juror_mod.payment_data_invoice_number_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror_mod.payment_data_unique_id_seq;

CREATE SEQUENCE juror_mod.payment_data_unique_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999999
    START 1
    CACHE 1
    CYCLE;
-- DROP SEQUENCE juror_mod.payment_file_count;

CREATE SEQUENCE juror_mod.payment_file_count
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999
    START 1
    CACHE 1
    NO CYCLE;
-- Drop table

-- DROP TABLE juror_mod.accused;

CREATE TABLE juror_mod.accused (
                                   "owner" varchar(3) NOT NULL,
                                   trial_no varchar(16) NOT NULL,
                                   lname varchar(20) NOT NULL,
                                   fname varchar(20) NOT NULL,
                                   CONSTRAINT accused_pkey PRIMARY KEY (owner, trial_no, lname, fname)
);
CREATE INDEX accused_trial_idx ON juror_mod.accused USING btree (owner, trial_no);


-- juror_mod.app_settings definition

-- Drop table

-- DROP TABLE juror_mod.app_settings;

CREATE TABLE juror_mod.app_settings (
                                        setting varchar(80) NOT NULL,
                                        value varchar(200) NULL,
                                        CONSTRAINT app_settings_pkey PRIMARY KEY (setting)
);


-- juror_mod.content_store definition

-- Drop table

-- DROP TABLE juror_mod.content_store;

CREATE TABLE juror_mod.content_store (
                                         request_id int8 NULL,
                                         document_id varchar(50) NOT NULL,
                                         date_on_q_for_send timestamp(0) NULL DEFAULT statement_timestamp(),
                                         file_type varchar(10) NOT NULL,
                                         date_sent timestamp(0) NULL,
                                         "data" text NULL
);


-- juror_mod.court_location definition

-- Drop table

-- DROP TABLE juror_mod.court_location;

CREATE TABLE juror_mod.court_location (
                                          "owner" varchar(3) NULL,
                                          loc_code varchar(3) NOT NULL,
                                          loc_name varchar(40) NULL,
                                          loc_court_name varchar(30) NULL,
                                          loc_attend_time varchar(10) NULL,
                                          loc_address1 varchar(35) NULL,
                                          loc_address2 varchar(35) NULL,
                                          loc_address3 varchar(35) NULL,
                                          loc_address4 varchar(35) NULL,
                                          loc_address5 varchar(35) NULL,
                                          loc_address6 varchar(35) NULL,
                                          loc_zip varchar(10) NULL,
                                          loc_phone varchar(12) NULL,
                                          jury_officer_phone varchar(100) NULL,
                                          location_address varchar(230) NULL,
                                          region_id varchar(5) NULL,
                                          yield float8 NULL,
                                          voters_lock int2 NULL,
                                          term_of_service varchar(20) NULL,
                                          tdd_phone varchar(12) NULL,
                                          loc_signature varchar(30) NULL,
                                          rate_per_mile_car_0_passengers numeric(8, 5) NULL,
                                          rate_per_mile_car_1_passengers numeric(8, 5) NULL,
                                          rate_per_mile_car_2_or_more_passengers numeric(8, 5) NULL,
                                          rate_per_mile_motorcycle_0_passengers numeric(8, 5) NULL,
                                          rate_per_mile_motorcycle_1_or_more_passengers numeric(8, 5) NULL,
                                          rate_per_mile_bike numeric(8, 2) NULL,
                                          limit_financial_loss_half_day numeric(8, 5) NULL,
                                          limit_financial_loss_full_day numeric(8, 5) NULL,
                                          limit_financial_loss_half_day_long_trial numeric(8, 5) NULL,
                                          limit_financial_loss_full_day_long_trial numeric(8, 5) NULL,
                                          public_transport_soft_limit numeric(8, 5) NULL,
                                          rate_substance_standard numeric(8, 5) NULL,
                                          rate_substance_long_day numeric(8, 5) NULL,
                                          rates_effective_from date NULL,
                                          CONSTRAINT court_location_pkey PRIMARY KEY (loc_code)
);


-- juror_mod.court_region definition

-- Drop table

-- DROP TABLE juror_mod.court_region;

CREATE TABLE juror_mod.court_region (
                                        region_id varchar(5) NOT NULL,
                                        region_name varchar(30) NULL,
                                        notify_account_key varchar(100) NULL,
                                        CONSTRAINT court_region_pkey PRIMARY KEY (region_id),
                                        CONSTRAINT court_region_region_name_key UNIQUE (region_name)
);


-- juror_mod.courtroom definition

-- Drop table

-- DROP TABLE juror_mod.courtroom;

CREATE TABLE juror_mod.courtroom (
                                     id bigserial NOT NULL,
                                     "owner" varchar(3) NOT NULL,
                                     room_number varchar(6) NOT NULL,
                                     description varchar(30) NOT NULL,
                                     CONSTRAINT courtroom_pk PRIMARY KEY (id)
);


-- juror_mod.export_placeholders definition

-- Drop table

-- DROP TABLE juror_mod.export_placeholders;

CREATE TABLE juror_mod.export_placeholders (
                                               placeholder_name varchar(48) NOT NULL,
                                               source_table_name varchar(48) NOT NULL,
                                               source_column_name varchar(48) NOT NULL,
                                               "type" varchar(12) NOT NULL,
                                               description varchar(100) NULL,
                                               default_value varchar(200) NULL,
                                               editable varchar(1) NULL,
                                               validation_rule varchar(600) NULL,
                                               validation_message varchar(200) NULL,
                                               validation_format varchar(60) NULL
);


-- juror_mod.judge definition

-- Drop table

-- DROP TABLE juror_mod.judge;

CREATE TABLE juror_mod.judge (
                                 id bigserial NOT NULL,
                                 "owner" varchar(3) NOT NULL,
                                 code varchar(4) NOT NULL,
                                 description varchar(30) NOT NULL,
                                 telephone_number varchar(16) NULL,
                                 CONSTRAINT judge_pk PRIMARY KEY (id)
);


-- juror_mod.juror_trial definition

-- Drop table

-- DROP TABLE juror_mod.juror_trial;

CREATE TABLE juror_mod.juror_trial (
                                       loc_code varchar(3) NULL,
                                       juror_number varchar(9) NOT NULL,
                                       trial_number varchar(16) NULL,
                                       pool_number varchar(9) NOT NULL,
                                       rand_number int8 NULL,
                                       date_selected timestamp NOT NULL,
                                       "result" varchar(2) NULL,
                                       completed bool NULL
);


-- juror_mod.message definition

-- Drop table

-- DROP TABLE juror_mod.message;

CREATE TABLE juror_mod.message (
                                   juror_number varchar(9) NOT NULL,
                                   file_datetime date NOT NULL,
                                   username varchar(20) NOT NULL,
                                   loc_code varchar(3) NOT NULL,
                                   phone varchar(15) NULL,
                                   email varchar(254) NULL,
                                   pool_no varchar(9) NULL,
                                   subject varchar(50) NULL,
                                   message_text varchar(2000) NULL,
                                   message_id int8 NOT NULL,
                                   message_read varchar(2) NULL DEFAULT 'NR'::character varying,
                                   CONSTRAINT message_pkey PRIMARY KEY (juror_number, file_datetime, username, loc_code)
);


-- juror_mod.message_placeholders definition

-- Drop table

-- DROP TABLE juror_mod.message_placeholders;

CREATE TABLE juror_mod.message_placeholders (
                                                placeholder_name varchar(48) NOT NULL,
                                                source_table_name varchar(48) NULL,
                                                source_column_name varchar(48) NULL,
                                                display_name varchar(32) NOT NULL,
                                                data_type varchar(12) NOT NULL,
                                                description varchar(100) NULL,
                                                editable bool NOT NULL,
                                                validation_regex varchar(600) NULL,
                                                validation_message varchar(200) NULL,
                                                CONSTRAINT data_type_val CHECK (((data_type)::text = ANY ((ARRAY['NONE'::character varying, 'STRING'::character varying, 'DATE'::character varying, 'TIME'::character varying])::text[]))),
                                                CONSTRAINT message_placeholders_pkey PRIMARY KEY (placeholder_name)
);


-- juror_mod.notify_template_field definition

-- Drop table

-- DROP TABLE juror_mod.notify_template_field;

CREATE TABLE juror_mod.notify_template_field (
                                                 id int4 NULL,
                                                 template_id varchar(50) NULL,
                                                 template_field varchar(40) NULL,
                                                 database_field varchar(80) NULL,
                                                 position_from int2 NULL,
                                                 position_to int2 NULL,
                                                 field_length int2 NULL,
                                                 convert_to_date bool NULL,
                                                 jd_class_name varchar(60) NULL,
                                                 jd_class_property varchar(60) NULL,
                                                 "version" int8 NULL
);


-- juror_mod.password_export_placeholders definition

-- Drop table

-- DROP TABLE juror_mod.password_export_placeholders;

CREATE TABLE juror_mod.password_export_placeholders (
                                                        "owner" varchar(3) NOT NULL,
                                                        login varchar(20) NOT NULL,
                                                        placeholder_name varchar(48) NOT NULL,
                                                        use varchar(1) NULL
);


-- juror_mod.payment_data definition

-- Drop table

-- DROP TABLE juror_mod.payment_data;

CREATE TABLE juror_mod.payment_data (
                                        loc_code varchar(3) NOT NULL,
                                        unique_id varchar(7) NOT NULL DEFAULT nextval('juror_mod.payment_data_unique_id_seq'::regclass),
                                        creation_date timestamp(0) NOT NULL DEFAULT statement_timestamp(),
                                        expense_total numeric(8, 2) NOT NULL,
                                        juror_number varchar(9) NOT NULL,
                                        invoice_id varchar(7) NOT NULL DEFAULT nextval('juror_mod.payment_data_invoice_number_seq'::regclass),
                                        bank_sort_code varchar(6) NULL,
                                        bank_ac_name varchar(18) NULL,
                                        bank_ac_number varchar(8) NULL,
                                        build_soc_number varchar(18) NULL,
                                        address_line_1 varchar(35) NULL,
                                        address_line_2 varchar(35) NULL,
                                        address_line_3 varchar(35) NULL,
                                        address_line_4 varchar(35) NULL,
                                        address_line_5 varchar(35) NULL,
                                        postcode varchar(10) NULL,
                                        auth_code varchar(9) NOT NULL,
                                        juror_name varchar(50) NOT NULL,
                                        loc_cost_centre varchar(5) NOT NULL,
                                        travel_total numeric(8, 2) NULL,
                                        subsistence_total numeric(8, 2) NULL,
                                        financial_loss_total numeric(8, 2) NULL,
                                        expense_file_name varchar(30) NULL,
                                        extracted bool NOT NULL DEFAULT false,
                                        CONSTRAINT payment_data_pkey PRIMARY KEY (loc_code, unique_id)
);
CREATE INDEX payment_data_extracted_idx ON juror_mod.payment_data USING btree (extracted);


-- juror_mod.pool_transfer_weekday definition

-- Drop table

-- DROP TABLE juror_mod.pool_transfer_weekday;

CREATE TABLE juror_mod.pool_transfer_weekday (
                                                 transfer_day varchar(3) NULL,
                                                 run_day varchar(3) NULL,
                                                 adjustment int2 NULL
);


-- juror_mod.region_notify_template definition

-- Drop table

-- DROP TABLE juror_mod.region_notify_template;

CREATE TABLE juror_mod.region_notify_template (
                                                  region_template_id int2 NOT NULL,
                                                  template_name varchar(100) NULL,
                                                  region_id varchar(5) NULL,
                                                  triggered_template_id varchar(100) NULL,
                                                  legacy_template_id int8 NULL,
                                                  notify_template_id varchar(100) NULL,
                                                  message_format varchar(10) NULL,
                                                  welsh_language varchar(1) NULL,
                                                  CONSTRAINT region_notify_template_pkey PRIMARY KEY (region_template_id)
);


-- juror_mod.rev_info definition

-- Drop table

-- DROP TABLE juror_mod.rev_info;

CREATE TABLE juror_mod.rev_info (
                                    revision_number int8 NOT NULL,
                                    revision_timestamp int8 NULL,
                                    CONSTRAINT rev_info_pkey PRIMARY KEY (revision_number)
);

-- juror_mod.system_parameter definition

-- Drop table

-- DROP TABLE juror_mod.system_parameter;

CREATE TABLE juror_mod.system_parameter (
                                            sp_id int8 NOT NULL,
                                            sp_desc varchar(80) NULL,
                                            sp_value varchar(200) NULL,
                                            created_by varchar(20) NULL,
                                            created_date timestamp(0) NULL,
                                            updated_by varchar(20) NULL,
                                            updated_date timestamp(0) NULL,
                                            CONSTRAINT system_parameter_pkey PRIMARY KEY (sp_id)
);


-- juror_mod.t_contact definition

-- Drop table

-- DROP TABLE juror_mod.t_contact;

CREATE TABLE juror_mod.t_contact (
                                     enquiry_code varchar(2) NOT NULL,
                                     description varchar(60) NULL,
                                     CONSTRAINT t_contact_pkey PRIMARY KEY (enquiry_code)
);


-- juror_mod.t_disq_code definition

-- Drop table

-- DROP TABLE juror_mod.t_disq_code;

CREATE TABLE juror_mod.t_disq_code (
                                       disq_code varchar(1) NOT NULL,
                                       description varchar(60) NULL,
                                       enabled bool NULL,
                                       CONSTRAINT t_disq_code_pkey PRIMARY KEY (disq_code)
);


-- juror_mod.t_exc_code definition

-- Drop table

-- DROP TABLE juror_mod.t_exc_code;

CREATE TABLE juror_mod.t_exc_code (
                                      exc_code varchar(2) NOT NULL,
                                      description varchar(60) NULL,
                                      by_right bool NULL,
                                      enabled bool NULL,
                                      for_excusal bool NULL DEFAULT false,
                                      for_deferral bool NULL DEFAULT false,
                                      CONSTRAINT t_exc_code_pkey PRIMARY KEY (exc_code)
);


-- juror_mod.t_form_attr definition

-- Drop table

-- DROP TABLE juror_mod.t_form_attr;

CREATE TABLE juror_mod.t_form_attr (
                                       form_type varchar(6) NOT NULL,
                                       dir_name varchar(20) NULL,
                                       max_rec_len int8 NULL,
                                       CONSTRAINT t_form_attr_pkey PRIMARY KEY (form_type)
);


-- juror_mod.t_history_code definition

-- Drop table

-- DROP TABLE juror_mod.t_history_code;

CREATE TABLE juror_mod.t_history_code (
                                          history_code varchar(4) NOT NULL,
                                          description varchar(40) NULL,
                                          CONSTRAINT t_history_code_pkey PRIMARY KEY (history_code)
);


-- juror_mod.t_juror_status definition

-- Drop table

-- DROP TABLE juror_mod.t_juror_status;

CREATE TABLE juror_mod.t_juror_status (
                                          status int4 NOT NULL,
                                          status_desc varchar(30) NOT NULL,
                                          active bool NULL,
                                          CONSTRAINT pool_status_pkey PRIMARY KEY (status)
);


-- juror_mod.t_message_template definition

-- Drop table

-- DROP TABLE juror_mod.t_message_template;

CREATE TABLE juror_mod.t_message_template (
                                              id numeric(38) NOT NULL,
                                              "scope" varchar(6) NOT NULL,
                                              title varchar(27) NOT NULL,
                                              subject varchar(100) NOT NULL,
                                              "text" varchar(2000) NULL,
                                              display_order numeric(38) NULL,
                                              CONSTRAINT t_message_template_pkey PRIMARY KEY (id)
);


-- juror_mod.t_pending_juror_status definition

-- Drop table

-- DROP TABLE juror_mod.t_pending_juror_status;

CREATE TABLE juror_mod.t_pending_juror_status (
                                                  code varchar(1) NOT NULL,
                                                  description varchar(60) NOT NULL,
                                                  CONSTRAINT t_pending_juror_status_pkey PRIMARY KEY (code)
);


-- juror_mod.t_police definition

-- Drop table

-- DROP TABLE juror_mod.t_police;

CREATE TABLE juror_mod.t_police (
                                    code varchar(1) NOT NULL,
                                    description varchar(20) NULL,
                                    CONSTRAINT t_police_pkey PRIMARY KEY (code)
);


-- juror_mod.t_pool_type definition

-- Drop table

-- DROP TABLE juror_mod.t_pool_type;

CREATE TABLE juror_mod.t_pool_type (
                                       pool_type varchar(3) NOT NULL,
                                       pool_type_desc varchar(20) NULL,
                                       CONSTRAINT t_pool_type_pkey PRIMARY KEY (pool_type)
);


-- juror_mod.t_reasonable_adjustments definition

-- Drop table

-- DROP TABLE juror_mod.t_reasonable_adjustments;

CREATE TABLE juror_mod.t_reasonable_adjustments (
                                                    code varchar(1) NOT NULL,
                                                    description varchar(60) NULL,
                                                    CONSTRAINT t_reasonable_adjustments_pkey PRIMARY KEY (code)
);


-- juror_mod.t_reply_type definition

-- Drop table

-- DROP TABLE juror_mod.t_reply_type;

CREATE TABLE juror_mod.t_reply_type (
                                        "type" varchar(32) NOT NULL,
                                        description varchar(1000) NULL,
                                        CONSTRAINT t_reply_type_pkey PRIMARY KEY (type)
);


-- juror_mod.users definition

-- Drop table

-- DROP TABLE juror_mod.users;

CREATE TABLE juror_mod.users (
                                 "owner" varchar(3) NOT NULL,
                                 username varchar(20) NOT NULL,
                                 "name" varchar(50) NOT NULL,
                                 "level" int2 NOT NULL,
                                 active bool NOT NULL DEFAULT true,
                                 last_logged_in timestamp(6) NULL,
                                 "version" int8 NULL,
                                 team_id int8 NULL,
                                 "password" varchar(20) NULL,
                                 password_warning bool NULL,
                                 days_to_expire int8 NULL,
                                 password_changed_date timestamp(6) NULL,
                                 failed_login_attempts int2 NOT NULL DEFAULT 0,
                                 login_enabled_yn varchar(1) NULL DEFAULT 'Y'::character varying,
                                 CONSTRAINT users_pkey PRIMARY KEY (username)
);


-- juror_mod.voters definition

-- Drop table

-- DROP TABLE juror_mod.voters;

CREATE TABLE juror_mod.voters (
                                  loc_code varchar(3) NOT NULL,
                                  part_no varchar(9) NOT NULL,
                                  register_lett varchar(5) NULL,
                                  poll_number varchar(5) NULL,
                                  new_marker varchar(1) NULL,
                                  title varchar(10) NULL,
                                  lname varchar(20) NOT NULL,
                                  fname varchar(20) NOT NULL,
                                  dob date NULL,
                                  flags varchar(2) NULL,
                                  address varchar(35) NOT NULL,
                                  address2 varchar(35) NULL,
                                  address3 varchar(35) NULL,
                                  address4 varchar(35) NULL,
                                  address5 varchar(35) NULL,
                                  address6 varchar(35) NULL,
                                  zip varchar(10) NULL,
                                  date_selected1 date NULL,
                                  date_selected2 date NULL,
                                  date_selected3 date NULL,
                                  rec_num int4 NULL,
                                  perm_disqual varchar(1) NULL,
                                  source_id varchar(1) NULL,
                                  CONSTRAINT voters_pk PRIMARY KEY (loc_code, part_no)
);


-- juror_mod.coroner_pool definition

-- Drop table

-- DROP TABLE juror_mod.coroner_pool;

CREATE TABLE juror_mod.coroner_pool (
                                        cor_pool_no varchar(9) NOT NULL,
                                        cor_name varchar(35) NOT NULL,
                                        email varchar(254) NOT NULL,
                                        phone varchar(15) NULL,
                                        cor_court_loc varchar(3) NOT NULL,
                                        cor_request_dt date NOT NULL,
                                        cor_service_dt date NOT NULL,
                                        cor_no_requested int4 NOT NULL,
                                        CONSTRAINT cor_pool_pk PRIMARY KEY (cor_pool_no),
                                        CONSTRAINT coroner_pool_loc_code_fk FOREIGN KEY (cor_court_loc) REFERENCES juror_mod.court_location(loc_code)
);


-- juror_mod.coroner_pool_detail definition

-- Drop table

-- DROP TABLE juror_mod.coroner_pool_detail;

CREATE TABLE juror_mod.coroner_pool_detail (
                                               cor_pool_no varchar(9) NULL,
                                               juror_number varchar(9) NULL,
                                               title varchar(10) NULL,
                                               first_name varchar(20) NULL,
                                               last_name varchar(20) NULL,
                                               address_line_1 varchar(35) NULL,
                                               address_line_2 varchar(35) NULL,
                                               address_line_3 varchar(35) NULL,
                                               address_line_4 varchar(35) NULL,
                                               address_line_5 varchar(35) NULL,
                                               postcode varchar(10) NULL,
                                               CONSTRAINT coroner_pool_detail_pool_no_fk FOREIGN KEY (cor_pool_no) REFERENCES juror_mod.coroner_pool(cor_pool_no)
);


-- juror_mod.court_catchment_area definition

-- Drop table

-- DROP TABLE juror_mod.court_catchment_area;

CREATE TABLE juror_mod.court_catchment_area (
                                                postcode varchar(4) NOT NULL,
                                                loc_code varchar(3) NOT NULL,
                                                CONSTRAINT court_catchment_area_pkey PRIMARY KEY (postcode, loc_code),
                                                CONSTRAINT court_catchment_area_fk_loc_code FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code)
);


-- juror_mod.court_location_audit definition

-- Drop table

-- DROP TABLE juror_mod.court_location_audit;

CREATE TABLE juror_mod.court_location_audit (
                                                revision int8 NOT NULL,
                                                rev_type int4 NULL,
                                                loc_code varchar(3) NOT NULL,
                                                rates_effective_from date NULL,
                                                rate_per_mile_car_0_passengers numeric(8, 5) NULL,
                                                rate_per_mile_car_1_passengers numeric(8, 5) NULL,
                                                rate_per_mile_car_2_or_more_passengers numeric(8, 5) NULL,
                                                rate_per_mile_motorcycle_0_passengers numeric(8, 5) NULL,
                                                rate_per_mile_motorcycle_1_or_more_passengers numeric(8, 5) NULL,
                                                rate_per_mile_bike numeric(8, 5) NULL,
                                                limit_financial_loss_half_day numeric(8, 5) NULL,
                                                limit_financial_loss_full_day numeric(8, 5) NULL,
                                                limit_financial_loss_half_day_long_trial numeric(8, 5) NULL,
                                                limit_financial_loss_full_day_long_trial numeric(8, 5) NULL,
                                                rate_substance_standard numeric(8, 5) NULL,
                                                rate_substance_long_day numeric(8, 5) NULL,
                                                public_transport_soft_limit numeric(8, 5) NULL,
                                                CONSTRAINT court_location_audit_pkey PRIMARY KEY (revision, loc_code),
                                                CONSTRAINT fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info(revision_number)
);


-- juror_mod.financial_audit_details definition

-- Drop table

-- DROP TABLE juror_mod.financial_audit_details;

CREATE TABLE juror_mod.financial_audit_details (
                                                   id int8 NOT NULL,
                                                   submitted_on timestamp(6) NOT NULL,
                                                   submitted_by varchar(20) NOT NULL,
                                                   approved_on timestamp(6) NULL,
                                                   approved_by varchar(20) NULL,
                                                   juror_revision_when_approved int8 NULL,
                                                   CONSTRAINT financial_audit_details_pkey PRIMARY KEY (id),
                                                   CONSTRAINT financial_audit_details_fk_approved_by FOREIGN KEY (approved_by) REFERENCES juror_mod.users(username),
                                                   CONSTRAINT financial_audit_details_fk_revision_number FOREIGN KEY (juror_revision_when_approved) REFERENCES juror_mod.rev_info(revision_number),
                                                   CONSTRAINT financial_audit_details_fk_submitted_by FOREIGN KEY (submitted_by) REFERENCES juror_mod.users(username)
);


-- juror_mod.holiday definition

-- Drop table

-- DROP TABLE juror_mod.holiday;

CREATE TABLE juror_mod.holiday (
                                   id bigserial NOT NULL,
                                   "owner" varchar(3) NULL,
                                   holiday date NOT NULL,
                                   description varchar(30) NOT NULL,
                                   public bool NOT NULL,
                                   CONSTRAINT holiday_owner_holiday_key UNIQUE (owner, holiday),
                                   CONSTRAINT holiday_pkey PRIMARY KEY (id),
                                   CONSTRAINT holiday_loc_code_fk FOREIGN KEY ("owner") REFERENCES juror_mod.court_location(loc_code)
);
CREATE INDEX holiday_index ON juror_mod.holiday USING btree (holiday DESC);


-- juror_mod.juror definition

-- Drop table

-- DROP TABLE juror_mod.juror;

CREATE TABLE juror_mod.juror (
                                 juror_number varchar(9) NOT NULL,
                                 poll_number varchar(5) NULL,
                                 title varchar(10) NULL,
                                 last_name varchar(20) NOT NULL,
                                 first_name varchar(20) NOT NULL,
                                 dob timestamp(0) NULL,
                                 address_line_1 varchar(35) NOT NULL,
                                 address_line_2 varchar(35) NULL,
                                 address_line_3 varchar(35) NULL,
                                 address_line_4 varchar(35) NULL,
                                 address_line_5 varchar(35) NULL,
                                 postcode varchar(10) NULL,
                                 h_phone varchar(15) NULL,
                                 w_phone varchar(15) NULL,
                                 w_ph_local varchar(4) NULL,
                                 responded bool NOT NULL,
                                 date_excused timestamp(0) NULL,
                                 excusal_code varchar(1) NULL,
                                 acc_exc varchar(1) NULL,
                                 date_disq timestamp(0) NULL,
                                 disq_code varchar(1) NULL,
                                 user_edtq varchar(20) NULL,
                                 notes varchar(2000) NULL,
                                 no_def_pos int4 NULL,
                                 perm_disqual bool NULL,
                                 reasonable_adj_code varchar(1) NULL,
                                 reasonable_adj_msg varchar(60) NULL,
                                 smart_card varchar(20) NULL,
                                 completion_date timestamp(0) NULL,
                                 sort_code varchar(6) NULL,
                                 bank_acct_name varchar(18) NULL,
                                 bank_acct_no varchar(8) NULL,
                                 bldg_soc_roll_no varchar(18) NULL,
                                 welsh bool NULL,
                                 police_check varchar(50) NULL DEFAULT 'NOT_CHECKED'::character varying,
                                 last_update timestamp(0) NULL,
                                 summons_file varchar(20) NULL,
                                 m_phone varchar(15) NULL,
                                 h_email varchar(254) NULL,
                                 contact_preference int4 NULL DEFAULT 0,
                                 notifications int4 NULL DEFAULT 0,
                                 date_created timestamp NULL,
                                 optic_reference varchar(8) NULL,
                                 pending_title varchar(10) NULL,
                                 pending_first_name varchar(20) NULL,
                                 pending_last_name varchar(20) NULL,
                                 mileage int4 NULL,
                                 amount_spent float8 NULL,
                                 financial_loss float8 NULL,
                                 travel_time time NULL,
                                 bureau_transfer_date date NULL,
                                 CONSTRAINT juror_pk PRIMARY KEY (juror_number),
                                 CONSTRAINT police_check_val CHECK (((police_check)::text = ANY ((ARRAY['INSUFFICIENT_INFORMATION'::character varying, 'NOT_CHECKED'::character varying, 'IN_PROGRESS'::character varying, 'ELIGIBLE'::character varying, 'INELIGIBLE'::character varying, 'ERROR_RETRY_NAME_HAS_NUMERICS'::character varying, 'ERROR_RETRY_CONNECTION_ERROR'::character varying, 'ERROR_RETRY_OTHER_ERROR_CODE'::character varying, 'ERROR_RETRY_NO_ERROR_REASON'::character varying, 'ERROR_RETRY_UNEXPECTED_EXCEPTION'::character varying, 'UNCHECKED_MAX_RETRIES_EXCEEDED'::character varying])::text[]))),
                                 CONSTRAINT disq_code_fk FOREIGN KEY (disq_code) REFERENCES juror_mod.t_disq_code(disq_code),
                                 CONSTRAINT excusal_code_fk FOREIGN KEY (excusal_code) REFERENCES juror_mod.t_exc_code(exc_code),
                                 CONSTRAINT reasonable_adjustment_code_fk FOREIGN KEY (reasonable_adj_code) REFERENCES juror_mod.t_reasonable_adjustments(code)
);
CREATE INDEX i_zip_1 ON juror_mod.juror USING btree (postcode);
CREATE INDEX last_name_1 ON juror_mod.juror USING btree (last_name);


-- juror_mod.juror_audit definition

-- Drop table

-- DROP TABLE juror_mod.juror_audit;

CREATE TABLE juror_mod.juror_audit (
                                       revision int8 NOT NULL,
                                       juror_number varchar(255) NOT NULL,
                                       rev_type int4 NULL,
                                       title varchar(255) NULL,
                                       first_name varchar(255) NULL,
                                       last_name varchar(255) NULL,
                                       dob date NULL,
                                       address_line_1 varchar(255) NULL,
                                       address_line_2 varchar(255) NULL,
                                       address_line_3 varchar(255) NULL,
                                       address_line_4 varchar(255) NULL,
                                       address_line_5 varchar(255) NULL,
                                       address6 varchar(255) NULL,
                                       postcode varchar(255) NULL,
                                       h_email varchar(255) NULL,
                                       h_phone varchar(255) NULL,
                                       m_phone varchar(255) NULL,
                                       w_phone varchar(255) NULL,
                                       w_ph_local varchar(255) NULL,
                                       bank_acct_name varchar(255) NULL,
                                       bank_acct_no varchar(255) NULL,
                                       bldg_soc_roll_no varchar(255) NULL,
                                       sort_code varchar(255) NULL,
                                       pending_title varchar(255) NULL,
                                       pending_first_name varchar(255) NULL,
                                       pending_last_name varchar(255) NULL,
                                       CONSTRAINT juror_audit_pkey PRIMARY KEY (revision, juror_number),
                                       CONSTRAINT fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info(revision_number)
);


-- juror_mod.juror_history definition

-- Drop table

-- DROP TABLE juror_mod.juror_history;

CREATE TABLE juror_mod.juror_history (
                                         id bigserial NOT NULL,
                                         juror_number varchar(9) NOT NULL,
                                         date_created timestamp NULL,
                                         history_code varchar(4) NOT NULL,
                                         user_id varchar(20) NOT NULL,
                                         other_information text NULL,
                                         pool_number varchar(9) NULL,
                                         other_info_date date NULL,
                                         other_info_reference varchar(10) NULL,
                                         CONSTRAINT juror_hist_pk PRIMARY KEY (id),
                                         CONSTRAINT juror_history_fk FOREIGN KEY (juror_number) REFERENCES juror_mod.juror(juror_number),
                                         CONSTRAINT juror_history_hist_code_fk FOREIGN KEY (history_code) REFERENCES juror_mod.t_history_code(history_code)
);


-- juror_mod.juror_reasonable_adjustment definition

-- Drop table

-- DROP TABLE juror_mod.juror_reasonable_adjustment;

CREATE TABLE juror_mod.juror_reasonable_adjustment (
                                                       juror_number varchar(9) NOT NULL,
                                                       reasonable_adjustment varchar(1) NULL,
                                                       reasonable_adjustment_detail varchar(1000) NOT NULL,
                                                       id bigserial NOT NULL,
                                                       CONSTRAINT juror_response_reasonable_adjustments_pkey PRIMARY KEY (id),
                                                       CONSTRAINT juror_reasonable_adjustment_reasonable_adjustment_fkey FOREIGN KEY (reasonable_adjustment) REFERENCES juror_mod.t_reasonable_adjustments(code)
);


-- juror_mod.juror_response definition

-- Drop table

-- DROP TABLE juror_mod.juror_response;

CREATE TABLE juror_mod.juror_response (
                                          juror_number varchar(9) NOT NULL,
                                          date_received timestamp NULL,
                                          title varchar(10) NULL,
                                          first_name varchar(20) NULL,
                                          last_name varchar(20) NULL,
                                          address_line_1 varchar(35) NULL,
                                          address_line_2 varchar(35) NULL,
                                          address_line_3 varchar(35) NULL,
                                          address_line_4 varchar(35) NULL,
                                          address_line_5 varchar(35) NULL,
                                          postcode varchar(10) NULL,
                                          processing_status varchar(50) NULL,
                                          date_of_birth date NULL,
                                          phone_number varchar(15) NULL,
                                          alt_phone_number varchar(15) NULL,
                                          email varchar(254) NULL,
                                          residency bool NULL,
                                          residency_detail varchar(1250) NULL,
                                          mental_health_act bool NULL,
                                          mental_health_capacity bool NULL,
                                          mental_health_act_details varchar(2020) NULL,
                                          bail bool NULL,
                                          bail_details varchar(1250) NULL,
                                          convictions bool NULL,
                                          convictions_details varchar(1250) NULL,
                                          deferral bool NULL,
                                          deferral_reason varchar(1250) NULL,
                                          deferral_date varchar(1000) NULL,
                                          reasonable_adjustments_arrangements varchar(1000) NULL,
                                          excusal bool NULL,
                                          excusal_reason varchar(1250) NULL,
                                          processing_complete bool NULL,
                                          signed bool NULL,
                                          "version" int4 NULL DEFAULT 0,
                                          thirdparty_fname varchar(50) NULL,
                                          thirdparty_lname varchar(50) NULL,
                                          relationship varchar(50) NULL,
                                          main_phone varchar(50) NULL,
                                          other_phone varchar(50) NULL,
                                          email_address varchar(254) NULL,
                                          thirdparty_reason varchar(1250) NULL,
                                          thirdparty_other_reason varchar(1250) NULL,
                                          juror_phone_details bool NULL,
                                          juror_email_details bool NULL,
                                          staff_login varchar(20) NULL,
                                          staff_assignment_date timestamp NULL,
                                          urgent bool NULL,
                                          super_urgent bool NULL,
                                          completed_at timestamp NULL,
                                          welsh bool NULL,
                                          reply_type varchar(32) NULL,
                                          CONSTRAINT juror_response_pkey PRIMARY KEY (juror_number),
                                          CONSTRAINT juror_response_juror_number_fkey FOREIGN KEY (juror_number) REFERENCES juror_mod.juror(juror_number),
                                          CONSTRAINT juror_response_reply_type_fkey FOREIGN KEY (reply_type) REFERENCES juror_mod.t_reply_type("type")
);


-- juror_mod.juror_response_aud definition

-- Drop table

-- DROP TABLE juror_mod.juror_response_aud;

CREATE TABLE juror_mod.juror_response_aud (
                                              juror_number varchar(9) NULL,
                                              changed timestamp NULL,
                                              login varchar(20) NULL,
                                              old_processing_status varchar(50) NULL,
                                              new_processing_status varchar(50) NULL,
                                              CONSTRAINT juror_response_aud_juror_number_fkey FOREIGN KEY (juror_number) REFERENCES juror_mod.juror_response(juror_number)
);


-- juror_mod.juror_response_cjs_employment definition

-- Drop table

-- DROP TABLE juror_mod.juror_response_cjs_employment;

CREATE TABLE juror_mod.juror_response_cjs_employment (
                                                         juror_number varchar(9) NOT NULL,
                                                         cjs_employer varchar(100) NOT NULL,
                                                         cjs_employer_details varchar(1000) NOT NULL,
                                                         id bigserial NOT NULL,
                                                         CONSTRAINT juror_response_cjs_employment_pkey PRIMARY KEY (id),
                                                         CONSTRAINT juror_response_cjs_employment_juror_number_fkey FOREIGN KEY (juror_number) REFERENCES juror_mod.juror_response(juror_number)
);


-- juror_mod.message_to_placeholders definition

-- Drop table

-- DROP TABLE juror_mod.message_to_placeholders;

CREATE TABLE juror_mod.message_to_placeholders (
                                                   message_id numeric(38) NOT NULL,
                                                   placeholder_name varchar(48) NOT NULL,
                                                   CONSTRAINT message_to_placeholders_pkey PRIMARY KEY (message_id, placeholder_name),
                                                   CONSTRAINT message_id_fk FOREIGN KEY (message_id) REFERENCES juror_mod.t_message_template(id),
                                                   CONSTRAINT placeholder_name_fk FOREIGN KEY (placeholder_name) REFERENCES juror_mod.message_placeholders(placeholder_name)
);


-- juror_mod.notify_template_mapping definition

-- Drop table

-- DROP TABLE juror_mod.notify_template_mapping;

CREATE TABLE juror_mod.notify_template_mapping (
                                                   template_id varchar(50) NOT NULL,
                                                   template_name varchar(40) NULL,
                                                   notify_name varchar(60) NULL,
                                                   form_type varchar(6) NULL,
                                                   notification_type int2 NULL,
                                                   "version" int8 NULL,
                                                   CONSTRAINT notify_template_mapping_pkey PRIMARY KEY (template_id),
                                                   CONSTRAINT notify_template_mapping_template_name_key UNIQUE (template_name),
                                                   CONSTRAINT t_form_attr_fkey FOREIGN KEY (form_type) REFERENCES juror_mod.t_form_attr(form_type)
);


-- juror_mod.pending_juror definition

-- Drop table

-- DROP TABLE juror_mod.pending_juror;

CREATE TABLE juror_mod.pending_juror (
                                         juror_number varchar(9) NOT NULL,
                                         pool_number varchar(9) NOT NULL,
                                         title varchar(10) NULL,
                                         last_name varchar(20) NOT NULL,
                                         first_name varchar(20) NOT NULL,
                                         dob date NOT NULL,
                                         address_line_1 varchar(35) NOT NULL,
                                         address_line_2 varchar(35) NULL,
                                         address_line_3 varchar(35) NULL,
                                         address_line_4 varchar(35) NOT NULL,
                                         address_line_5 varchar(35) NULL,
                                         postcode varchar(10) NOT NULL,
                                         h_phone varchar(15) NULL,
                                         w_phone varchar(15) NULL,
                                         w_ph_local varchar(4) NULL,
                                         m_phone varchar(15) NULL,
                                         h_email varchar(254) NULL,
                                         contact_preference int4 NULL DEFAULT 0,
                                         responded bool NOT NULL,
                                         next_date date NULL,
                                         date_added date NOT NULL,
                                         mileage int4 NULL,
                                         pool_seq varchar(4) NULL,
                                         status varchar(1) NOT NULL,
                                         is_active bool NULL,
                                         added_by varchar(20) NULL,
                                         notes varchar(2000) NULL,
                                         date_created timestamp NOT NULL,
                                         CONSTRAINT pending_juror_pk PRIMARY KEY (juror_number),
                                         CONSTRAINT pending_juror_status_fk FOREIGN KEY (status) REFERENCES juror_mod.t_pending_juror_status(code)
);


-- juror_mod.pool definition

-- Drop table

-- DROP TABLE juror_mod.pool;

CREATE TABLE juror_mod.pool (
                                pool_no varchar(9) NOT NULL,
                                "owner" varchar(3) NOT NULL,
                                return_date date NOT NULL,
                                no_requested int4 NULL DEFAULT 0,
                                pool_type varchar(3) NULL,
                                loc_code varchar(3) NULL,
                                new_request varchar(1) NULL DEFAULT 'Y'::character varying,
                                last_update timestamp(0) NULL,
                                additional_summons int4 NULL,
                                attend_time timestamp(0) NULL,
                                nil_pool bool NULL DEFAULT false,
                                total_no_required int4 NOT NULL,
                                date_created timestamp NULL,
                                CONSTRAINT pool_pk PRIMARY KEY (pool_no),
                                CONSTRAINT pool_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code),
                                CONSTRAINT pool_pool_type_fk FOREIGN KEY (pool_type) REFERENCES juror_mod.t_pool_type(pool_type)
);
CREATE INDEX pool_rtndate_loccode ON juror_mod.pool USING btree (return_date, loc_code);


-- juror_mod.pool_comments definition

-- Drop table

-- DROP TABLE juror_mod.pool_comments;

CREATE TABLE juror_mod.pool_comments (
                                         id bigserial NOT NULL,
                                         pool_no varchar(9) NOT NULL,
                                         user_id varchar(20) NOT NULL,
                                         last_update timestamp(0) NULL,
                                         pcomment varchar(80) NOT NULL,
                                         no_requested int4 NULL DEFAULT 0,
                                         CONSTRAINT pool_comments_pkey PRIMARY KEY (id),
                                         CONSTRAINT pool_comments_fk FOREIGN KEY (pool_no) REFERENCES juror_mod.pool(pool_no),
                                         CONSTRAINT pool_comments_pool_no_fk FOREIGN KEY (pool_no) REFERENCES juror_mod.pool(pool_no)
);
CREATE INDEX pool_comments_pool_idx ON juror_mod.pool_comments USING btree (pool_no);


-- juror_mod.pool_history definition

-- Drop table

-- DROP TABLE juror_mod.pool_history;

CREATE TABLE juror_mod.pool_history (
                                        id bigserial NOT NULL,
                                        history_code varchar(4) NOT NULL,
                                        pool_no varchar(9) NOT NULL,
                                        history_date timestamp NULL,
                                        user_id varchar(20) NULL,
                                        other_information varchar(50) NULL,
                                        CONSTRAINT pool_history_pkey PRIMARY KEY (id),
                                        CONSTRAINT pool_history_fk FOREIGN KEY (history_code) REFERENCES juror_mod.t_history_code(history_code)
);
CREATE INDEX pool_history_pool_idx ON juror_mod.pool_history USING btree (pool_no);


-- juror_mod.staff_juror_response_audit definition

-- Drop table

-- DROP TABLE juror_mod.staff_juror_response_audit;

CREATE TABLE juror_mod.staff_juror_response_audit (
                                                      team_leader_login varchar(20) NOT NULL,
                                                      staff_login varchar(20) NULL,
                                                      juror_number varchar(9) NOT NULL,
                                                      date_received timestamp NOT NULL,
                                                      staff_assignment_date timestamp NOT NULL,
                                                      created timestamp NOT NULL,
                                                      "version" int4 NULL,
                                                      CONSTRAINT staff_juror_response_audit_pkey PRIMARY KEY (team_leader_login, juror_number, date_received, created),
                                                      CONSTRAINT staff_juror_response_audit_juror_number_fkey FOREIGN KEY (juror_number) REFERENCES juror_mod.juror_response(juror_number)
);


-- juror_mod.trial definition

-- Drop table

-- DROP TABLE juror_mod.trial;

CREATE TABLE juror_mod.trial (
                                 trial_number varchar(16) NOT NULL,
                                 loc_code varchar(3) NOT NULL,
                                 description varchar(50) NOT NULL,
                                 courtroom int8 NOT NULL,
                                 judge int8 NOT NULL,
                                 trial_type varchar(3) NOT NULL,
                                 trial_start_date date NULL,
                                 trial_end_date date NULL,
                                 anonymous bool NULL,
                                 juror_requested int2 NULL,
                                 jurors_sent int2 NULL,
                                 CONSTRAINT trial_pkey PRIMARY KEY (trial_number, loc_code),
                                 CONSTRAINT trial_type_val CHECK (((trial_type)::text = ANY (ARRAY['CIV'::text, 'CRI'::text]))),
                                 CONSTRAINT trial_court_loc_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code),
                                 CONSTRAINT trial_courtroom_fk FOREIGN KEY (courtroom) REFERENCES juror_mod.courtroom(id),
                                 CONSTRAINT trial_judge_fk FOREIGN KEY (judge) REFERENCES juror_mod.judge(id)
);


-- juror_mod.utilisation_stats definition

-- Drop table

-- DROP TABLE juror_mod.utilisation_stats;

CREATE TABLE juror_mod.utilisation_stats (
                                             "owner" varchar(3) NOT NULL,
                                             month_start timestamp(0) NOT NULL,
                                             loc_code varchar(3) NOT NULL,
                                             available_days int4 NULL,
                                             attendance_days int4 NULL,
                                             sitting_days int4 NULL,
                                             no_trials int4 NULL,
                                             last_update timestamp(0) NULL,
                                             CONSTRAINT attendance_pkey PRIMARY KEY (month_start, loc_code, owner),
                                             CONSTRAINT utilisation_stats_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code)
);
CREATE INDEX attendance_locde_mths_own ON juror_mod.utilisation_stats USING btree (loc_code, month_start, owner);


-- juror_mod.welsh_court_location definition

-- Drop table

-- DROP TABLE juror_mod.welsh_court_location;

CREATE TABLE juror_mod.welsh_court_location (
                                                loc_code varchar(3) NOT NULL,
                                                loc_name varchar(40) NULL,
                                                loc_address1 varchar(35) NULL,
                                                loc_address2 varchar(35) NULL,
                                                loc_address3 varchar(35) NULL,
                                                loc_address4 varchar(35) NULL,
                                                loc_address5 varchar(35) NULL,
                                                loc_address6 varchar(35) NULL,
                                                location_address varchar(230) NULL,
                                                CONSTRAINT welsh_court_location_pkey PRIMARY KEY (loc_code),
                                                CONSTRAINT welsh_court_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code)
);


-- juror_mod.appearance definition

-- Drop table

-- DROP TABLE juror_mod.appearance;

CREATE TABLE juror_mod.appearance (
                                      attendance_date date NOT NULL,
                                      juror_number varchar(9) NOT NULL,
                                      loc_code varchar(3) NOT NULL,
                                      time_in time NULL,
                                      time_out time NULL,
                                      trial_number varchar(16) NULL,
                                      non_attendance bool NULL,
                                      no_show bool NULL,
                                      mileage_due int4 NULL,
                                      misc_description varchar(50) NULL,
                                      pay_cash bool NULL,
                                      last_updated_by varchar(20) NULL,
                                      created_by varchar(20) NULL,
                                      public_transport_total_due numeric(8, 2) NULL,
                                      public_transport_total_paid numeric(8, 2) NULL,
                                      hired_vehicle_total_due numeric(8, 2) NULL,
                                      hired_vehicle_total_paid numeric(8, 2) NULL,
                                      motorcycle_total_due numeric(8, 2) NULL,
                                      motorcycle_total_paid numeric(8, 2) NULL,
                                      car_total_due numeric(8, 2) NULL,
                                      car_total_paid numeric(8, 2) NULL,
                                      pedal_cycle_total_due numeric(8, 2) NULL,
                                      pedal_cycle_total_paid numeric(8, 2) NULL,
                                      childcare_total_due numeric(8, 2) NULL,
                                      childcare_total_paid numeric(8, 2) NULL,
                                      parking_total_due numeric(8, 2) NULL,
                                      parking_total_paid numeric(8, 2) NULL,
                                      misc_total_due numeric(8, 2) NULL,
                                      misc_total_paid numeric(8, 2) NULL,
                                      smart_card_due numeric(8, 2) NULL,
                                      payment_approved_date date NULL,
                                      expense_submitted_date date NULL,
                                      is_draft_expense bool NULL,
                                      f_audit int8 NULL,
                                      sat_on_jury bool NULL,
                                      pool_number varchar(9) NULL,
                                      appearance_stage varchar(25) NULL,
                                      loss_of_earnings_due numeric(8, 2) NULL,
                                      loss_of_earnings_paid numeric(8, 2) NULL,
                                      subsistence_due numeric(8, 2) NULL,
                                      subsistence_paid numeric(8, 2) NULL,
                                      attendance_type varchar(25) NULL,
                                      mileage_paid int4 NULL,
                                      smart_card_paid numeric(8, 2) NULL,
                                      pay_attendance_type varchar(25) NULL,
                                      travel_time time NULL,
                                      travel_jurors_taken_by_car int4 NULL,
                                      travel_by_car bool NULL,
                                      travel_jurors_taken_by_motorcycle int4 NULL,
                                      travel_by_motorcycle bool NULL,
                                      travel_by_bicycle bool NULL,
                                      miles_traveled int4 NULL,
                                      food_and_drink_claim_type varchar(20) NULL,
                                      CONSTRAINT appearance_pkey PRIMARY KEY (juror_number, attendance_date, loc_code),
                                      CONSTRAINT appearance_stage_val CHECK (((appearance_stage)::text = ANY ((ARRAY['CHECKED_IN'::character varying, 'CHECKED_OUT'::character varying, 'APPEARANCE_CONFIRMED'::character varying, 'EXPENSE_ENTERED'::character varying, 'EXPENSE_AUTHORISED'::character varying, 'EXPENSE_EDITED'::character varying])::text[]))),
                                      CONSTRAINT attendance_type_val CHECK (((attendance_type)::text = ANY ((ARRAY['FULL_DAY'::character varying, 'HALF_DAY'::character varying, 'FULL_DAY_LONG_TRIAL'::character varying, 'HALF_DAY_LONG_TRIAL'::character varying, 'ABSENT'::character varying, 'NON_ATTENDANCE'::character varying])::text[]))),
                                      CONSTRAINT food_and_drink_claim_type_val CHECK (((food_and_drink_claim_type)::text = ANY ((ARRAY['NONE'::character varying, 'LESS_THAN_1O_HOURS'::character varying, 'MORE_THAN_10_HOURS'::character varying])::text[]))),
                                      CONSTRAINT pay_attendance_type_val CHECK (((pay_attendance_type)::text = ANY ((ARRAY['FULL_DAY'::character varying, 'HALF_DAY'::character varying, 'FULL_DAY_LONG_TRIAL'::character varying, 'HALF_DAY_LONG_TRIAL'::character varying, 'NON_ATTENDANCE'::character varying])::text[]))),
                                      CONSTRAINT app_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code),
                                      CONSTRAINT appearance_juror_fk FOREIGN KEY (juror_number) REFERENCES juror_mod.juror(juror_number),
                                      CONSTRAINT appearance_pool_fk FOREIGN KEY (pool_number) REFERENCES juror_mod.pool(pool_no),
                                      CONSTRAINT appearance_trial_fk FOREIGN KEY (trial_number,loc_code) REFERENCES juror_mod.trial(trial_number,loc_code)
);


-- juror_mod.appearance_audit definition

-- Drop table

-- DROP TABLE juror_mod.appearance_audit;

CREATE TABLE juror_mod.appearance_audit (
                                            revision int8 NOT NULL,
                                            rev_type int4 NULL,
                                            attendance_date date NOT NULL,
                                            juror_number varchar(9) NOT NULL,
                                            loc_code varchar(3) NOT NULL,
                                            time_in time NULL,
                                            time_out time NULL,
                                            trial_number varchar(16) NULL,
                                            non_attendance bool NULL,
                                            no_show bool NULL,
                                            mileage_due int4 NULL,
                                            mileage_paid int4 NULL,
                                            misc_description varchar(50) NULL,
                                            pay_cash bool NULL,
                                            last_updated_by varchar(20) NULL,
                                            created_by varchar(20) NULL,
                                            public_transport_total_due numeric(8, 2) NULL,
                                            public_transport_total_paid numeric(8, 2) NULL,
                                            hired_vehicle_total_due numeric(8, 2) NULL,
                                            hired_vehicle_total_paid numeric(8, 2) NULL,
                                            motorcycle_total_due numeric(8, 2) NULL,
                                            motorcycle_total_paid numeric(8, 2) NULL,
                                            car_total_due numeric(8, 2) NULL,
                                            car_total_paid numeric(8, 2) NULL,
                                            pedal_cycle_total_due numeric(8, 2) NULL,
                                            pedal_cycle_total_paid numeric(8, 2) NULL,
                                            childcare_total_due numeric(8, 2) NULL,
                                            childcare_total_paid numeric(8, 2) NULL,
                                            parking_total_due numeric(8, 2) NULL,
                                            parking_total_paid numeric(8, 2) NULL,
                                            misc_total_due numeric(8, 2) NULL,
                                            misc_total_paid numeric(8, 2) NULL,
                                            smart_card_due numeric(8, 2) NULL,
                                            smart_card_paid numeric(8, 2) NULL,
                                            payment_approved_date date NULL,
                                            expense_submitted_date date NULL,
                                            is_draft_expense bool NULL,
                                            f_audit int8 NULL,
                                            sat_on_jury bool NULL,
                                            pool_number varchar(9) NULL,
                                            appearance_stage varchar(25) NULL,
                                            loss_of_earnings_due numeric(8, 2) NULL,
                                            loss_of_earnings_paid numeric(8, 2) NULL,
                                            subsistence_due numeric(8, 2) NULL,
                                            subsistence_paid numeric(8, 2) NULL,
                                            attendance_type varchar(25) NULL,
                                            pay_attendance_type varchar(25) NULL,
                                            travel_time time NULL,
                                            travel_jurors_taken_by_car int4 NULL,
                                            travel_by_car bool NULL,
                                            travel_jurors_taken_by_motorcycle int4 NULL,
                                            travel_by_motorcycle bool NULL,
                                            travel_by_bicycle bool NULL,
                                            miles_traveled int4 NULL,
                                            food_and_drink_claim_type varchar(20) NULL,
                                            CONSTRAINT appearance_audit_pkey PRIMARY KEY (revision, juror_number, attendance_date, loc_code),
                                            CONSTRAINT food_and_drink_claim_type_val CHECK (((food_and_drink_claim_type)::text = ANY ((ARRAY['NONE'::character varying, 'LESS_THAN_1O_HOURS'::character varying, 'MORE_THAN_10_HOURS'::character varying])::text[]))),
                                            CONSTRAINT pay_attendance_type_val CHECK (((pay_attendance_type)::text = ANY ((ARRAY['FULL_DAY'::character varying, 'HALF_DAY'::character varying, 'FULL_DAY_LONG_TRIAL'::character varying, 'HALF_DAY_LONG_TRIAL'::character varying, 'NON_ATTENDANCE'::character varying])::text[]))),
                                            CONSTRAINT fk_f_audit FOREIGN KEY (f_audit) REFERENCES juror_mod.financial_audit_details(id),
                                            CONSTRAINT fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info(revision_number)
);


-- juror_mod.bulk_print_data definition

-- Drop table

-- DROP TABLE juror_mod.bulk_print_data;

CREATE TABLE juror_mod.bulk_print_data (
                                           id int8 NOT NULL DEFAULT nextval('juror_mod.bulk_print_data_seq'::regclass),
                                           juror_no varchar(9) NOT NULL,
                                           creation_date date NOT NULL,
                                           form_type varchar(6) NOT NULL,
                                           detail_rec varchar(1260) NOT NULL,
                                           extracted_flag bool NULL,
                                           digital_comms bool NULL DEFAULT false,
                                           CONSTRAINT bulk_print_data_pkey PRIMARY KEY (id),
                                           CONSTRAINT bulk_print_data_fk_form_type FOREIGN KEY (form_type) REFERENCES juror_mod.t_form_attr(form_type),
                                           CONSTRAINT bulk_print_data_juror_no_fk FOREIGN KEY (juror_no) REFERENCES juror_mod.juror(juror_number)
);


-- juror_mod.contact_log definition

-- Drop table

-- DROP TABLE juror_mod.contact_log;

CREATE TABLE juror_mod.contact_log (
                                       id bigserial NOT NULL,
                                       juror_number varchar(9) NOT NULL,
                                       user_id varchar(20) NULL,
                                       notes varchar(2000) NULL,
                                       last_update timestamp NULL,
                                       start_call timestamp NULL,
                                       end_call timestamp NULL,
                                       enquiry_type varchar(2) NULL,
                                       repeat_enquiry bool NULL,
                                       CONSTRAINT contact_log_pkey PRIMARY KEY (id),
                                       CONSTRAINT juror_number_fk FOREIGN KEY (juror_number) REFERENCES juror_mod.juror(juror_number),
                                       CONSTRAINT t_contact_fk FOREIGN KEY (enquiry_type) REFERENCES juror_mod.t_contact(enquiry_code)
);
CREATE INDEX contact_log_part_no ON juror_mod.contact_log USING btree (juror_number, start_call, last_update);


-- juror_mod.juror_pool definition

-- Drop table

-- DROP TABLE juror_mod.juror_pool;

CREATE TABLE juror_mod.juror_pool (
                                      juror_number varchar(9) NOT NULL,
                                      pool_number varchar(9) NOT NULL,
                                      "owner" varchar(3) NOT NULL,
                                      user_edtq varchar(20) NULL,
                                      is_active bool NULL,
                                      status int4 NULL,
                                      times_sel int4 NULL,
                                      def_date date NULL,
                                      "location" varchar(6) NULL,
                                      no_attendances int4 NULL,
                                      no_attended int4 NULL,
                                      no_fta int4 NULL,
                                      no_awol int4 NULL,
                                      pool_seq varchar(4) NULL,
                                      edit_tag varchar(1) NULL,
                                      next_date date NULL,
                                      on_call bool NULL,
                                      smart_card varchar(20) NULL,
                                      was_deferred bool NULL,
                                      deferral_code varchar(1) NULL,
                                      id_checked varchar(1) NULL,
                                      postpone bool NULL,
                                      paid_cash bool NULL,
                                      scan_code varchar(9) NULL,
                                      last_update timestamp NULL,
                                      reminder_sent bool NULL,
                                      transfer_date date NULL,
                                      date_created timestamp NULL,
                                      CONSTRAINT juror_pool_pkey PRIMARY KEY (juror_number, pool_number),
                                      CONSTRAINT juror_pool_fk_juror FOREIGN KEY (juror_number) REFERENCES juror_mod.juror(juror_number),
                                      CONSTRAINT juror_pool_pool_no_fk FOREIGN KEY (pool_number) REFERENCES juror_mod.pool(pool_no),
                                      CONSTRAINT juror_pool_status_fk FOREIGN KEY (status) REFERENCES juror_mod.t_juror_status(status)
);
CREATE INDEX i_juror_no ON juror_mod.juror_pool USING btree (juror_number);
CREATE INDEX i_next_date ON juror_mod.juror_pool USING btree (next_date);
CREATE INDEX i_pool_no ON juror_mod.juror_pool USING btree (pool_number);




-- juror_mod.court_deferral_granted source

CREATE OR REPLACE VIEW juror_mod.court_deferral_granted
AS SELECT jp.owner,
          jp.pool_number,
          j.juror_number,
          j.first_name,
          j.last_name,
          j.postcode,
          js.status_desc,
          jp.def_date,
          d.description AS deferral_reason,
          jh.date_created AS date_printed,
          jp.is_active,
          row_number() OVER (PARTITION BY j.juror_number ORDER BY jp.def_date DESC) AS row_no
   FROM juror_mod.juror_pool jp
            JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
            JOIN juror_mod.t_exc_code d ON d.exc_code::text = jp.deferral_code::text
            JOIN juror_mod.t_juror_status js ON js.status = jp.status
            LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'RDEF'::text AND jh.other_info_date = jp.def_date AND jh.date_created > j.bureau_transfer_date
   WHERE jp.status = 7 AND d.exc_code::text <> 'P'::text;


-- juror_mod.currently_deferred source

CREATE OR REPLACE VIEW juror_mod.currently_deferred
AS SELECT j.juror_number,
          jp.owner,
          jp.def_date AS deferred_to,
          p.loc_code
   FROM juror_mod.juror j
            JOIN juror_mod.juror_pool jp ON j.juror_number::text = jp.juror_number::text
            JOIN juror_mod.pool p ON jp.pool_number::text = p.pool_no::text
   WHERE jp.status = 7 AND jp.is_active = true;


-- juror_mod.juror_digital_response source

CREATE OR REPLACE VIEW juror_mod.juror_digital_response
AS SELECT jr.juror_number,
          jr.date_received,
          jr.title,
          jr.first_name,
          jr.last_name,
          jr.address_line_1,
          jr.address_line_2,
          jr.address_line_3,
          jr.address_line_4,
          jr.address_line_5,
          jr.postcode,
          jr.processing_status,
          jr.date_of_birth,
          jr.phone_number,
          jr.alt_phone_number,
          jr.email,
          jr.residency,
          jr.residency_detail,
          jr.mental_health_act,
          jr.mental_health_act_details,
          jr.bail,
          jr.bail_details,
          jr.convictions,
          jr.convictions_details,
          jr.deferral_reason,
          jr.deferral_date,
          jr.reasonable_adjustments_arrangements,
          jr.excusal_reason,
          jr.processing_complete,
          jr.version,
          jr.thirdparty_fname,
          jr.thirdparty_lname,
          jr.relationship,
          jr.main_phone,
          jr.other_phone,
          jr.email_address,
          jr.thirdparty_reason,
          jr.thirdparty_other_reason,
          jr.juror_phone_details,
          jr.juror_email_details,
          jr.staff_login,
          jr.staff_assignment_date,
          jr.urgent,
          jr.super_urgent,
          jr.completed_at,
          jr.welsh
   FROM juror_mod.juror_response jr
   WHERE lower(jr.reply_type::text) = 'digital'::text;


-- juror_mod.juror_expense_subtotals source

CREATE OR REPLACE VIEW juror_mod.juror_expense_subtotals
AS SELECT a.juror_number,
          a.pool_number,
          j.first_name,
          j.last_name,
          a.loc_code,
          sum(COALESCE(a.public_transport_total_due, 0::numeric)) AS public_transport_total_due_total,
          sum(COALESCE(a.public_transport_total_paid, 0::numeric)) AS public_transport_total_paid_total,
          sum(COALESCE(a.hired_vehicle_total_due, 0::numeric)) AS hired_vehicle_total_due_total,
          sum(COALESCE(a.hired_vehicle_total_paid, 0::numeric)) AS hired_vehicle_total_paid_total,
          sum(COALESCE(a.motorcycle_total_due, 0::numeric)) AS motorcycle_total_due_total,
          sum(COALESCE(a.motorcycle_total_paid, 0::numeric)) AS motorcycle_total_paid_total,
          sum(COALESCE(a.car_total_due, 0::numeric)) AS car_total_due_total,
          sum(COALESCE(a.car_total_paid, 0::numeric)) AS car_total_paid_total,
          sum(COALESCE(a.pedal_cycle_total_due, 0::numeric)) AS pedal_cycle_total_due_total,
          sum(COALESCE(a.pedal_cycle_total_paid, 0::numeric)) AS pedal_cycle_total_paid_total,
          sum(COALESCE(a.parking_total_due, 0::numeric)) AS parking_total_due_total,
          sum(COALESCE(a.parking_total_paid, 0::numeric)) AS parking_total_paid_total,
          sum(COALESCE(a.loss_of_earnings_due, 0::numeric)) AS loss_of_earnings_due_total,
          sum(COALESCE(a.loss_of_earnings_paid, 0::numeric)) AS loss_of_earnings_paid_total,
          sum(COALESCE(a.childcare_total_due, 0::numeric)) AS childcare_total_due_total,
          sum(COALESCE(a.childcare_total_paid, 0::numeric)) AS childcare_total_paid_total,
          sum(COALESCE(a.misc_total_due, 0::numeric)) AS misc_total_due_total,
          sum(COALESCE(a.misc_total_paid, 0::numeric)) AS misc_total_paid_total,
          sum(COALESCE(a.subsistence_due, 0::numeric)) AS subsistence_due_total,
          sum(COALESCE(a.subsistence_paid, 0::numeric)) AS subsistence_paid_total,
          sum(COALESCE(a.smart_card_due, 0::numeric)) AS smart_card_spend_total
   FROM juror_mod.appearance a
            JOIN juror_mod.juror j ON j.juror_number::text = a.juror_number::text
   GROUP BY a.juror_number, a.pool_number, j.first_name, j.last_name, a.loc_code;


-- juror_mod.juror_expense_totals source

CREATE OR REPLACE VIEW juror_mod.juror_expense_totals
AS SELECT juror_expense_subtotals.juror_number,
          juror_expense_subtotals.pool_number,
          juror_expense_subtotals.first_name,
          juror_expense_subtotals.last_name,
          juror_expense_subtotals.loc_code,
          juror_expense_subtotals.public_transport_total_due_total + juror_expense_subtotals.hired_vehicle_total_due_total + juror_expense_subtotals.motorcycle_total_due_total + juror_expense_subtotals.car_total_due_total + juror_expense_subtotals.pedal_cycle_total_due_total + juror_expense_subtotals.parking_total_due_total AS travel_unapproved,
          juror_expense_subtotals.public_transport_total_paid_total + juror_expense_subtotals.hired_vehicle_total_paid_total + juror_expense_subtotals.motorcycle_total_paid_total + juror_expense_subtotals.car_total_paid_total + juror_expense_subtotals.pedal_cycle_total_paid_total + juror_expense_subtotals.parking_total_paid_total AS travel_approved,
          juror_expense_subtotals.loss_of_earnings_due_total AS financial_loss_unapproved,
          juror_expense_subtotals.loss_of_earnings_paid_total AS financial_loss_approved,
          juror_expense_subtotals.subsistence_due_total AS subsistence_unapproved,
          juror_expense_subtotals.subsistence_paid_total AS subsistence_approved,
          juror_expense_subtotals.smart_card_spend_total,
          juror_expense_subtotals.public_transport_total_due_total + juror_expense_subtotals.hired_vehicle_total_due_total + juror_expense_subtotals.motorcycle_total_due_total + juror_expense_subtotals.car_total_due_total + juror_expense_subtotals.pedal_cycle_total_due_total + juror_expense_subtotals.parking_total_due_total + juror_expense_subtotals.childcare_total_due_total + juror_expense_subtotals.misc_total_due_total + juror_expense_subtotals.loss_of_earnings_due_total + juror_expense_subtotals.subsistence_due_total AS total_unapproved,
          juror_expense_subtotals.public_transport_total_paid_total + juror_expense_subtotals.hired_vehicle_total_paid_total + juror_expense_subtotals.motorcycle_total_paid_total + juror_expense_subtotals.car_total_paid_total + juror_expense_subtotals.pedal_cycle_total_paid_total + juror_expense_subtotals.parking_total_paid_total + juror_expense_subtotals.childcare_total_paid_total + juror_expense_subtotals.misc_total_paid_total + juror_expense_subtotals.loss_of_earnings_paid_total + juror_expense_subtotals.subsistence_paid_total AS total_approved
   FROM juror_mod.juror_expense_subtotals;


-- juror_mod.juror_paper_response source

CREATE OR REPLACE VIEW juror_mod.juror_paper_response
AS SELECT jr.juror_number,
          jr.date_received,
          jr.title,
          jr.first_name,
          jr.last_name,
          jr.address_line_1,
          jr.address_line_2,
          jr.address_line_3,
          jr.address_line_4,
          jr.address_line_5,
          jr.postcode,
          jr.processing_status,
          jr.date_of_birth,
          jr.phone_number,
          jr.alt_phone_number,
          jr.email,
          jr.residency,
          jr.mental_health_act,
          jr.mental_health_capacity,
          jr.bail,
          jr.convictions,
          jr.reasonable_adjustments_arrangements,
          jr.relationship,
          jr.thirdparty_reason,
          jr.deferral AS excusal,
          jr.signed,
          jr.staff_login,
          jr.urgent,
          jr.super_urgent,
          jr.processing_complete,
          jr.completed_at,
          jr.welsh
   FROM juror_mod.juror_response jr
   WHERE lower(jr.reply_type::text) = 'paper'::text;


-- juror_mod.loc_postcode_totals_view source

CREATE OR REPLACE VIEW juror_mod.loc_postcode_totals_view
AS SELECT voters.loc_code,
          voters.zip,
          sum(
                  CASE
                      WHEN voters.date_selected1 IS NULL AND voters.perm_disqual IS NULL THEN 1
                      ELSE 0
                      END) AS total,
          sum(
                  CASE
                      WHEN voters.date_selected1 IS NULL AND voters.perm_disqual IS NULL AND voters.flags IS NULL THEN 1
                      ELSE 0
                      END) AS total_cor
   FROM juror_mod.voters
   WHERE voters.date_selected1 IS NULL AND voters.perm_disqual IS NULL
   GROUP BY voters.loc_code, voters.zip;



-- juror_mod.panel_stats_view source

CREATE OR REPLACE VIEW juror_mod.panel_stats_view
AS SELECT juror_trial.trial_number,
          juror_trial.loc_code,
          count(*) AS jurors_requested,
          count(*) FILTER (WHERE juror_trial.result::text = 'J'::text) AS jury_count
   FROM juror_mod.juror_trial
   GROUP BY juror_trial.trial_number, juror_trial.loc_code;


-- juror_mod.pool_stats source

CREATE OR REPLACE VIEW juror_mod.pool_stats
AS SELECT jp.pool_number,
          sum(
                  CASE
                      WHEN jp.owner::text = '400'::text THEN 1
                      ELSE 0
                      END) AS total_summoned,
          sum(
                  CASE
                      WHEN jp.owner::text <> '400'::text AND jp.is_active = true THEN 1
                      ELSE 0
                      END) AS court_supply,
          sum(
                  CASE
                      WHEN jp.owner::text = '400'::text AND jp.status = 2 THEN 1
                      ELSE 0
                      END) AS available,
          sum(
                  CASE
                      WHEN jp.owner::text = '400'::text AND (jp.status <> ALL (ARRAY[1, 2, 11])) THEN 1
                      ELSE 0
                      END) AS unavailable,
          sum(
                  CASE
                      WHEN jp.owner::text = '400'::text AND (jp.status = ANY (ARRAY[1, 11])) THEN 1
                      ELSE 0
                      END) AS unresolved
   FROM juror_mod.juror_pool jp
            JOIN juror_mod.juror j ON jp.juror_number::text = j.juror_number::text
   WHERE j.summons_file IS NULL OR j.summons_file::text <> 'Disq. on selection'::text
   GROUP BY jp.pool_number;


-- juror_mod.require_pnc_check_view source

CREATE OR REPLACE VIEW juror_mod.require_pnc_check_view
AS SELECT j.police_check,
          j.juror_number,
          regexp_replace(j.first_name::text, '\s.*'::text, ''::text) AS first_name,
          NULLIF(regexp_replace(j.first_name::text, '.*?\s'::text, ''::text), j.first_name::text) AS middle_name,
          j.last_name,
          j.dob AS date_of_birth,
          regexp_replace(j.postcode::text, '\s'::text, ''::text) AS post_code
   FROM juror_mod.juror j
            JOIN juror_mod.juror_pool jp ON jp.juror_number::text = j.juror_number::text
   WHERE jp.status = 2 AND (j.police_check IS NULL OR (j.police_check::text <> ALL (ARRAY['UNCHECKED_MAX_RETRIES_EXCEEDED'::character varying, 'ELIGIBLE'::character varying, 'INELIGIBLE'::character varying]::text[]))) AND jp.owner::text = '400'::text AND jp.is_active = true;


-- juror_mod.summons_snapshot source

CREATE OR REPLACE VIEW juror_mod.summons_snapshot
AS WITH original_summons_cte AS (
    SELECT jh.juror_number,
           jh.pool_number AS pool_no,
           jh.date_created,
           row_number() OVER (PARTITION BY jh.juror_number ORDER BY jh.date_created) AS row_no
    FROM juror_mod.juror_history jh
    WHERE jh.history_code::text = 'RSUM'::text
)
   SELECT os.juror_number,
          os.pool_no,
          p.return_date AS service_start_date,
          p.loc_code,
          cl.loc_name AS location_name,
          cl.loc_court_name AS court_name,
          os.date_created
   FROM original_summons_cte os
            JOIN juror_mod.pool p ON p.pool_no::text = os.pool_no::text
            JOIN juror_mod.court_location cl ON p.loc_code::text = cl.loc_code::text
   WHERE os.row_no = 1;



-- DROP FUNCTION juror_mod.generatependingjurornumber(text);

CREATE OR REPLACE FUNCTION juror_mod.generatependingjurornumber(location_code text)
    RETURNS text
    LANGUAGE plpgsql
AS $function$
declare
    available_juror_number text;
begin
    select min(available_juror_numbers.juror_number)
    into available_juror_number
    from (select CONCAT('0', location_code) || ltrim(to_char(numbers, '09999')) as juror_number
          from generate_series(1, 99999) numbers
          except all
          (select j.juror_number as juror_number
           from juror_mod.juror j
           where j.juror_number like CONCAT('0', location_code, '%')
           UNION ALL
           select pj.juror_number
           from juror_mod.pending_juror pj)) as available_juror_numbers;
    return available_juror_number;
end;
$function$
;

-- DROP FUNCTION juror_mod.get_active_pools_at_court_location(text);

CREATE OR REPLACE FUNCTION juror_mod.get_active_pools_at_court_location(p_loccode text)
    RETURNS TABLE(pool_number character varying, total_possible_in_attendance bigint, in_attendance bigint, on_call bigint, total_possible_on_trial bigint, jurors_on_trial bigint, pool_type character varying, service_start_date date)
    LANGUAGE plpgsql
AS $function$
begin

    -- find jurors on call and possible number of jurors on trial and attendance, limit the search to 4 weeks to improve performance
    return query with cte_first_query as (
        select p.pool_no as pool_number,
               sum(case when jp.status = 2 and jp.on_call = true then 1 else 0 end) as on_call,
               sum(case when jp.status = 2 and (jp.on_call is null or jp.on_call = false) then 1 else 0 end) as total_possible_in_attendance,
               sum(case when jp.status in (3,4) then 1 else 0 end) as total_possible_on_trial,
               p.pool_type,
               p.return_date as service_start_date
        from juror_mod.juror_pool jp
                 join juror_mod.pool p
                      on jp.pool_number = p.pool_no
        where p.loc_code  = p_loccode and p.return_date <= current_date and jp.is_active = true and p.return_date >= current_date - interval '4 weeks'
        group by pool_no
    ),
                      cte_second_query as (  -- find jurors who are physically at court today
                          select p.pool_no,
                                 sum(case when jp.status = 2 and a.appearance_stage = 'CHECKED_IN' then 1 else 0 end) as in_attendance,
                                 sum(case when jp.status in (3,4) and a.appearance_stage = 'CHECKED_IN' then 1 else 0 end) as jurors_on_trial
                          from juror_mod.juror_pool jp
                                   join juror_mod.pool p
                                        on jp.pool_number = p.pool_no
                                   left join juror_mod.appearance a
                                             on jp.juror_number = a.juror_number and jp.pool_number = a.pool_number
                          where p.loc_code  = p_loccode and a.attendance_date = current_date and jp.is_active = true
                          group by pool_no
                      )

                 select cte1.pool_number, cte1.total_possible_in_attendance, cte2.in_attendance, cte1.on_call, cte1.total_possible_on_trial,
                        cte2.jurors_on_trial, cte1.pool_type, cte1.service_start_date
                 from cte_first_query cte1
                          join cte_second_query cte2
                               on cte1.pool_number = cte2.pool_no;

END;
$function$
;

-- DROP FUNCTION juror_mod.get_voters(int8, text, text, text, text, text);

CREATE OR REPLACE FUNCTION juror_mod.get_voters(p_required bigint, p_mindate text, p_maxdate text, p_loccode text, p_areacode_list text, p_pool_type text)
    RETURNS TABLE(part_number character varying, juror_flags character varying)
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS $function$
DECLARE
    -- p_pool_type can be 'C'ORONER OR 'N'ON CORONER POOLS (REGULAR POOL)

    -- using Julian Format for date comparison
    l_julian_min_dt bigint := (to_char(to_date(p_minDate, 'yyyy-mm-dd'),'J'))::numeric;
    l_julian_max_dt bigint := (to_char(to_date(p_maxDate, 'yyyy-mm-dd'),'J'))::numeric;
begin
    return query select part_no, flags from juror_mod.voters
                 where loc_code=p_LocCode and date_selected1 is null
                   and ((dob is null) or to_number(to_char(dob,'J'),'9999999') > l_julian_min_dt
                     and to_number(to_char(dob,'J'),'9999999') < l_julian_max_dt)
                   and perm_disqual is null
                   and (split_part(zip, ' ', 1) in (select unnest(string_to_array(p_areacode_list, ',')))) -- specified postcode areas
                   and (flags is null or p_pool_type = 'N') -- only coroner pools check flag
                 order by random()
                 limit p_required*1.2; -- grab 20% more than requested to allow for jurors with flags

END;
$function$
;



-- DROP FUNCTION juror_mod.weekdayname_to_dow(varchar);

CREATE OR REPLACE FUNCTION juror_mod.weekdayname_to_dow(weekdayname character varying)
    RETURNS integer
    LANGUAGE sql
AS $function$
SELECT
    CASE UPPER(weekdayname)
        WHEN 'SUNDAY'
            THEN 0
        WHEN 'MONDAY'
            THEN 1
        WHEN 'TUESDAY'
            THEN 2
        WHEN 'WEDNESDAY'
            THEN 3
        WHEN 'THURSDAY'
            THEN 4
        WHEN 'FRIDAY'
            THEN 5
        WHEN 'SATURDAY'
            THEN 6
        END AS ret_val
$function$
;
-- DROP FUNCTION juror_mod.next_day(date, varchar);

CREATE OR REPLACE FUNCTION juror_mod.next_day(date_input date, weekdayname character varying)
    RETURNS date
    LANGUAGE sql
AS $function$
SELECT $1::DATE + COALESCE(NULLIF((7 + juror_mod.weekdayname_to_dow($2) - EXTRACT(DOW FROM $1::DATE))::INT % 7, 0), 7) AS result
$function$
;
-- juror_mod.active_pools_bureau source

CREATE OR REPLACE VIEW juror_mod.active_pools_bureau
AS SELECT pr.pool_no,
          pr.no_requested AS jurors_requested,
          CASE
              WHEN ps.available IS NULL THEN 0::bigint
              ELSE ps.available
              END AS confirmed_jurors,
          cl.loc_name AS court_name,
          pt.pool_type_desc AS pool_type,
          pr.return_date AS service_start_date
   FROM juror_mod.pool pr
            LEFT JOIN juror_mod.pool_stats ps ON pr.pool_no::text = ps.pool_number::text
            JOIN juror_mod.court_location cl ON pr.loc_code::text = cl.loc_code::text
            JOIN juror_mod.t_pool_type pt ON pr.pool_type::text = pt.pool_type::text
   WHERE pr.owner::text = '400'::text AND pr.new_request::text = 'N'::text AND pr.no_requested <> 0;
-- juror_mod.mod_juror_detail source

CREATE OR REPLACE VIEW juror_mod.mod_juror_detail
AS WITH juror_details_cte AS (
    SELECT j_1.juror_number,
           COALESCE(s.pool_no, jp.pool_number) AS pool_no,
           COALESCE(s.service_start_date, p.return_date) AS ret_date,
           COALESCE(s.loc_code, p.loc_code) AS loc_code,
           j_1.title,
           j_1.first_name,
           j_1.last_name,
           j_1.address_line_1,
           j_1.address_line_2,
           j_1.address_line_3,
           j_1.address_line_4,
           j_1.address_line_5,
           j_1.postcode,
           jp.next_date,
           jp.status,
           j_1.h_phone AS phone_number,
           j_1.m_phone AS alt_phone_number,
           j_1.dob,
           j_1.notes,
           j_1.h_email AS email,
           j_1.last_update,
           r.title AS new_title,
           r.first_name AS new_first_name,
           r.last_name AS new_last_name,
           r.address_line_1 AS new_address_1,
           r.address_line_2 AS new_address_2,
           r.address_line_3 AS new_address_3,
           r.address_line_4 AS new_address_4,
           r.address_line_5 AS new_address_5,
           r.postcode AS new_postcode,
           r.date_received,
           r.processing_status,
           r.phone_number AS new_phone_number,
           r.alt_phone_number AS new_alt_phone_number,
           r.date_of_birth AS new_dob,
           r.email AS new_email,
           r.thirdparty_fname,
           r.thirdparty_lname,
           r.thirdparty_reason,
           r.thirdparty_other_reason,
           r.main_phone,
           r.other_phone,
           r.email_address,
           r.relationship,
           r.residency,
           r.residency_detail,
           r.mental_health_act,
           r.mental_health_act_details,
           r.bail,
           r.bail_details,
           r.convictions,
           r.convictions_details,
           r.deferral_reason,
           r.deferral_date,
           r.reasonable_adjustments_arrangements,
           r.excusal_reason,
           r.processing_complete,
           r.completed_at,
           r.version,
           r.juror_email_details,
           r.juror_phone_details,
           r.staff_login,
           r.staff_assignment_date,
           r.urgent,
           r.super_urgent,
           r.welsh,
           r.reply_type,
           row_number() OVER (PARTITION BY j_1.juror_number ORDER BY p.return_date DESC) AS row_no
    FROM juror_mod.juror j_1
             LEFT JOIN juror_mod.juror_pool jp ON j_1.juror_number::text = jp.juror_number::text
             LEFT JOIN juror_mod.pool p ON jp.pool_number::text = p.pool_no::text
             LEFT JOIN juror_mod.juror_response r ON r.juror_number::text = j_1.juror_number::text
             LEFT JOIN juror_mod.summons_snapshot s ON r.juror_number::text = s.juror_number::text
    WHERE jp.is_active = true
)
   SELECT j.juror_number,
          j.pool_no,
          j.ret_date,
          c.loc_code,
          c.loc_name,
          c.loc_court_name,
          c.loc_address1,
          c.loc_address2,
          c.loc_address3,
          c.loc_address4,
          c.loc_address5,
          c.loc_address6,
          c.loc_zip,
          c.loc_attend_time,
          j.title,
          j.first_name,
          j.last_name,
          j.address_line_1 AS address,
          j.address_line_2 AS address2,
          j.address_line_3 AS address3,
          j.address_line_4 AS address4,
          j.address_line_5 AS address5,
          j.postcode AS zip,
          j.next_date,
          j.status,
          j.phone_number,
          j.alt_phone_number,
          j.dob,
          j.notes,
          j.email,
          j.last_update,
          j.new_title,
          j.new_first_name,
          j.new_last_name,
          j.new_address_1 AS new_address,
          j.new_address_2 AS new_address2,
          j.new_address_3 AS new_address3,
          j.new_address_4 AS new_address4,
          j.new_address_5 AS new_address5,
          j.new_postcode AS new_zip,
          j.date_received,
          j.processing_status,
          j.new_phone_number,
          j.new_alt_phone_number,
          j.new_dob,
          j.new_email,
          j.thirdparty_fname,
          j.thirdparty_lname,
          j.thirdparty_reason,
          j.thirdparty_other_reason,
          j.main_phone,
          j.other_phone,
          j.email_address,
          j.relationship,
          j.residency,
          j.residency_detail,
          j.mental_health_act,
          j.mental_health_act_details,
          j.bail,
          j.bail_details,
          j.convictions,
          j.convictions_details,
          j.deferral_reason,
          j.deferral_date,
          j.reasonable_adjustments_arrangements,
          j.excusal_reason,
          j.processing_complete,
          j.completed_at,
          j.version,
          j.juror_email_details,
          j.juror_phone_details,
          j.staff_login,
          j.staff_assignment_date,
          j.urgent,
          j.super_urgent,
          j.welsh,
          j.reply_type,
          CASE
              WHEN wl.loc_code IS NULL THEN false
              ELSE true
              END AS welsh_court
   FROM juror_details_cte j
            JOIN juror_mod.court_location c ON j.loc_code::text = c.loc_code::text
            LEFT JOIN juror_mod.welsh_court_location wl ON c.loc_code::text = wl.loc_code::text
   WHERE j.row_no = 1;

