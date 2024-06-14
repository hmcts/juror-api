-- DROP PROCEDURE juror_mod.transfer_juror_pool(varchar, date);
CREATE OR REPLACE PROCEDURE juror_mod.transfer_juror_pool(IN location_code character varying, IN pd_latest_return_date date)
 LANGUAGE plpgsql
AS $procedure$

declare
	return_date date;
	rec record;
	juror_pool_cursor cursor (c_loc_code varchar(3)) for
		select      row_number() over (order by jp.juror_number),
                    p."owner",
                    jp.pool_number,
                    jp.juror_number,
                    jp.status
		from        juror_mod.juror_pool jp
		join        juror_mod.pool p
            on      p.pool_no = jp.pool_number
		where   	jp.status in (1, 2, 11)
                    and jp.owner = '400'
                    and p.return_date <= pd_latest_return_date
                    and p.loc_code = c_loc_code;

begin

    open juror_pool_cursor(location_code);

	loop

		fetch next from juror_pool_cursor into rec;
		exit when not found;

		call juror_mod.create_bureau_snapshot(rec);

		update      juror_mod.juror_pool jp
		set         "owner" = cl."owner",
                    status =	case
                                    when status = 11 then 1
                                    else status
                            end
		from	    juror_mod.court_location cl
		where	    cl.loc_code = location_code
                    and jp."owner" = '400'
                    and rec.pool_number = jp.pool_number
                    and rec.juror_number = jp.juror_number
                    and rec.status = jp.status;

		update		juror_mod.juror
		set 		bureau_transfer_date = current_date
		where		juror_number = rec.juror_number;

	end loop;

	close juror_pool_cursor;

end;

$procedure$;

-- update transfer_pool procedure (fix bug with owner being set to satellite loc_code)
create or replace
procedure juror_mod.transfer_pool(in location_code character varying,
in pd_latest_return_date date)
 language plpgsql
as $procedure$

declare
	ln_weeks_adjustment integer := 1;
	ln_weekday_adjustment integer;
	lc_xfer_day varchar(3) := 'fri';
	rec record;

	pool_cursor cursor (location_code varchar) for
		select		pool_no,
					return_date,
					no_requested,
					pool_type,
					p.loc_code,
					new_request
		from		juror_mod.pool p
		join 		juror_mod.court_location cl
		    on		cl.loc_code = p.loc_code
		where		p."owner" = '400'
					and return_date <= pd_latest_return_date
					and p.loc_code = location_code;

begin

    open pool_cursor(location_code);

	loop

		fetch next from pool_cursor into rec;

		exit when not found;

		update		juror_mod.pool p
		set			"owner" = cl."owner",
			    	new_request = 'N'
		from 		juror_mod.court_location cl
		where		cl.loc_code = p.loc_code
					and rec.pool_no = pool_no
					and p."owner" = '400'
					and return_date = rec.return_date;

	end loop;

	close pool_cursor;

end;

$procedure$;