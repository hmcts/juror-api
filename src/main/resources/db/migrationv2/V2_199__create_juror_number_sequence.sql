-- create the juror number sequence
CREATE SEQUENCE IF NOT EXISTS juror_mod.juror_number_seq
    INCREMENT BY 1
    MINVALUE 610000000
    MAXVALUE 639999999
    START 610000000
    CACHE 50
    NO CYCLE;
