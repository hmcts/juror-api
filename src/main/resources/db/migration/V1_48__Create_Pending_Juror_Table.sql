create table juror_mod.t_pending_juror_status
(
    code        varchar(1)  not null,
    description varchar(60) not null,
    constraint t_pending_juror_status_pkey primary key (code)
);

insert into juror_mod.t_pending_juror_status (code, description)
values ('A', 'Authorised'),
       ('Q', 'Queued'),
       ('R', 'Rejected');

create table juror_mod.pending_juror
(
    juror_number       varchar(9)       not null,
    pool_number        varchar(9)       not null,
    title              varchar(10)      null,
    last_name          varchar(20)      not null,
    first_name         varchar(20)      not null,
    dob                date             not null,
    address            varchar(35)      not null,
    address2           varchar(35)      null,
    address3           varchar(35)      null,
    address4           varchar(35)      not null,
    address5           varchar(35)      null,
    address6           varchar(35)      null,
    postcode           varchar(10)      not null,
    h_phone            varchar(15)      null,
    w_phone            varchar(15)      null,
    w_ph_local         varchar(4)       null,
    m_phone            varchar(15)      null,
    h_email            varchar(254)     null,
    contact_preference integer          null default 0,
    responded          bool             not null,
    next_date          date             null,
    date_added         date             not null,
    mileage            integer          null,
    pool_seq           varchar(4)       null,
    status             varchar(1)       not null,
    is_active          bool             null,
    added_by           varchar(20)      null,
    notes              varchar(2000)    null,
    date_created       timestamp        not null,
    constraint pending_juror_pk primary key (juror_number)
);

alter table juror_mod.pending_juror
    add constraint pending_juror_status_fk foreign key (status)
        references juror_mod.t_pending_juror_status (code);

create or replace function juror_mod.generatePendingJurorNumber(location_code text)
    returns text
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