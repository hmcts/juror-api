create or replace procedure juror_mod.create_bureau_snapshot(in juror_pool_rec record)
	language plpgsql
		as $procedure$

begin

	insert into juror_mod.bureau_snapshot (juror_number, pool_number, "owner", user_edtq, is_active, status, def_date, pool_seq, edit_tag, next_date, was_deferred, deferral_code, postpone, scan_code, last_update, reminder_sent, transfer_date, date_created, excusal_code, acc_exc, police_check)
	select		jp.juror_number,
				jp.pool_number,
				jp."owner",
				jp.user_edtq,
				jp.is_active,
				jp.status,
				jp.def_date,
				jp.pool_seq,
				jp.edit_tag,
				jp.next_date,
				jp.was_deferred,
				jp.deferral_code,
				jp.postpone,
				jp.scan_code,
				jp.last_update,
				jp.reminder_sent,
				current_date,
				jp.date_created,
				j.excusal_code,
				j.acc_exc,
				j.police_check
	from 		juror_mod.juror_pool jp
	inner join	juror_mod.juror j
		on		jp.juror_number = j.juror_number
	where 		jp.juror_number = juror_pool_rec.juror_number
				and jp.pool_number = juror_pool_rec.pool_number
				and jp."owner" = '400';
end;

$procedure$;

-- update the transfer_juror_pool procedure to call the new create_bureau_snapshot procedure before updating/transferring juror records
create or replace procedure juror_mod.transfer_juror_pool(in location_code character varying, in pd_latest_return_date date)
	language plpgsql
		as $procedure$

declare
	return_date date;
	rec record;
	juror_pool_cursor cursor (c_loc_code varchar(3)) for
		select
			row_number() over (order by jp.juror_number),
			p."owner",
			jp.pool_number,
			jp.juror_number,
			jp.status
		from
			juror_mod.juror_pool jp
		join juror_mod.pool p
		        on
			p.pool_no = jp.pool_number
		join juror_mod.court_location cl
		        on
			p.loc_code = cl.loc_code
		where
			jp.status in (1, 2, 11)
			and jp.owner = '400'
			and p.return_date <= pd_latest_return_date
			and p.loc_code = c_loc_code;

begin
    open juror_pool_cursor(location_code);

	loop
		fetch next from juror_pool_cursor into rec;

		exit
		when not found;

		call juror_mod.create_bureau_snapshot(rec);

		update
			juror_mod.juror_pool
		set
			"owner" = location_code,
			status =	case
							when status != 2 then 1
							else 2
						end
		where
			rec."owner" = "owner"
			and rec.pool_number = pool_number
			and rec.juror_number = juror_number
			and rec.status = status;

		update
			juror_mod.juror
		set
			bureau_transfer_date = current_date
		where
			juror_number = rec.juror_number;

	end loop;

	close juror_pool_cursor;

end;

$procedure$;