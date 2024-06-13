alter table juror_mod.juror
    ALTER COLUMN h_phone TYPE varchar(19),
    ALTER COLUMN m_phone TYPE varchar(19),
    ALTER COLUMN w_phone TYPE varchar(19);

  alter table juror_mod.juror_response
      ALTER COLUMN phone_number TYPE varchar(19),
      ALTER COLUMN alt_phone_number TYPE varchar(19);
