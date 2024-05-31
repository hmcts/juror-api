/***********************************************************************************************************************
*  author  : phil head
*  created : 11 march 2024
*  purpose : transfer bureau owned pools to the court
*
*   change history:
*
*   ver  date     		author     	description
*   ---  ----     		------     	-----------
*	1.0	 11 mar 2024 	phil head	transfer bureau owned records to a court
***********************************************************************************************************************/


/**
Get the cutoff date for transferring the records.
+-----------------------+------------+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|         Name          |    Type    | IN/OUT |                                                                               Description                                                                               |
+-----------------------+------------+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| pn_weeks_adjustment   | varchar(3) | in     | Weeks in advance for transfer window                                                                                                                                    |
| pc_transfer_day       | varchar    | in     | day of the week for transfer                                                                                                                                            |
| pd_latest_return_date | date       | out    | the cutoff date for transferring records                                                                                                                                |
+-----------------------+------------+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
*/

create or replace procedure juror_mod.get_return_date(pn_weeks_adjustment integer, pc_transfer_day varchar, pd_latest_return_date out date)
language plpgsql
as
$$
declare
ln_day varchar(3); -- current day abbreivated as three letters aka 'mon'.
ld_effective_date date; -- the current date the job is ran on.
ln_weekday_adjustment integer; -- the amount of days the date needs to be adjusted so that it runs on the provided days e.g. current date is sunday and want to run on monday then this will be 1.
begin
    ld_effective_date :=  current_date;
    -- if the stored procedure is ran before 6pm then make it run as if it was running on the previous day.
    if (select floor(extract(epoch from current_time )) <= 64800) and (to_char(current_date, 'dy') = pc_transfer_day) then
        ld_effective_date := ld_effective_date - 1;
    end if;
    ln_day := to_char(ld_effective_date,'dy');

    select adjustment into ln_weekday_adjustment from juror_mod.pool_transfer_weekday
    where transfer_day = pc_transfer_day and run_day = ln_day;

    if ln_weekday_adjustment is null then
       ln_weekday_adjustment := 0;
    end if;

    ld_effective_date := ld_effective_date + ln_weekday_adjustment; -- add on the adjustment for the week day and transferred day
    pd_latest_return_date := ld_effective_date + 7*pn_weeks_adjustment; -- add on the number of weeks in advance jurors should be transferred

end;
$$;


/**
Transfer the juror pool records to the court.
+-----------------------+---------+--------+-------------------------------------------------------+
|         Name          |  Type   | IN/OUT |                      Description                      |
+-----------------------+---------+--------+-------------------------------------------------------+
| location_code         | varchar | in     | unique three digit code representing a court location |
| pd_latest_return_date | date    | in     | the cutoff date for transferring records              |
+-----------------------+---------+--------+-------------------------------------------------------+
*/
create or replace procedure juror_mod.transfer_juror_pool(location_code varchar, pd_latest_return_date date)
language plpgsql
as
$$
declare
	ln_weeks_adjustment integer := 1;
    lc_xfer_day varchar(3) := 'fri';
    return_date date;
    rec record;
    juror_pool_cursor cursor (c_loc_code varchar(3)) for
    select
        row_number() over (order by jp.juror_number),
        p."owner",
        jp.pool_number,
        jp.juror_number,
        jp.status
        from juror_mod.juror_pool jp
        join juror_mod.pool p
        on p.pool_no  = jp.pool_number
        join juror_mod.court_location cl
        on p.loc_code = cl.loc_code
        where jp.status in (1,2,11)
        and jp.owner = '400'
        and p.return_date <= pd_latest_return_date
        and p.loc_code = c_loc_code;
begin
    open juror_pool_cursor(location_code);
    loop
        fetch next from juror_pool_cursor into rec;
        exit when not found;

       update juror_mod.juror_pool
        set "owner" = location_code,
            status = case
	                        when status != 2 then 1 else 2 end
        where rec."owner" = "owner"
        and rec.pool_number = pool_number
        and rec.juror_number = juror_number
        and rec.status = status;

        update juror_mod.juror
        set bureau_transfer_date = current_date
        where juror_number = rec.juror_number;
    end loop;
    close juror_pool_cursor;
end;
$$;

/**
Transfer the pool record to the court.
+-----------------------+---------+--------+-------------------------------------------------------+
|         Name          |  Type   | IN/OUT |                      Description                      |
+-----------------------+---------+--------+-------------------------------------------------------+
| location_code         | varchar | in     | unique three digit code representing a court location |
| pd_latest_return_date | date    | in     | the cutoff date for transferring records              |
+-----------------------+---------+--------+-------------------------------------------------------+
*/
create or replace procedure juror_mod.transfer_pool(location_code varchar,  pd_latest_return_date date)
language plpgsql
as
$$
declare
	ln_weeks_adjustment integer := 1;
    ln_weekday_adjustment integer;
    lc_xfer_day varchar(3) := 'fri';
    rec record;
    pool_cursor cursor (location_code varchar) for
    select pool_no,
        return_date,
        no_requested,
        pool_type,
        p.loc_code,
        new_request
    from juror_mod.pool p
    join juror_mod.court_location cl
    on cl.loc_code = p.loc_code
    where p."owner" = '400'
    and return_date <= pd_latest_return_date
    and p.loc_code = location_code;
begin
    open pool_cursor(location_code);
    loop
	    fetch next from pool_cursor into rec;
    	exit when not found;
	    update juror_mod.pool
	    set "owner" = location_code,
	    new_request = 'N'
	    where rec.pool_no = pool_no
        and "owner" = '400'
	    and return_date = rec.return_date;
    end loop;
    close pool_cursor;
end;
$$;

-- Main execution
create or replace procedure juror_mod.transfer_pool_details()
language plpgsql
as
$$
declare
    ln_weeks_adjustment integer;
    lc_xfer_day varchar(3);
    latest_return_date date;
    court_record record;
    courts_cursor cursor for select distinct loc_code from juror_mod.court_location where "owner" <> '400';
begin
    select sp_value into ln_weeks_adjustment from juror_mod.system_parameter where sp_id = 7;
    if ln_weeks_adjustment is null then
        ln_weeks_adjustment := 1;
    end if;

    select sp_value into lc_xfer_day from juror_mod.system_parameter where sp_id = 8;
    if lc_xfer_day is null then
        lc_xfer_day := 'fri';
    end if;

    call juror_mod.get_return_date(ln_weeks_adjustment::integer, lc_xfer_day::varchar, latest_return_date);

    open courts_cursor;

    loop
        fetch next from courts_cursor into court_record;
        exit when not found;
        call juror_mod.transfer_juror_pool(court_record.loc_code::varchar, latest_return_date::date);
        call juror_mod.transfer_pool(court_record.loc_code::varchar, latest_return_date::date);
    end loop;
    close courts_cursor;
end;
$$;