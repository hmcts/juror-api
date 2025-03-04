
do
$$begin if not exists (select * from pg_catalog.pg_roles where rolname = 'juror_eric') then
    CREATE ROLE juror_eric WITH
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

--
-- Create the schema juror_eric
--
CREATE SCHEMA IF NOT EXISTS juror_eric;

-- Create the voters_temp table
CREATE TABLE juror_eric.voters_temp (
    loc_code character varying(3),
    part_no character varying(9),
    register_lett character varying(5),
    poll_number character varying(5),
    new_marker character varying(1),
    title character varying(10),
    lname character varying(20) NOT NULL,
    fname character varying(20) NOT NULL,
    dob date,
    flags character varying(2),
    address character varying(35) NOT NULL,
    address2 character varying(35),
    address3 character varying(35),
    address4 character varying(35),
    address5 character varying(35),
    address6 character varying(35),
    zip character varying(10),
    date_selected1 date,
    date_selected2 date,
    date_selected3 date,
    rec_num integer,
    perm_disqual character varying(1),
    source_id character varying(1),
    postcode_start character varying(10) GENERATED ALWAYS AS (split_part((zip)::text, ' '::text, 1)) STORED
);


ALTER TABLE ONLY juror_eric.voters_temp
    ADD CONSTRAINT voters_temp_pk PRIMARY KEY (loc_code, part_no);

CREATE INDEX voters_temp_loc_code_idx ON juror_eric.voters_temp USING btree (loc_code);

CREATE INDEX voters_temp_part_no_idx ON juror_eric.voters_temp USING btree (part_no);

CREATE INDEX voters_temp_postcode_start_idx ON juror_eric.voters_temp USING btree (postcode_start, loc_code, perm_disqual, flags, dob);

