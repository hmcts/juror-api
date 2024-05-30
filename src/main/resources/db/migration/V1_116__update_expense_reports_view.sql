drop view juror_mod.reports_juror_payments_summary;
CREATE OR REPLACE VIEW juror_mod.reports_juror_payments_summary
AS with payment_audits as (
	select		fad.juror_number,
				fad.loc_code,
				fad.id as latest_payment_f_audit_id
	from		juror_mod.financial_audit_details fad
	where		fad."type" in ('APPROVED_BACS', 'APPROVED_CASH', 'REAPPROVED_CASH', 'REAPPROVED_BACS')
)
, attendance_dates as (
	select		pa.juror_number,
				fada.loc_code,
				max(fada.financial_audit_id) as latest_payment_f_audit_id,
				fada.attendance_date
	from		juror_mod.financial_audit_details_appearances fada
	inner join	payment_audits pa
		on		pa.loc_code = fada.loc_code
				and pa.latest_payment_f_audit_id = fada.financial_audit_id
	group by 	juror_number, fada.loc_code, attendance_date
	order by	juror_number
)
select	a.trial_number,
				a.non_attendance,
				a.loc_code,
				a.attendance_date,
				a.juror_number,
				j.first_name,
				j.last_name,
				a.pool_number,
				ad.latest_payment_f_audit_id,
				a.time_in as checked_in,
				a.time_out as checked_out,
				a.time_out - a.time_in as hours_attended,
				a.attendance_audit_number as attendance_audit,
				fad.created_on as payment_date,
				(
					coalesce(a.public_transport_total_due,0) +
					coalesce(a.hired_vehicle_total_due,0) +
					coalesce(a.motorcycle_total_due,0) +
					coalesce(a.car_total_due,0) +
					coalesce(a.pedal_cycle_total_due,0) +
					coalesce(a.parking_total_due,0)
				) as total_travel_due,
				(
					coalesce(a.loss_of_earnings_due,0) +
					coalesce(a.childcare_total_due,0) +
					coalesce(a.misc_total_due, 0)
				) as total_financial_loss_due,
				coalesce(a.subsistence_due, 0) as subsistence_due,
				coalesce(a.smart_card_due,0) as smart_card_due,
				(
					coalesce(a.public_transport_total_due, 0) +
					coalesce(a.hired_vehicle_total_due, 0) +
					coalesce(a.motorcycle_total_due, 0) +
					coalesce(a.car_total_due, 0) +
					coalesce(a.pedal_cycle_total_due, 0) +
					coalesce(a.parking_total_due, 0) +
					coalesce(a.loss_of_earnings_due, 0) +
					coalesce(a.childcare_total_due, 0) +
					coalesce(a.misc_total_due, 0) +
					coalesce(a.subsistence_due, 0) -
					coalesce(a.smart_card_due, 0)
				) as total_due,
					(
					coalesce(a.public_transport_total_paid, 0) +
					coalesce(a.hired_vehicle_total_paid, 0) +
					coalesce(a.motorcycle_total_paid, 0) +
					coalesce(a.car_total_paid, 0) +
					coalesce(a.pedal_cycle_total_paid, 0) +
					coalesce(a.parking_total_paid, 0) +
					coalesce(a.loss_of_earnings_paid, 0) +
					coalesce(a.childcare_total_paid, 0) +
					coalesce(a.misc_total_paid, 0) +
					coalesce(a.subsistence_paid, 0) -
					coalesce(a.smart_card_paid, 0)
				) as total_paid
from			juror_mod.appearance a
join			juror_mod.juror j
	on			a.juror_number = j.juror_number
left join		attendance_dates ad
	on			ad.juror_number = a.juror_number
				and ad.attendance_date = a.attendance_date
				and ad.loc_code = a.loc_code
left join		juror_mod.financial_audit_details fad
	on			fad.juror_number = j.juror_number
				and fad.loc_code = a.loc_code
				and fad.id = ad.latest_payment_f_audit_id
